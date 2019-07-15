package io.github.cadiboo.nocubes.util;

/**
 * @author Cadiboo
 */
public enum ExtendFluidsRange {

	Off(0),
	OneBlock(1),
	TwoBlocks(2);

	public static final ExtendFluidsRange[] VALUES = values();

	private final int range;

	ExtendFluidsRange(final int range) {
		this.range = range;
	}

	public int getRange() {
		return range;
	}

}
