package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.network.S2CSetTerrainMeshGenerator;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.fml.network.PacketDistributor;

import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

/**
 * Command to set the TerrainMeshGenerator.
 *
 * @author Cadiboo
 */
public class SetTerrainMeshGeneratorCommand {

	/**
	 * Syntax is "setTerrainMeshGenerator <MeshGeneratorType>"
	 */
	public static LiteralArgumentBuilder<CommandSource> register() {
		LiteralArgumentBuilder<CommandSource> setExtendFluidsRange = Commands.literal("setTerrainMeshGenerator")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL));
		for (final MeshGeneratorType type : MeshGeneratorType.getValues())
			setExtendFluidsRange.then(Commands.literal(type.name())
					.executes(ctx -> set(type))
			);
		return setExtendFluidsRange;
	}

	/**
	 * Called on the Server.
	 * Sets the TerrainMeshGenerator to the new value
	 * and sends a packet to all clients to update their TerrainMeshGenerator.
	 *
	 * @return The amount of successes the command had
	 */
	private static int set(final MeshGeneratorType newGenerator) {
		ConfigHelper.setTerrainMeshGenerator(newGenerator);
		NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetTerrainMeshGenerator(newGenerator));
		return Command.SINGLE_SUCCESS;
	}

}
