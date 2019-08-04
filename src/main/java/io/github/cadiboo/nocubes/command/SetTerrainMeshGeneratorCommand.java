package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

public class SetTerrainMeshGeneratorCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> setExtendFluidsRange = Commands.literal("setTerrainMeshGenerator")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL));
		for (int i = 0; i < MeshGeneratorType.VALUES_LENGTH; i++) {
			final MeshGeneratorType type = MeshGeneratorType.VALUES[i];
			setExtendFluidsRange.then(Commands.literal(type.name())
					.executes(ctx -> set(ctx, type))
			);
		}
		dispatcher.register(setExtendFluidsRange);
	}

	private static int set(final CommandContext<CommandSource> ctx, final MeshGeneratorType newGenerator) {
		// Config saving is async so set it now
		Config.terrainMeshGenerator = newGenerator;
		ConfigHelper.setTerrainMeshGenerator(newGenerator);
		ctx.getSource().sendFeedback(new TranslationTextComponent(MOD_ID + ".setTerrainMeshGenerator", newGenerator), true);
		return Command.SINGLE_SUCCESS;
	}

}
