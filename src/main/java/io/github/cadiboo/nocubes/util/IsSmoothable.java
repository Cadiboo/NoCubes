package io.github.cadiboo.nocubes.util;

import net.minecraft.block.state.IBlockState;

import java.util.function.Predicate;

/**
 * Removes boxing cost of using generic functions with Boolean
 *
 * @author Cadiboo
 */
public interface IsSmoothable extends Predicate<IBlockState> {

	IsSmoothable TERRAIN = new IsSmoothable() {
		@Override
		public String name() {
			return "terrain";
		}

		@Override
		public boolean test(IBlockState state) {
			return ((INoCubesBlockState) state).nocubes_isTerrainSmoothable();
		}

		@Override
		public void set(IBlockState state, boolean smoothable) {
			((INoCubesBlockState) state).nocubes_setTerrainSmoothable(smoothable);
		}
	};
	IsSmoothable LEAVES = new IsSmoothable() {
		@Override
		public String name() {
			return "leaves";
		}

		@Override
		public boolean test(IBlockState state) {
			return ((INoCubesBlockState) state).nocubes_isLeavesSmoothable();
		}

		@Override
		public void set(IBlockState state, boolean smoothable) {
			((INoCubesBlockState) state).nocubes_setLeavesSmoothable(smoothable);
		}
	};

	String name();

	/**
	 * @param state the state to be tested
	 * @return If the state should be smoothed
	 */
	@Override
	boolean test(IBlockState state);

	void set(IBlockState state, boolean smoothable);

}
