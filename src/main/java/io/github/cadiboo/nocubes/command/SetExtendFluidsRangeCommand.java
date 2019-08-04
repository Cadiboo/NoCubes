package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

public class SetExtendFluidsRangeCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> setExtendFluidsRange = Commands.literal("setExtendFluidsRange")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL));
		for (int i = 0; i < ExtendFluidsRange.VALUES_LENGTH; i++) {
			final ExtendFluidsRange range = ExtendFluidsRange.VALUES[i];
			setExtendFluidsRange.then(Commands.literal(range.name())
					.executes(ctx -> set(ctx, range))
			);
		}
		dispatcher.register(setExtendFluidsRange);
	}

	private static int set(final CommandContext<CommandSource> ctx, final ExtendFluidsRange newRange) {
		// Config saving is async so set it now
		Config.extendFluidsRange = newRange;
		ConfigHelper.setExtendFluidsRange(newRange);
		ctx.getSource().sendFeedback(new TranslationTextComponent(MOD_ID + ".setExtendFluidsRange", newRange), true);
		return Command.SINGLE_SUCCESS;
	}

}
