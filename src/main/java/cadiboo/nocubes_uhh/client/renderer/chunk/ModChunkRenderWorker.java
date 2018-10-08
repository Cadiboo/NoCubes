package cadiboo.nocubes_uhh.client.renderer.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import cadiboo.nocubes_uhh.client.renderer.ModRegionRenderCacheBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModChunkRenderWorker extends net.minecraft.client.renderer.chunk.ChunkRenderWorker {
	public static final Logger					LOGGER	= LogManager.getLogger();
	public final ModChunkRenderDispatcher		chunkRenderDispatcher;
	public final ModRegionRenderCacheBuilder	regionRenderCacheBuilder;
	public boolean								shouldRun;

	public ModChunkRenderWorker(final ModChunkRenderDispatcher chunkRenderDispatcherIn) {
		this(chunkRenderDispatcherIn, null);
	}

	public ModChunkRenderWorker(final ModChunkRenderDispatcher chunkRenderDispatcherIn, @Nullable final ModRegionRenderCacheBuilder regionRenderCacheBuilderIn) {
		super(chunkRenderDispatcherIn, regionRenderCacheBuilderIn);
		this.shouldRun = true;
		this.chunkRenderDispatcher = chunkRenderDispatcherIn;
		this.regionRenderCacheBuilder = regionRenderCacheBuilderIn;
	}

	@Override
	public void run() {
		while (this.shouldRun) {
			try {
				this.processTask(this.chunkRenderDispatcher.getNextChunkUpdate());
			} catch (final InterruptedException var3) {
				LOGGER.debug("Stopping chunk worker due to interrupt");
				return;
			} catch (final Throwable throwable) {
				final CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Batching chunks");
				Minecraft.getMinecraft().crashed(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
				return;
			}
		}
	}

	protected void processTask(final ModChunkCompileTaskGenerator generator) throws InterruptedException {
		generator.getLock().lock();

		try {
			if (generator.getStatus() != ModChunkCompileTaskGenerator.Status.PENDING) {
				if (!generator.isFinished()) {
					LOGGER.warn("Chunk render task was {} when I expected it to be pending; ignoring task", generator.getStatus());
				}

				return;
			}

			final BlockPos blockpos = new BlockPos(Minecraft.getMinecraft().player);
			final BlockPos blockpos1 = generator.getRenderChunk().getPosition();
			final int i = 16;
			final int j = 8;
			final int k = 24;

			if (blockpos1.add(8, 8, 8).distanceSq(blockpos) > 576.0D) {
				final World world = generator.getRenderChunk().getWorld();
				final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(blockpos1);

				if (!this.isChunkExisting(blockpos$mutableblockpos.setPos(blockpos1).move(EnumFacing.WEST, 16), world) || !this.isChunkExisting(blockpos$mutableblockpos.setPos(blockpos1).move(EnumFacing.NORTH, 16), world) || !this.isChunkExisting(blockpos$mutableblockpos.setPos(blockpos1).move(EnumFacing.EAST, 16), world) || !this.isChunkExisting(blockpos$mutableblockpos.setPos(blockpos1).move(EnumFacing.SOUTH, 16), world)) {
					return;
				}
			}

			generator.setStatus(ModChunkCompileTaskGenerator.Status.COMPILING);
		} finally {
			generator.getLock().unlock();
		}

		final Entity entity = Minecraft.getMinecraft().getRenderViewEntity();

		if (entity == null) {
			generator.finish();
		} else {
			generator.setRegionRenderCacheBuilder(this.getRegionRenderCacheBuilder());
			final float f = (float) entity.posX;
			final float f1 = (float) entity.posY + entity.getEyeHeight();
			final float f2 = (float) entity.posZ;
			final ModChunkCompileTaskGenerator.Type chunkcompiletaskgenerator$type = generator.getType();

			if (chunkcompiletaskgenerator$type == ModChunkCompileTaskGenerator.Type.REBUILD_CHUNK) {
				generator.getRenderChunk().rebuildChunk(f, f1, f2, generator);
			} else if (chunkcompiletaskgenerator$type == ModChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY) {
				generator.getRenderChunk().resortTransparency(f, f1, f2, generator);
			}

			generator.getLock().lock();

			try {
				if (generator.getStatus() != ModChunkCompileTaskGenerator.Status.COMPILING) {
					if (!generator.isFinished()) {
						LOGGER.warn("Chunk render task was {} when I expected it to be compiling; aborting task", generator.getStatus());
					}

					this.freeRenderBuilder(generator);
					return;
				}

				generator.setStatus(ModChunkCompileTaskGenerator.Status.UPLOADING);
			} finally {
				generator.getLock().unlock();
			}

			final ModCompiledChunk compiledchunk = generator.getCompiledChunk();
			final ArrayList arraylist = Lists.newArrayList();

			if (chunkcompiletaskgenerator$type == ModChunkCompileTaskGenerator.Type.REBUILD_CHUNK) {
				for (final BlockRenderLayer blockrenderlayer : BlockRenderLayer.values()) {
					if (compiledchunk.isLayerStarted(blockrenderlayer)) {
						arraylist.add(this.chunkRenderDispatcher.uploadChunk(blockrenderlayer, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(blockrenderlayer), generator.getRenderChunk(), compiledchunk, generator.getDistanceSq()));
					}
				}
			} else if (chunkcompiletaskgenerator$type == ModChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY) {
				arraylist.add(this.chunkRenderDispatcher.uploadChunk(BlockRenderLayer.TRANSLUCENT, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT), generator.getRenderChunk(), compiledchunk, generator.getDistanceSq()));
			}

			final ListenableFuture<List<Object>> listenablefuture = Futures.allAsList(arraylist);
			generator.addFinishRunnable(() -> listenablefuture.cancel(false));
			Futures.addCallback(listenablefuture, new FutureCallback<List<Object>>() {
				@Override
				public void onSuccess(@Nullable final List<Object> p_onSuccess_1_) {
					ModChunkRenderWorker.this.freeRenderBuilder(generator);
					generator.getLock().lock();
					label49: {
						try {
							if (generator.getStatus() == ModChunkCompileTaskGenerator.Status.UPLOADING) {
								generator.setStatus(ModChunkCompileTaskGenerator.Status.DONE);
								break label49;
							}

							if (!generator.isFinished()) {
								ModChunkRenderWorker.LOGGER.warn("Chunk render task was {} when I expected it to be uploading; aborting task", generator.getStatus());
							}
						} finally {
							generator.getLock().unlock();
						}

						return;
					}
					generator.getRenderChunk().setCompiledChunk(compiledchunk);
				}

				@Override
				public void onFailure(final Throwable p_onFailure_1_) {
					ModChunkRenderWorker.this.freeRenderBuilder(generator);

					if (!(p_onFailure_1_ instanceof CancellationException) && !(p_onFailure_1_ instanceof InterruptedException)) {
						Minecraft.getMinecraft().crashed(CrashReport.makeCrashReport(p_onFailure_1_, "Rendering chunk"));
					}
				}
			});
		}
	}

	private boolean isChunkExisting(final BlockPos pos, final World worldIn) {
		return !worldIn.getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4).isEmpty();
	}

	private ModRegionRenderCacheBuilder getRegionRenderCacheBuilder() throws InterruptedException {
		return this.regionRenderCacheBuilder != null ? this.regionRenderCacheBuilder : this.chunkRenderDispatcher.allocateRenderBuilder();
	}

	private void freeRenderBuilder(final ModChunkCompileTaskGenerator taskGenerator) {
		if (this.regionRenderCacheBuilder == null) {
			this.chunkRenderDispatcher.freeRenderBuilder(taskGenerator.getRegionRenderCacheBuilder());
		}
	}

	@Override
	public void notifyToStop() {
		this.shouldRun = false;
	}
}