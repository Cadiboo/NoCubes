package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.network.S2CSetTerrainSmoothable;
import io.github.cadiboo.nocubes.util.StateHolder;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraftforge.fml.network.PacketDistributor;

import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

public class RemoveTerrainSmoothableCommand {

	public static LiteralArgumentBuilder<CommandSource> register() {
		return Commands.literal("removeTerrainSmoothable")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL))
				.then(Commands.argument("block", BlockStateArgument.blockState())
						.executes(RemoveTerrainSmoothableCommand::removeBlockState)
				);
	}

	private static int removeBlockState(final CommandContext<CommandSource> ctx) {
		final BlockState blockState = BlockStateArgument.getBlockState(ctx, "block").getState();
		if (blockState == StateHolder.AIR_DEFAULT) {
			NoCubes.LOGGER.error("Trying to remove invalid terrain smoothable blockstate: " + blockState);
			return 0;
		}
		final boolean newSmoothability = false;
		ConfigHelper.setTerrainSmoothable(blockState, newSmoothability);
		NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetTerrainSmoothable(blockState, newSmoothability));
		return Command.SINGLE_SUCCESS;
	}

}
