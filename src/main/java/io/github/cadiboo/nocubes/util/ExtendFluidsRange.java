package io.github.cadiboo.nocubes.util;

/**
 * @author Cadiboo
 */
public enum ExtendFluidsRange {
	OneBlock(1), TwoBlocks(2);

	private final int range;

	ExtendFluidsRange(final int range) {
		this.range = range;
	}

	public int getRange() {
		return range;
	}

}
