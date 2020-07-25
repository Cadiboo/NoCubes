package io.github.cadiboo.nocubes.smoothable;

import net.minecraft.block.BlockState;

/**
 * @author Cadiboo
 */
public interface ServerSmoothableChangeHandler extends SmoothableChangeHandler {

	/**
	 * From the minecraft wiki.
	 * 1. Ops can bypass spawn protection.
	 * 2. Ops can use /clear, /difficulty, /effect, /gamemode, /gamerule, /give, /summon, /setblock and /tp, and can edit command blocks.
	 * 3. Ops can use /ban, /deop, /whitelist, /kick, and /op.
	 * 4. Ops can use /stop.
	 */
	int REQUIRED_PERMISSION_LEVEL = 2;

}
