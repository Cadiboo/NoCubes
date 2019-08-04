package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.network.S2CDisableTerrainCollisions;
import io.github.cadiboo.nocubes.network.S2CEnableTerrainCollisions;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.fml.network.PacketDistributor;

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
		final boolean newEnabled = BoolArgumentType.getBool(ctx, "enabled");
		// Config saving is async so set it now
		Config.terrainCollisions = newEnabled;
		ConfigHelper.setTerrainCollisions(newEnabled);
		NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), newEnabled ? new S2CEnableTerrainCollisions() : new S2CDisableTerrainCollisions());
		return Command.SINGLE_SUCCESS;
	}

}
