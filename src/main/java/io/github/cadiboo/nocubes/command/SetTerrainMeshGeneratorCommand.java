package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.network.S2CSetTerrainMeshGenerator;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.fml.network.PacketDistributor;

import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

public class SetTerrainMeshGeneratorCommand {

	public static LiteralArgumentBuilder<CommandSource> register() {
		LiteralArgumentBuilder<CommandSource> setExtendFluidsRange = Commands.literal("setTerrainMeshGenerator")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL));
		for (int i = 0; i < MeshGeneratorType.VALUES_LENGTH; i++) {
			final MeshGeneratorType type = MeshGeneratorType.VALUES[i];
			setExtendFluidsRange.then(Commands.literal(type.name())
					.executes(ctx -> set(type))
			);
		}
		return setExtendFluidsRange;
	}

	private static int set(final MeshGeneratorType newGenerator) {
		// Config saving is async so set it now
		Config.terrainMeshGenerator = newGenerator;
		ConfigHelper.setTerrainMeshGenerator(newGenerator);
		NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetTerrainMeshGenerator(newGenerator));
		return Command.SINGLE_SUCCESS;
	}

}
