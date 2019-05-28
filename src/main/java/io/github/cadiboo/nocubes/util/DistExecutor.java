package io.github.cadiboo.nocubes.util;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public final class DistExecutor {

	public static void runWhenOn(Side side, Supplier<Runnable> toRun) {
		if (side == FMLCommonHandler.instance().getSide()) {
			toRun.get().run();
		}
	}

}
