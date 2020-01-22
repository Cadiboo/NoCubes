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

/**
 * Command to enable/disable TerrainCollisions.
 *
 * @author Cadiboo
 */
public class SetTerrainCollisionsCommand {

	/**
	 * Syntax is "setTerrainCollisions <true/false>"
	 */
	public static LiteralArgumentBuilder<CommandSource> register() {
		return Commands.literal("setTerrainCollisions")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL))
				.then(Commands.argument("enabled", BoolArgumentType.bool())
						.executes(SetTerrainCollisionsCommand::set)
				);
	}

	/**
	 * Called on the Server.
	 * Sets TerrainCollisions to the new value
	 * and sends a packet to all clients to update their TerrainCollisions.
	 *
	 * @return The amount of successes the command had
	 */
	public static int set(final CommandContext<CommandSource> ctx) {
		final boolean newEnabled = BoolArgumentType.getBool(ctx, "enabled");
		ConfigHelper.setTerrainCollisions(newEnabled);
		NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetTerrainCollisions(newEnabled));
		return Command.SINGLE_SUCCESS;
	}

}
