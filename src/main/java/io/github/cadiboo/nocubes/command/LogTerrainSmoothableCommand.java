package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

public class LogTerrainSmoothableCommand {

	// Yes, I realise how stupid this cooldown is. I also hate log spam though.
	private static long lastUseTime = 0;

	public static LiteralArgumentBuilder<CommandSource> register() {
		return Commands.literal("logTerrainSmoothable")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL))
				.executes(ctx -> {
					final long cooldown = lastUseTime - System.currentTimeMillis() + 10_000;
					if (cooldown > 0) {
						ctx.getSource().sendErrorMessage(new TranslationTextComponent(MOD_ID + ".waitSeconds", (int) Math.ceil(cooldown / 1000D)));
						return 0;
					}
					lastUseTime = System.currentTimeMillis();
					NoCubes.LOGGER.info("Terrain Smoothable:");
					NoCubes.LOGGER.info(Strings.join(Config.terrainSmoothable, ", "));
					ctx.getSource().sendFeedback(new TranslationTextComponent("gui.done"), true);
					return Command.SINGLE_SUCCESS;
				});
	}

}
