package cadiboo.nocubes.client.renderer.chunk;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cadiboo.nocubes.client.renderer.ModRegionRenderCacheBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModChunkRenderDispatcher extends net.minecraft.client.renderer.chunk.ChunkRenderDispatcher {
	public static final Logger											LOGGER				= LogManager.getLogger();
	public static final ThreadFactory									THREAD_FACTORY		= (new ThreadFactoryBuilder()).setNameFormat("Chunk Batcher %d").setDaemon(true).build();
	public final int													countRenderBuilders;
	public final List<Thread>											listWorkerThreads	= Lists.<Thread>newArrayList();
	public final List<ModChunkRenderWorker>								listThreadedWorkers	= Lists.<ModChunkRenderWorker>newArrayList();
	public final PriorityBlockingQueue<ModChunkCompileTaskGenerator>	queueChunkUpdates	= Queues.<ModChunkCompileTaskGenerator>newPriorityBlockingQueue();
	public final BlockingQueue<ModRegionRenderCacheBuilder>				queueFreeRenderBuilders;
	public final WorldVertexBufferUploader								worldVertexUploader	= new WorldVertexBufferUploader();
	public final VertexBufferUploader									vertexUploader		= new VertexBufferUploader();
	public final Queue<ModChunkRenderDispatcher.PendingUpload>			queueChunkUploads	= Queues.<ModChunkRenderDispatcher.PendingUpload>newPriorityQueue();
	public final ModChunkRenderWorker									renderWorker;

	public ModChunkRenderDispatcher() {
		this(-1);
	}

	public ModChunkRenderDispatcher(int countRenderBuilders) {
		final int i = Math.max(1, (int) (Runtime.getRuntime().maxMemory() * 0.3D) / 10485760);
		final int j = Math.max(1, MathHelper.clamp(Runtime.getRuntime().availableProcessors(), 1, i / 5));
		if (countRenderBuilders < 0) {
			countRenderBuilders = MathHelper.clamp(j * 10, 1, i);
		}
		this.countRenderBuilders = countRenderBuilders;

		if (j > 1) {
			for (int k = 0; k < j; ++k) {
				final ModChunkRenderWorker chunkrenderworker = new ModChunkRenderWorker(this);
				final Thread thread = THREAD_FACTORY.newThread(chunkrenderworker);
				thread.start();
				this.listThreadedWorkers.add(chunkrenderworker);
				this.listWorkerThreads.add(thread);
			}
		}

		this.queueFreeRenderBuilders = Queues.<ModRegionRenderCacheBuilder>newArrayBlockingQueue(this.countRenderBuilders);

		for (int l = 0; l < this.countRenderBuilders; ++l) {
			this.queueFreeRenderBuilders.add(new ModRegionRenderCacheBuilder());
		}

		this.renderWorker = new ModChunkRenderWorker(this, new ModRegionRenderCacheBuilder());
	}

	@Override
	public String getDebugInfo() {
		return this.listWorkerThreads.isEmpty() ? String.format("pC: %03d, single-threaded", this.queueChunkUpdates.size()) : String.format("pC: %03d, pU: %1d, aB: %1d", this.queueChunkUpdates.size(), this.queueChunkUploads.size(), this.queueFreeRenderBuilders.size());
	}

	@Override
	public boolean runChunkUploads(final long finishTimeNano) {
		boolean flag = false;

		while (true) {
			boolean flag1 = false;

			if (this.listWorkerThreads.isEmpty()) {
				final ModChunkCompileTaskGenerator chunkcompiletaskgenerator = this.queueChunkUpdates.poll();

				if (chunkcompiletaskgenerator != null) {
					try {
						this.renderWorker.processTask(chunkcompiletaskgenerator);
						flag1 = true;
					} catch (final InterruptedException var8) {
						LOGGER.warn("Skipped task due to interrupt");
					}
				}
			}

			synchronized (this.queueChunkUploads) {
				if (!this.queueChunkUploads.isEmpty()) {
					(this.queueChunkUploads.poll()).uploadTask.run();
					flag1 = true;
					flag = true;
				}
			}

			if ((finishTimeNano == 0L) || !flag1 || (finishTimeNano < System.nanoTime())) {
				break;
			}
		}

		return flag;
	}

	public boolean updateChunkLater(final ModRenderChunk chunkRenderer) {
		chunkRenderer.getLockCompileTask().lock();
		boolean flag1;

		try {
			final ModChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();
			chunkcompiletaskgenerator.addFinishRunnable(() -> ModChunkRenderDispatcher.this.queueChunkUpdates.remove(chunkcompiletaskgenerator));
			final boolean flag = this.queueChunkUpdates.offer(chunkcompiletaskgenerator);

			if (!flag) {
				chunkcompiletaskgenerator.finish();
			}

			flag1 = flag;
		} finally {
			chunkRenderer.getLockCompileTask().unlock();
		}

		return flag1;
	}

	public boolean updateChunkNow(final ModRenderChunk chunkRenderer) {
		chunkRenderer.getLockCompileTask().lock();
		boolean flag;

		try {
			final ModChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();

			try {
				this.renderWorker.processTask(chunkcompiletaskgenerator);
			} catch (final InterruptedException var7) {
				;
			}

			flag = true;
		} finally {
			chunkRenderer.getLockCompileTask().unlock();
		}

		return flag;
	}

	@Override
	public void stopChunkUpdates() {
		this.clearChunkUpdates();
		final List<ModRegionRenderCacheBuilder> list = Lists.<ModRegionRenderCacheBuilder>newArrayList();

		while (list.size() != this.countRenderBuilders) {
			this.runChunkUploads(Long.MAX_VALUE);

			try {
				list.add(this.allocateRenderBuilder());
			} catch (final InterruptedException var3) {
				;
			}
		}

		this.queueFreeRenderBuilders.addAll(list);
	}

	public void freeRenderBuilder(final ModRegionRenderCacheBuilder p_178512_1_) {
		this.queueFreeRenderBuilders.add(p_178512_1_);
	}

	@Override
	public ModRegionRenderCacheBuilder allocateRenderBuilder() throws InterruptedException {
		if ((this.queueFreeRenderBuilders == null) || (this.queueFreeRenderBuilders.size() <= 0)) {
			return new ModRegionRenderCacheBuilder(super.allocateRenderBuilder());
		}
		return this.queueFreeRenderBuilders.take();
	}

	@Override
	public ModChunkCompileTaskGenerator getNextChunkUpdate() throws InterruptedException {
		
		
		if ((this.queueChunkUpdates == null) || (this.queueChunkUpdates.size() <= 0)) {
			return new ModChunkCompileTaskGenerator(super.getNextChunkUpdate());
		}
		return this.queueChunkUpdates.take();
	}

	public boolean updateTransparencyLater(final ModRenderChunk chunkRenderer) {
		chunkRenderer.getLockCompileTask().lock();
		boolean flag;

		try {
			final ModChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskTransparency();

			if (chunkcompiletaskgenerator == null) {
				flag = true;
				return flag;
			}

			chunkcompiletaskgenerator.addFinishRunnable(() -> ModChunkRenderDispatcher.this.queueChunkUpdates.remove(chunkcompiletaskgenerator));
			flag = this.queueChunkUpdates.offer(chunkcompiletaskgenerator);
		} finally {
			chunkRenderer.getLockCompileTask().unlock();
		}

		return flag;
	}

	public ListenableFuture<Object> uploadChunk(final BlockRenderLayer p_188245_1_, final BufferBuilder p_188245_2_, final ModRenderChunk p_188245_3_, final ModCompiledChunk p_188245_4_, final double p_188245_5_) {
		if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
			if (OpenGlHelper.useVbo()) {
				this.uploadVertexBuffer(p_188245_2_, p_188245_3_.getVertexBufferByLayer(p_188245_1_.ordinal()));
			} else {
				this.uploadDisplayList(p_188245_2_, ((ModListedRenderChunk) p_188245_3_).getDisplayList(p_188245_1_, p_188245_4_), p_188245_3_);
			}

			p_188245_2_.setTranslation(0.0D, 0.0D, 0.0D);
			return Futures.<Object>immediateFuture((Object) null);
		} else {
			final ListenableFutureTask<Object> listenablefuturetask = ListenableFutureTask.<Object>create(() -> ModChunkRenderDispatcher.this.uploadChunk(p_188245_1_, p_188245_2_, p_188245_3_, p_188245_4_, p_188245_5_), (Object) null);

			synchronized (this.queueChunkUploads) {
				this.queueChunkUploads.add(new ModChunkRenderDispatcher.PendingUpload(listenablefuturetask, p_188245_5_));
				return listenablefuturetask;
			}
		}
	}

	public void uploadDisplayList(final BufferBuilder bufferBuilderIn, final int list, final ModRenderChunk chunkRenderer) {
		GlStateManager.glNewList(list, 4864);
		GlStateManager.pushMatrix();
		chunkRenderer.multModelviewMatrix();
		this.worldVertexUploader.draw(bufferBuilderIn);
		GlStateManager.popMatrix();
		GlStateManager.glEndList();
	}

	public void uploadVertexBuffer(final BufferBuilder p_178506_1_, final VertexBuffer vertexBufferIn) {
		this.vertexUploader.setVertexBuffer(vertexBufferIn);
		this.vertexUploader.draw(p_178506_1_);
	}

	@Override
	public void clearChunkUpdates() {
		while (!this.queueChunkUpdates.isEmpty()) {
			final ModChunkCompileTaskGenerator chunkcompiletaskgenerator = this.queueChunkUpdates.poll();

			if (chunkcompiletaskgenerator != null) {
				chunkcompiletaskgenerator.finish();
			}
		}
	}

	@Override
	public boolean hasChunkUpdates() {
		return this.queueChunkUpdates.isEmpty() && this.queueChunkUploads.isEmpty();
	}

	@Override
	public void stopWorkerThreads() {
		this.clearChunkUpdates();

		for (final ModChunkRenderWorker chunkrenderworker : this.listThreadedWorkers) {
			chunkrenderworker.notifyToStop();
		}

		for (final Thread thread : this.listWorkerThreads) {
			try {
				thread.interrupt();
				thread.join();
			} catch (final InterruptedException interruptedexception) {
				LOGGER.warn("Interrupted whilst waiting for worker to die", interruptedexception);
			}
		}

		this.queueFreeRenderBuilders.clear();
	}

	@Override
	public boolean hasNoFreeRenderBuilders() {
		return this.queueFreeRenderBuilders.isEmpty();
	}

	@SideOnly(Side.CLIENT)
	class PendingUpload implements Comparable<ModChunkRenderDispatcher.PendingUpload> {
		public final ListenableFutureTask<Object>	uploadTask;
		public final double							distanceSq;

		public PendingUpload(final ListenableFutureTask<Object> uploadTaskIn, final double distanceSqIn) {
			this.uploadTask = uploadTaskIn;
			this.distanceSq = distanceSqIn;
		}

		@Override
		public int compareTo(final ModChunkRenderDispatcher.PendingUpload p_compareTo_1_) {
			return Doubles.compare(this.distanceSq, p_compareTo_1_.distanceSq);
		}
	}
}