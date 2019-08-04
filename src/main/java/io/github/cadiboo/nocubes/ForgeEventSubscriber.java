package io.github.cadiboo.nocubes;

import com.mojang.brigadier.CommandDispatcher;
import io.github.cadiboo.nocubes.command.AddTerrainSmoothableCommand;
import io.github.cadiboo.nocubes.command.RemoveTerrainSmoothableCommand;
import io.github.cadiboo.nocubes.command.SetExtendFluidsRangeCommand;
import io.github.cadiboo.nocubes.command.SetTerrainCollisionsCommand;
import io.github.cadiboo.nocubes.command.SetTerrainMeshGeneratorCommand;
import net.minecraft.command.CommandSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * @author Cadiboo
 */
@EventBusSubscriber(modid = NoCubes.MOD_ID)
public final class ForgeEventSubscriber {

	@SubscribeEvent
	public static void onFMLServerStartingEvent(final FMLServerStartingEvent event) {
		final CommandDispatcher<CommandSource> commandDispatcher = event.getCommandDispatcher();
		AddTerrainSmoothableCommand.register(commandDispatcher);
		RemoveTerrainSmoothableCommand.register(commandDispatcher);
		SetExtendFluidsRangeCommand.register(commandDispatcher);
		SetTerrainCollisionsCommand.register(commandDispatcher);
		SetTerrainMeshGeneratorCommand.register(commandDispatcher);
	}

}
