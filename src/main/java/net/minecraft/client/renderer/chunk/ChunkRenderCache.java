//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.client.renderer.chunk;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.CreateEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Iterator;

@OnlyIn(Dist.CLIENT)
public class ChunkRenderCache implements IEnviromentBlockReader {

	public final int chunkStartX;
	public final int chunkStartZ;
	public final BlockPos cacheStartPos;
	public final int cacheSizeX;
	public final int cacheSizeY;
	public final int cacheSizeZ;
	public final Chunk[][] chunks;
	public final BlockState[] blockStates;
	public final IFluidState[] fluidStates;
	public final World world;

	public ChunkRenderCache(World world, int chunkStartX, int chunkStartZ, Chunk[][] chunks, BlockPos start, BlockPos end) {
		this.world = world;
		this.chunkStartX = chunkStartX;
		this.chunkStartZ = chunkStartZ;
		this.chunks = chunks;
		this.cacheStartPos = start;

		// Start removed code
//		this.cacheSizeX = end.getX() - start.getX() + 1;
//		this.cacheSizeY = end.getY() - start.getY() + 1;
//		this.cacheSizeZ = end.getZ() - start.getZ() + 1;
//		this.blockStates = new BlockState[this.cacheSizeX * this.cacheSizeY * this.cacheSizeZ];
//		this.fluidStates = new IFluidState[this.cacheSizeX * this.cacheSizeY * this.cacheSizeZ];
//
//		BlockPos lvt_8_1_;
//		Chunk lvt_11_1_;
//		int lvt_12_1_;
//		for(Iterator var7 = BlockPos.getAllInBoxMutable(start, end).iterator(); var7.hasNext(); this.fluidStates[lvt_12_1_] = lvt_11_1_.getFluidState(lvt_8_1_)) {
//			lvt_8_1_ = (BlockPos)var7.next();
//			int lvt_9_1_ = (lvt_8_1_.getX() >> 4) - chunkStartX;
//			int lvt_10_1_ = (lvt_8_1_.getZ() >> 4) - chunkStartZ;
//			lvt_11_1_ = chunks[lvt_9_1_][lvt_10_1_];
//			lvt_12_1_ = this.getIndex(lvt_8_1_);
//			this.blockStates[lvt_12_1_] = lvt_11_1_.getBlockState(lvt_8_1_);
//		}
		// End removed code

		// Start added code
		final int startX = start.getX();
		final int startY = start.getY();
		final int startZ = start.getZ();

		final int cacheSizeX = end.getX() - startX + 1;
		final int cacheSizeY = end.getY() - startY + 1;
		final int cacheSizeZ = end.getZ() - startZ + 1;

		final int size = cacheSizeX * cacheSizeY * cacheSizeZ;
		final BlockState[] blockStates = new BlockState[size];
		final IFluidState[] fluidStates = new IFluidState[size];

		int cx = (startX >> 4) - chunkStartX;
		int cz = (startZ >> 4) - chunkStartZ;
		Chunk currentChunk = chunks[cx][cz];

		try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain()) {
			int index = 0;
			for (int z = 0; z < cacheSizeZ; ++z) {
				for (int y = 0; y < cacheSizeY; ++y) {
					for (int x = 0; x < cacheSizeX; ++x, ++index) {

						final int posX = startX + x;
						final int posY = startY + y;
						final int posZ = startZ + z;

						final int ccx = ((startX + x) >> 4) - chunkStartX;
						final int ccz = ((startZ + z) >> 4) - chunkStartZ;

						boolean changed = false;
						if (cx != ccx) {
							cx = ccx;
							changed = true;
						}
						if (cz != ccz) {
							cz = ccz;
							changed = true;
						}
						if (changed) {
							currentChunk = chunks[cx][cz];
						}

						pooledMutableBlockPos.setPos(posX, posY, posZ);
						blockStates[index] = currentChunk.getBlockState(pooledMutableBlockPos);
						fluidStates[index] = currentChunk.getFluidState(posX, posY, posZ);
					}
				}
			}
		}

		this.cacheSizeX = cacheSizeX;
		this.cacheSizeY = cacheSizeY;
		this.cacheSizeZ = cacheSizeZ;

		this.blockStates = blockStates;
		this.fluidStates = fluidStates;
		// End added code

	}

	@Nullable
	public static ChunkRenderCache generateCache(World world, BlockPos start, BlockPos end, int padding) {
		int chunkStartX = start.getX() - padding >> 4;
		int chunkStartZ = start.getZ() - padding >> 4;
		int chunkEndX = end.getX() + padding >> 4;
		int chunkEndZ = end.getZ() + padding >> 4;
		Chunk[][] chunks = new Chunk[chunkEndX - chunkStartX + 1][chunkEndZ - chunkStartZ + 1];

		for (int x = chunkStartX; x <= chunkEndX; ++x) {
			for (int z = chunkStartZ; z <= chunkEndZ; ++z) {
				chunks[x - chunkStartX][z - chunkStartZ] = world.getChunk(x, z);
			}
		}

		boolean empty = true;

		// Start added code
		IS_EMPTY:
		// End added code
		for (int x = start.getX() >> 4; x <= end.getX() >> 4; ++x) {
			for (int z = start.getZ() >> 4; z <= end.getZ() >> 4; ++z) {
				Chunk chunk = chunks[x - chunkStartX][z - chunkStartZ];
				if (!chunk.isEmptyBetween(start.getY(), end.getY())) {
					empty = false;
					// Start added code
					break IS_EMPTY;
					// End added code
				}
			}
		}

		if (empty) {
			return null;
		} else {
			// Start removed code
//            int lvt_10_3_ = true;
			// End removed code
			BlockPos startAndPadding = start.add(-1, -1, -1);
			BlockPos endAndPadding = end.add(1, 1, 1);
			return new ChunkRenderCache(world, chunkStartX, chunkStartZ, chunks, startAndPadding, endAndPadding);
		}
	}

	protected final int getIndex(BlockPos pos) {
		return this.getIndex(pos.getX(), pos.getY(), pos.getZ());
	}

	protected int getIndex(int xIn, int yIn, int zIn) {
		int x = xIn - this.cacheStartPos.getX();
		int y = yIn - this.cacheStartPos.getY();
		int z = zIn - this.cacheStartPos.getZ();
		return z * this.cacheSizeX * this.cacheSizeY + y * this.cacheSizeX + x;
	}

	public Biome getBiome(BlockPos pos) {
		int x = (pos.getX() >> 4) - this.chunkStartX;
		int z = (pos.getZ() >> 4) - this.chunkStartZ;
		return this.chunks[x][z].getBiome(pos);
	}

	public int getLightFor(LightType type, BlockPos pos) {
		return this.world.getLightFor(type, pos);
	}

	@Nullable
	public TileEntity getTileEntity(BlockPos pos) {
		return this.getTileEntity(pos, CreateEntityType.IMMEDIATE);
	}

	public BlockState getBlockState(BlockPos pos) {
		return this.blockStates[this.getIndex(pos)];
	}

	public IFluidState getFluidState(BlockPos pos) {
		return this.fluidStates[this.getIndex(pos)];
	}

	@Nullable
	public TileEntity getTileEntity(BlockPos pos, CreateEntityType type) {
		int x = (pos.getX() >> 4) - this.chunkStartX;
		int z = (pos.getZ() >> 4) - this.chunkStartZ;
		return this.chunks[x][z].getTileEntity(pos, type);
	}

}
