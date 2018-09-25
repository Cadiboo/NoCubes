package cadiboo.nocubes.client.renderer.chunk;

import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import cadiboo.nocubes.client.renderer.ModRenderGlobal;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModRenderChunk extends net.minecraft.client.renderer.chunk.RenderChunk {
	public World							world;
	public final ModRenderGlobal			renderGlobal;
	public static int						renderChunksUpdated;
	public ModCompiledChunk					compiledChunk		= ModCompiledChunk.DUMMY;
	public final ReentrantLock				lockCompileTask		= new ReentrantLock();
	public final ReentrantLock				lockCompiledChunk	= new ReentrantLock();
	public ModChunkCompileTaskGenerator		compileTask;
	public final Set<TileEntity>			setTileEntities		= Sets.<TileEntity>newHashSet();
	public final int						index;
	public final FloatBuffer				modelviewMatrix		= GLAllocation.createDirectFloatBuffer(16);
	public final VertexBuffer[]				vertexBuffers		= new VertexBuffer[BlockRenderLayer.values().length];
	public AxisAlignedBB					boundingBox;
	public int								frameIndex			= -1;
	public boolean							needsUpdate			= true;
	public final BlockPos.MutableBlockPos	position			= new BlockPos.MutableBlockPos(-1, -1, -1);
	public final BlockPos.MutableBlockPos[]	mapEnumFacing		= new BlockPos.MutableBlockPos[6];
	public boolean							needsImmediateUpdate;
	public ChunkCache						worldView;

	public ModRenderChunk(final World worldIn, final ModRenderGlobal renderGlobalIn, final int indexIn) {
		super(worldIn, renderGlobalIn, indexIn);
		for (int i = 0; i < this.mapEnumFacing.length; ++i) {
			this.mapEnumFacing[i] = new BlockPos.MutableBlockPos();
		}

		this.world = worldIn;
		this.renderGlobal = renderGlobalIn;
		this.index = indexIn;

		if (OpenGlHelper.useVbo()) {
			for (int j = 0; j < BlockRenderLayer.values().length; ++j) {
				this.vertexBuffers[j] = new VertexBuffer(DefaultVertexFormats.BLOCK);
			}
		}
	}

	public ModRenderChunk(final net.minecraft.client.renderer.chunk.RenderChunk chunk) {
		this(chunk.getWorld(), ReflectionHelper.getPrivateValue(net.minecraft.client.renderer.chunk.RenderChunk.class, chunk, "renderGlobal"), ReflectionHelper.getPrivateValue(net.minecraft.client.renderer.chunk.RenderChunk.class, chunk, "frameIndex"));
	}

	@Override
	public boolean setFrameIndex(final int frameIndexIn) {
		if (this.frameIndex == frameIndexIn) {
			return false;
		} else {
			this.frameIndex = frameIndexIn;
			return true;
		}
	}

	@Override
	public VertexBuffer getVertexBufferByLayer(final int layer) {
		return this.vertexBuffers[layer];
	}

	/**
	 * Sets the RenderChunk base position
	 */
	@Override
	public void setPosition(final int x, final int y, final int z) {
		if ((x != this.position.getX()) || (y != this.position.getY()) || (z != this.position.getZ())) {
			this.stopCompileTask();
			this.position.setPos(x, y, z);
			this.boundingBox = new AxisAlignedBB(x, y, z, x + 16, y + 16, z + 16);

			for (final EnumFacing enumfacing : EnumFacing.values()) {
				this.mapEnumFacing[enumfacing.ordinal()].setPos(this.position).move(enumfacing, 16);
			}

			this.initModelviewMatrix();
		}
	}

	public void resortTransparency(final float x, final float y, final float z, final ModChunkCompileTaskGenerator generator) {
		final ModCompiledChunk compiledchunk = generator.getCompiledChunk();

		if ((compiledchunk.getState() != null) && !compiledchunk.isLayerEmpty(BlockRenderLayer.TRANSLUCENT)) {
			this.preRenderBlocks(generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT), this.position);
			generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT).setVertexState(compiledchunk.getState());
			this.postRenderBlocks(BlockRenderLayer.TRANSLUCENT, x, y, z, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT), compiledchunk);
		}
	}

	public void rebuildChunk(final float x, final float y, final float z, final ModChunkCompileTaskGenerator generator) {
		final ModCompiledChunk compiledchunk = new ModCompiledChunk();
		final int i = 1;
		final BlockPos blockpos = this.position;
		final BlockPos blockpos1 = blockpos.add(15, 15, 15);
		generator.getLock().lock();

		try {
			if (generator.getStatus() != ModChunkCompileTaskGenerator.Status.COMPILING) {
				return;
			}

			generator.setCompiledChunk(compiledchunk);
		} finally {
			generator.getLock().unlock();
		}

		final VisGraph lvt_9_1_ = new VisGraph();
		final HashSet lvt_10_1_ = Sets.newHashSet();

		if (!this.worldView.isEmpty()) {
			++renderChunksUpdated;
			final boolean[] aboolean = new boolean[BlockRenderLayer.values().length];
			final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

			for (final BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockpos, blockpos1)) {
				final IBlockState iblockstate = this.worldView.getBlockState(blockpos$mutableblockpos);
				final Block block = iblockstate.getBlock();

				if (iblockstate.isOpaqueCube()) {
					lvt_9_1_.setOpaqueCube(blockpos$mutableblockpos);
				}

				if (block.hasTileEntity(iblockstate)) {
					final TileEntity tileentity = this.worldView.getTileEntity(blockpos$mutableblockpos, Chunk.EnumCreateEntityType.CHECK);

					if (tileentity != null) {
						final TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer = TileEntityRendererDispatcher.instance.<TileEntity>getRenderer(tileentity);

						if (tileentityspecialrenderer != null) {

							if (tileentityspecialrenderer.isGlobalRenderer(tileentity)) {
								lvt_10_1_.add(tileentity);
							} else {
								compiledchunk.addTileEntity(tileentity); // FORGE: Fix MC-112730
							}
						}
					}
				}

				for (final BlockRenderLayer blockrenderlayer1 : BlockRenderLayer.values()) {
					if (!block.canRenderInLayer(iblockstate, blockrenderlayer1)) {
						continue;
					}
					net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockrenderlayer1);
					final int j = blockrenderlayer1.ordinal();

					if (block.getDefaultState().getRenderType() != EnumBlockRenderType.INVISIBLE) {
						final BufferBuilder bufferbuilder = generator.getRegionRenderCacheBuilder().getWorldRendererByLayerId(j);

						if (!compiledchunk.isLayerStarted(blockrenderlayer1)) {
							compiledchunk.setLayerStarted(blockrenderlayer1);
							this.preRenderBlocks(bufferbuilder, blockpos);
						}

						aboolean[j] |= blockrendererdispatcher.renderBlock(iblockstate, blockpos$mutableblockpos, this.worldView, bufferbuilder);
					}
				}
				net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
			}

			for (final BlockRenderLayer blockrenderlayer : BlockRenderLayer.values()) {
				if (aboolean[blockrenderlayer.ordinal()]) {
					compiledchunk.setLayerUsed(blockrenderlayer);
				}

				if (compiledchunk.isLayerStarted(blockrenderlayer)) {
					this.postRenderBlocks(blockrenderlayer, x, y, z, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(blockrenderlayer), compiledchunk);
				}
			}
		}

		compiledchunk.setVisibility(lvt_9_1_.computeVisibility());
		this.lockCompileTask.lock();

		try {
			final Set<TileEntity> set = Sets.newHashSet(lvt_10_1_);
			final Set<TileEntity> set1 = Sets.newHashSet(this.setTileEntities);
			set.removeAll(this.setTileEntities);
			set1.removeAll(lvt_10_1_);
			this.setTileEntities.clear();
			this.setTileEntities.addAll(lvt_10_1_);
			this.renderGlobal.updateTileEntities(set1, set);
		} finally {
			this.lockCompileTask.unlock();
		}
	}

	@Override
	public void finishCompileTask() {
		this.lockCompileTask.lock();

		try {
			if ((this.compileTask != null) && (this.compileTask.getStatus() != ModChunkCompileTaskGenerator.Status.DONE)) {
				this.compileTask.finish();
				this.compileTask = null;
			}
		} finally {
			this.lockCompileTask.unlock();
		}
	}

	@Override
	public ReentrantLock getLockCompileTask() {
		return this.lockCompileTask;
	}

	@Override
	public ModChunkCompileTaskGenerator makeCompileTaskChunk() {
		this.lockCompileTask.lock();
		ModChunkCompileTaskGenerator chunkcompiletaskgenerator;

		try {
			this.finishCompileTask();
			this.compileTask = new ModChunkCompileTaskGenerator(this, ModChunkCompileTaskGenerator.Type.REBUILD_CHUNK, this.getDistanceSq());
			this.rebuildWorldView();
			chunkcompiletaskgenerator = this.compileTask;
		} finally {
			this.lockCompileTask.unlock();
		}

		return chunkcompiletaskgenerator;
	}

	public void rebuildWorldView() {
		final int i = 1;
		final ChunkCache cache = this.createRegionRenderCache(this.world, this.position.add(-1, -1, -1), this.position.add(16, 16, 16), 1);
		net.minecraftforge.client.MinecraftForgeClient.onRebuildChunk(this.world, this.position, cache);
		this.worldView = cache;
	}

	@Override
	@Nullable
	public ModChunkCompileTaskGenerator makeCompileTaskTransparency() {
		this.lockCompileTask.lock();
		ModChunkCompileTaskGenerator chunkcompiletaskgenerator;

		try {
			if ((this.compileTask == null) || (this.compileTask.getStatus() != ModChunkCompileTaskGenerator.Status.PENDING)) {
				if ((this.compileTask != null) && (this.compileTask.getStatus() != ModChunkCompileTaskGenerator.Status.DONE)) {
					this.compileTask.finish();
					this.compileTask = null;
				}

				this.compileTask = new ModChunkCompileTaskGenerator(this, ModChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY, this.getDistanceSq());
				this.compileTask.setCompiledChunk(this.compiledChunk);
				chunkcompiletaskgenerator = this.compileTask;
				return chunkcompiletaskgenerator;
			}

			chunkcompiletaskgenerator = null;
		} finally {
			this.lockCompileTask.unlock();
		}

		return chunkcompiletaskgenerator;
	}

	@Override
	public double getDistanceSq() {
		final EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
		final double d0 = (this.boundingBox.minX + 8.0D) - entityplayersp.posX;
		final double d1 = (this.boundingBox.minY + 8.0D) - entityplayersp.posY;
		final double d2 = (this.boundingBox.minZ + 8.0D) - entityplayersp.posZ;
		return (d0 * d0) + (d1 * d1) + (d2 * d2);
	}

	public void preRenderBlocks(final BufferBuilder bufferBuilderIn, final BlockPos pos) {
		bufferBuilderIn.begin(7, DefaultVertexFormats.BLOCK);
		bufferBuilderIn.setTranslation((-pos.getX()), (-pos.getY()), (-pos.getZ()));
	}

	public void postRenderBlocks(final BlockRenderLayer layer, final float x, final float y, final float z, final BufferBuilder bufferBuilderIn, final ModCompiledChunk compiledChunkIn) {
		if ((layer == BlockRenderLayer.TRANSLUCENT) && !compiledChunkIn.isLayerEmpty(layer)) {
			bufferBuilderIn.sortVertexData(x, y, z);
			compiledChunkIn.setState(bufferBuilderIn.getVertexState());
		}

		bufferBuilderIn.finishDrawing();
	}

	public void initModelviewMatrix() {
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		final float f = 1.000001F;
		GlStateManager.translate(-8.0F, -8.0F, -8.0F);
		GlStateManager.scale(1.000001F, 1.000001F, 1.000001F);
		GlStateManager.translate(8.0F, 8.0F, 8.0F);
		GlStateManager.getFloat(2982, this.modelviewMatrix);
		GlStateManager.popMatrix();
	}

	@Override
	public void multModelviewMatrix() {
		GlStateManager.multMatrix(this.modelviewMatrix);
	}

	@Override
	public ModCompiledChunk getCompiledChunk() {
		return this.compiledChunk;
	}

	public void setCompiledChunk(final ModCompiledChunk compiledChunkIn) {
		this.lockCompiledChunk.lock();

		try {
			this.compiledChunk = compiledChunkIn;
		} finally {
			this.lockCompiledChunk.unlock();
		}
	}

	@Override
	public void setCompiledChunk(final net.minecraft.client.renderer.chunk.CompiledChunk compiledChunkIn) {
		this.lockCompiledChunk.lock();

		try {
			this.compiledChunk = new ModCompiledChunk(compiledChunkIn);
		} finally {
			this.lockCompiledChunk.unlock();
		}
	}

	@Override
	public void stopCompileTask() {
		this.finishCompileTask();
		this.compiledChunk = ModCompiledChunk.DUMMY;
	}

	@Override
	public void deleteGlResources() {
		this.stopCompileTask();
		this.world = null;

		for (int i = 0; i < BlockRenderLayer.values().length; ++i) {
			if (this.vertexBuffers[i] != null) {
				this.vertexBuffers[i].deleteGlBuffers();
			}
		}
	}

	@Override
	public BlockPos getPosition() {
		return this.position;
	}

	@Override
	public void setNeedsUpdate(boolean immediate) {
		if (this.needsUpdate) {
			immediate |= this.needsImmediateUpdate;
		}

		this.needsUpdate = true;
		this.needsImmediateUpdate = immediate;
	}

	@Override
	public void clearNeedsUpdate() {
		this.needsUpdate = false;
		this.needsImmediateUpdate = false;
	}

	@Override
	public boolean needsUpdate() {
		return this.needsUpdate;
	}

	@Override
	public boolean needsImmediateUpdate() {
		return this.needsUpdate && this.needsImmediateUpdate;
	}

	/* ======================================== FORGE START ===================================== */
	/**
	 * Creates a new RegionRenderCache instance.<br>
	 * Extending classes can change the behavior of the cache, allowing to visually change blocks (schematics etc).
	 *
	 * @see RegionRenderCache
	 * @param world    The world to cache.
	 * @param from     The starting position of the chunk minus one on each axis.
	 * @param to       The ending position of the chunk plus one on each axis.
	 * @param subtract Padding used internally by the RegionRenderCache constructor to make the cache a 20x20x20 cube, for a total of 8000 states in the cache.
	 * @return new RegionRenderCache instance
	 */
	@Override
	public ChunkCache createRegionRenderCache(final World world, final BlockPos from, final BlockPos to, final int subtract) {
		return new ChunkCache(world, from, to, subtract);
	}
	/* ========================================= FORGE END ====================================== */

	@Override
	public BlockPos getBlockPosOffset16(final EnumFacing facing) {
		return this.mapEnumFacing[facing.ordinal()];
	}

	@Override
	public World getWorld() {
		return this.world;
	}
}