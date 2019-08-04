package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

public class SetTerrainCollisionsCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("setTerrainCollisions")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL))
				.then(Commands.argument("enabled", BoolArgumentType.bool())
						.executes(SetTerrainCollisionsCommand::set)
				)
		);
	}

	public static int set(final CommandContext<CommandSource> ctx) {
		final boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
		// Config saving is async so set it now
		Config.terrainCollisions = enabled;
		ConfigHelper.setTerrainCollisions(enabled);
		ctx.getSource().sendFeedback(new TranslationTextComponent(MOD_ID + ".terrainCollisions" + (enabled ? "Enabled" : "Disabled")), true);
		return Command.SINGLE_SUCCESS;
	}

}
