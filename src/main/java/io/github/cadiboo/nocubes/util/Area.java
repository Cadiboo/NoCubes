package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.Arrays;

public class Area implements AutoCloseable {

	private static final ThreadLocal<CachedArray> cache = ThreadLocal.withInitial(CachedArray::new);

	public final IWorldReader world;
	public final BlockPos start;
	public final BlockPos end;
	// Arrays are indexed [z][y][x] for cache locality
	private BlockState[] blocks;

	public Area(IWorldReader world, BlockPos start, BlockPos end) {
		this.world = world;
		this.start = start.immutable();
		this.end = end.immutable();
	}

	public BlockState[] getAndCacheBlocks() {
		if (blocks == null) {
			blocks = cache.get().takeArray(this.getLength());
			ModUtil.traverseArea(start, end.offset(-1, -1, -1), new BlockPos.Mutable(), world, (state, pos, zyxIndex) -> blocks[zyxIndex] = state);
		}
		return blocks;
	}

	public int getLength() {
		// I could do Math.abs but I don't think I'll ever have the params passed in reversed
		int depth = end.getZ() - start.getZ();
		int height = end.getY() - start.getY();
		int width = end.getX() - start.getX();
		return depth * height * width;
	}

	@Override
	public void close() {
		if (blocks != null)
			cache.get().releaseArray();
	}

	private static class CachedArray {

		private StackTraceElement[] trace;
		private BlockState[] array;

		public BlockState[] takeArray(int minLength) {
			if (trace != null)
				throw new IllegalStateException("Already owned by " + Arrays.toString(trace));
			trace = new Throwable().getStackTrace();
			if (array == null || array.length < minLength)
				array = new BlockState[minLength];
			return array;
		}

		public void releaseArray() {
			trace = null;
		}
	}

}
