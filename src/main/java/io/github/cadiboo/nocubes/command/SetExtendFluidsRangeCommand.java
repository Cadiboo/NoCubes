package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.network.S2CSetExtendFluidsRange;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.fml.network.PacketDistributor;

import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

public class SetExtendFluidsRangeCommand {

	public static LiteralArgumentBuilder<CommandSource> register() {
		LiteralArgumentBuilder<CommandSource> setExtendFluidsRange = Commands.literal("setExtendFluidsRange")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL));
		for (int i = 0; i < ExtendFluidsRange.VALUES_LENGTH; i++) {
			final ExtendFluidsRange range = ExtendFluidsRange.VALUES[i];
			setExtendFluidsRange.then(Commands.literal(range.name())
					.executes(ctx -> set(range))
			);
		}
		return setExtendFluidsRange;
	}

	private static int set(final ExtendFluidsRange newRange) {
		ConfigHelper.setExtendFluidsRange(newRange);
		NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetExtendFluidsRange(newRange));
		return Command.SINGLE_SUCCESS;
	}

}
