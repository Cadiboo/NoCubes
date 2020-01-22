package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

/**
 * Command to debug the Server's terrain smoothable whitelist and blacklist.
 * Can only be used every 10 seconds to prevent log spam.
 *
 * @author Cadiboo
 */
public class LogTerrainSmoothableCommand {

	private static long lastUseTime = 0;

	/**
	 * Syntax is "logTerrainSmoothable"
	 */
	public static LiteralArgumentBuilder<CommandSource> register() {
		return Commands.literal("logTerrainSmoothable")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL))
				.executes(LogTerrainSmoothableCommand::validateAndLog);
	}

	/**
	 * Called on the Server.
	 * Checks that it has been more than 10 seconds since the command was last used.
	 * If it hasn't sends the sender an error message.
	 * If it has, logs the white list then the black list to the server console.
	 *
	 * @return The amount of successes the command had
	 */
	private static int validateAndLog(CommandContext<CommandSource> ctx) {
		final long cooldown = lastUseTime - System.currentTimeMillis() + 10_000;
		if (cooldown > 0) {
			ctx.getSource().sendErrorMessage(new TranslationTextComponent(MOD_ID + ".waitSeconds", (int) Math.ceil(cooldown / 1000D)));
			return 0;
		}
		lastUseTime = System.currentTimeMillis();
		NoCubes.LOGGER.info("Terrain Smoothable White List:");
		NoCubes.LOGGER.info(String.join(", ", ConfigHelper.blockStatesToStrings(NoCubesConfig.Server.terrainSmoothableWhitelist)));
		NoCubes.LOGGER.info("Terrain Smoothable Black List:");
		NoCubes.LOGGER.info(String.join(", ", ConfigHelper.blockStatesToStrings(NoCubesConfig.Server.terrainSmoothableBlacklist)));
		ctx.getSource().sendFeedback(new TranslationTextComponent("gui.done"), true);
		return Command.SINGLE_SUCCESS;
	}

}
