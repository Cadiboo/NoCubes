package cadiboo.nocubes.client.render;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModChunkRenderDispatcher extends ChunkRenderDispatcher {

	public static final Method renderWorkerProcessTask = ReflectionHelper.findMethod(ChunkRenderWorker.class, "processTask", "null", ChunkCompileTaskGenerator.class);
	//	public static final Class<?> chunkRenderDispatcherPendingUpload = Class.forName( "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.PendingUpload");


	public static final Logger LOGGER = LogManager.getLogger();

	public final List<Thread> listWorkerThreads;
	public final Queue<ModChunkRenderDispatcher.PendingUpload> queueChunkUploads;
	public final PriorityBlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates;
	public final ChunkRenderWorker renderWorker;

	public ModChunkRenderDispatcher()
	{
		this(-1);
	}

	public ModChunkRenderDispatcher(final int countRenderBuilders) {
		super(countRenderBuilders);



		this.listWorkerThreads = ReflectionHelper.getPrivateValue(ChunkRenderDispatcher.class, (ChunkRenderDispatcher)this, "listWorkerThreads");
		this.queueChunkUploads = ReflectionHelper.getPrivateValue(ChunkRenderDispatcher.class, (ChunkRenderDispatcher)this, "queueChunkUploads");
		this.queueChunkUpdates = ReflectionHelper.getPrivateValue(ChunkRenderDispatcher.class, (ChunkRenderDispatcher)this, "queueChunkUpdates");
		this.renderWorker = ReflectionHelper.getPrivateValue(ChunkRenderDispatcher.class, (ChunkRenderDispatcher)this, "renderWorker");

	}

	@Override
	public boolean runChunkUploads(final long finishTimeNano)
	{
		boolean flag = false;

		while (true)
		{
			boolean flag1 = false;

			if (this.listWorkerThreads.isEmpty())
			{
				final ChunkCompileTaskGenerator chunkcompiletaskgenerator = this.queueChunkUpdates.poll();

				if (chunkcompiletaskgenerator != null)
				{
					try
					{
						renderWorkerProcessTask.invoke(this.renderWorker, chunkcompiletaskgenerator);
						//	this.renderWorker.processTask(chunkcompiletaskgenerator);
						flag1 = true;
					}
					//catch (final InterruptedException var8)
					//{
					//	LOGGER.warn("Skipped task due to interrupt");
					//}
					catch (final Throwable t)
					{
						LOGGER.warn("Skipped task due to interrupt");
					}

				}
			}

			synchronized (this.queueChunkUploads)
			{
				if (!this.queueChunkUploads.isEmpty())
				{
					(this.queueChunkUploads.poll()).uploadTask.run();
					flag1 = true;
					flag = true;
				}
			}

			if ((finishTimeNano == 0L) || !flag1 || (finishTimeNano < System.nanoTime()))
			{
				break;
			}
		}

		return flag;
	}

	@Override
	public ListenableFuture<Object> uploadChunk(final BlockRenderLayer p_188245_1_, final BufferBuilder p_188245_2_, final RenderChunk renderChunk, final CompiledChunk p_188245_4_, final double p_188245_5_)
	{
		if (Minecraft.getMinecraft().isCallingFromMinecraftThread())
		{
			return super.uploadChunk(p_188245_1_, p_188245_2_, renderChunk, p_188245_4_, p_188245_5_);
		}
		else
		{
			final ListenableFutureTask<Object> listenablefuturetask = ListenableFutureTask.<Object>create(() -> ModChunkRenderDispatcher.this.uploadChunk(p_188245_1_, p_188245_2_, renderChunk, p_188245_4_, p_188245_5_), (Object)null);

			synchronized (this.queueChunkUploads)
			{
				this.queueChunkUploads.add(new ModChunkRenderDispatcher.PendingUpload(listenablefuturetask, p_188245_5_));
				return listenablefuturetask;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static class PendingUpload implements Comparable<ModChunkRenderDispatcher.PendingUpload>
	{
		private final ListenableFutureTask<Object> uploadTask;
		private final double distanceSq;

		public PendingUpload(final ListenableFutureTask<Object> uploadTaskIn, final double distanceSqIn)
		{
			this.uploadTask = uploadTaskIn;
			this.distanceSq = distanceSqIn;
		}

		@Override
		public int compareTo(final ModChunkRenderDispatcher.PendingUpload p_compareTo_1_)
		{
			return Doubles.compare(this.distanceSq, p_compareTo_1_.distanceSq);
		}
	}


}
