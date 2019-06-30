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

@OnlyIn(Dist.CLIENT)
public class ChunkRenderCache implements IEnviromentBlockReader {

	public final int chunkStartX;
	public final int chunkStartZ;
	public final BlockPos cacheStartPos;
	public /*final*/ int cacheSizeX;
	public /*final*/ int cacheSizeY;
	public /*final*/ int cacheSizeZ;
	public final Chunk[][] chunks;
	public /*final*/ BlockState[] blockStates;
	public /*final*/ IFluidState[] fluidStates;
	public final World world;

	public ChunkRenderCache(World world, int chunkStartX, int chunkStartZ, Chunk[][] chunks, BlockPos start, BlockPos end) {
		this.world = world;
		this.chunkStartX = chunkStartX;
		this.chunkStartZ = chunkStartZ;
		this.chunks = chunks;
		this.cacheStartPos = start;
		// NoCubes Start
		io.github.cadiboo.nocubes.hooks.Hooks.initChunkRenderCache(this, chunkStartX, chunkStartZ, chunks, start, end);
		// NoCubes End
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

		// NoCubes Start
		IS_EMPTY:
		// NoCubes End
		for (int x = start.getX() >> 4; x <= end.getX() >> 4; ++x) {
			for (int z = start.getZ() >> 4; z <= end.getZ() >> 4; ++z) {
				Chunk chunk = chunks[x - chunkStartX][z - chunkStartZ];
				if (!chunk.isEmptyBetween(start.getY(), end.getY())) {
					empty = false;
					// NoCubes Start
					break IS_EMPTY;
					// NoCubes End
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
