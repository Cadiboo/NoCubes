package cadiboo.nocubes.config;

import cadiboo.nocubes.util.ModEnums.RenderAlgorithm;
import cadiboo.nocubes.util.ModEnums.RenderType;
import cadiboo.nocubes.util.ModReference;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = ModReference.MOD_ID)
@Config.LangKey(ModReference.MOD_ID + ".config.title")
public class ModConfig {

	@Name("Enabled")
	@Comment("If the mod is enabled")
	public static boolean isEnabled = true;

	@Name("Rendering Algorithm")
	@Comment({

			"Interactive examples: https://wordsandbuttons.online/interactive_explanation_of_marching_cubes_and_dual_contouring.html",

			"Marching Cubes - The original smoothing algorithm that has existed since 1987 - Fast, Well documented",

			"Surface Nets - A smoothing algorithm - Slightly slower, Dual contouring!",

	})
	public static RenderAlgorithm activeRenderingAlgorithm = RenderAlgorithm.SURFACE_NETS;

	@Name("Render Type")
	@Comment({

			"How the algorithm should render blocks",

			"All Blocks - Renders the whole chunk at once (does all computation at one time - stops everything else during that time)",

			"Single Block - Renders each block based on its neighbours (does computation spread out over time)",

	})
	public static RenderType renderType = RenderType.ALL_BLOCKS;

	@Name("Smooth Liquids")
	@Comment({

			"If liquids (lava and water) should be rendered extended into smoothable blocks"

	})
	public static boolean shouldSmoothLiquids = false;

	@Name("Draw Wireframe")
	@Comment({

			"If a wireframe should be drawn on non-air blocks"

	})
	public static boolean shouldDrawWireframe = false;

	@Name("Aproximate Lighting")
	@Comment({

			"If a lighting should be aproximated for blocks rendered with the bufferbuilder",

	})
	public static boolean shouldAproximateLighting = true;

	@Mod.EventBusSubscriber(modid = ModReference.MOD_ID)
	private static class EventSubscriber {

		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(ModReference.MOD_ID)) {
				ConfigManager.sync(ModReference.MOD_ID, Config.Type.INSTANCE);
			}
		}
	}

}
