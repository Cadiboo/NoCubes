package cadiboo.nocubes.client.renderer.chunk;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;

public class ModChunkCompileTaskGenerator extends ChunkCompileTaskGenerator {

	public final ModRenderChunk					renderChunk;
	public final ReentrantLock					lock				= new ReentrantLock();
	public final List<Runnable>					listFinishRunnables	= Lists.<Runnable>newArrayList();
	public final ChunkCompileTaskGenerator.Type	type;
	public final double							distanceSq;
	public RegionRenderCacheBuilder				regionRenderCacheBuilder;
	public ModCompiledChunk						compiledChunk;
	public ChunkCompileTaskGenerator.Status		status				= ChunkCompileTaskGenerator.Status.PENDING;
	public boolean								finished;

	public ModChunkCompileTaskGenerator(final ModRenderChunk renderChunkIn, final ChunkCompileTaskGenerator.Type typeIn, final double distanceSqIn) {
		super(renderChunkIn, typeIn, distanceSqIn);
		this.renderChunk = renderChunkIn;
		this.type = typeIn;
		this.distanceSq = distanceSqIn;
	}

	public ModChunkCompileTaskGenerator(final ChunkCompileTaskGenerator chunkCompileTaskGenerator) {
		this(new ModRenderChunk(chunkCompileTaskGenerator.getRenderChunk()), chunkCompileTaskGenerator.getType(), chunkCompileTaskGenerator.getDistanceSq());
	}

	@Override
	public ChunkCompileTaskGenerator.Status getStatus() {
		return this.status;
	}

	@Override
	public ModRenderChunk getRenderChunk() {
		return this.renderChunk;
	}

	@Override
	public ModCompiledChunk getCompiledChunk() {
		return this.compiledChunk;
	}

	public void setCompiledChunk(final ModCompiledChunk compiledChunkIn) {
		this.compiledChunk = compiledChunkIn;
	}

	@Override
	public RegionRenderCacheBuilder getRegionRenderCacheBuilder() {
		return this.regionRenderCacheBuilder;
	}

	@Override
	public void setRegionRenderCacheBuilder(final RegionRenderCacheBuilder regionRenderCacheBuilderIn) {
		this.regionRenderCacheBuilder = regionRenderCacheBuilderIn;
	}

	@Override
	public void setStatus(final ChunkCompileTaskGenerator.Status statusIn) {
		this.lock.lock();

		try {
			this.status = statusIn;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public void finish() {
		this.lock.lock();

		try {
			if ((this.type == ChunkCompileTaskGenerator.Type.REBUILD_CHUNK) && (this.status != ChunkCompileTaskGenerator.Status.DONE)) {
				this.renderChunk.setNeedsUpdate(false);
			}

			this.finished = true;
			this.status = ChunkCompileTaskGenerator.Status.DONE;

			for (final Runnable runnable : this.listFinishRunnables) {
				runnable.run();
			}
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public void addFinishRunnable(final Runnable runnable) {
		this.lock.lock();

		try {
			this.listFinishRunnables.add(runnable);

			if (this.finished) {
				runnable.run();
			}
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public ReentrantLock getLock() {
		return this.lock;
	}

	@Override
	public ChunkCompileTaskGenerator.Type getType() {
		return this.type;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

	@Override
	public double getDistanceSq() {
		return this.distanceSq;
	}

}
