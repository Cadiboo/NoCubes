package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.command.AddTerrainSmoothableCommand;
import io.github.cadiboo.nocubes.command.LogTerrainSmoothableCommand;
import io.github.cadiboo.nocubes.command.RemoveTerrainSmoothableCommand;
import io.github.cadiboo.nocubes.command.SetExtendFluidsRangeCommand;
import io.github.cadiboo.nocubes.command.SetTerrainCollisionsCommand;
import io.github.cadiboo.nocubes.command.SetTerrainMeshGeneratorCommand;
import net.minecraft.command.Commands;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * @author Cadiboo
 */
@EventBusSubscriber(modid = MOD_ID)
public final class ForgeEventSubscriber {

	@SubscribeEvent
	public static void onFMLServerStartingEvent(final FMLServerStartingEvent event) {
		event.getCommandDispatcher().register(Commands.literal(MOD_ID)
				.then(AddTerrainSmoothableCommand.register())
				.then(RemoveTerrainSmoothableCommand.register())
				.then(SetExtendFluidsRangeCommand.register())
				.then(SetTerrainCollisionsCommand.register())
				.then(SetTerrainMeshGeneratorCommand.register())
				.then(LogTerrainSmoothableCommand.register())
		);
	}

}
