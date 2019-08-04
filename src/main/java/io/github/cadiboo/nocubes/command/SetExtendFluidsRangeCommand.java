package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.network.S2CSetExtendFluidsRange;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.fml.network.PacketDistributor;

import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

public class SetExtendFluidsRangeCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> setExtendFluidsRange = Commands.literal("setExtendFluidsRange")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL));
		for (int i = 0; i < ExtendFluidsRange.VALUES_LENGTH; i++) {
			final ExtendFluidsRange range = ExtendFluidsRange.VALUES[i];
			setExtendFluidsRange.then(Commands.literal(range.name())
					.executes(ctx -> set(range))
			);
		}
		dispatcher.register(setExtendFluidsRange);
	}

	private static int set(final ExtendFluidsRange newRange) {
		// Config saving is async so set it now
		Config.extendFluidsRange = newRange;
		ConfigHelper.setExtendFluidsRange(newRange);
		NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetExtendFluidsRange(newRange));
		return Command.SINGLE_SUCCESS;
	}

}
