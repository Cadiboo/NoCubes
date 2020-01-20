package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.network.S2CSetTerrainCollisions;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.fml.network.PacketDistributor;

import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

public class SetTerrainCollisionsCommand {

	public static LiteralArgumentBuilder<CommandSource> register() {
		return Commands.literal("setTerrainCollisions")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL))
				.then(Commands.argument("enabled", BoolArgumentType.bool())
						.executes(SetTerrainCollisionsCommand::set)
				);
	}

	public static int set(final CommandContext<CommandSource> ctx) {
		final boolean newEnabled = BoolArgumentType.getBool(ctx, "enabled");
		ConfigHelper.setTerrainCollisions(newEnabled);
		NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetTerrainCollisions(newEnabled));
		return Command.SINGLE_SUCCESS;
	}

}
