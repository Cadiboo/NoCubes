package cadiboo.nocubes.config;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import cadiboo.nocubes.util.ModEnums.RenderAlgorithm;
import cadiboo.nocubes.util.ModReference;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = ModReference.MOD_ID)
@Config.LangKey(ModReference.MOD_ID + ".config.title")
public class ModConfig {

	@Name("Active Rendering Algorithms")
	@Comment({ "A list of all Active Rendering Algorithms, separated by a \", \".", "Valid Algorithms are: " + "VANILLA, VANILLA_MODDED, FRAGMENTED, FACING, MARCHING_CUBES, SURFACE_NETS, SURFACE_NETS_OOP" })
	public static String activeRenderingAlgorithms = RenderAlgorithm.SURFACE_NETS.name();

	public static Set<RenderAlgorithm> getActiveRenderingAlgorithms() {
		return Arrays.asList(activeRenderingAlgorithms.split(", ")).stream().map(RenderAlgorithm::valueOf).collect(Collectors.toSet());

	}

	@Name("Facing faces")
	@Comment("Faces of each block to render in Facing rendering algorithm")
	public static String facingFacings = String.join(", ", Arrays.asList(EnumFacing.values()).stream().map(EnumFacing::name).collect(Collectors.toList()));

	public static Set<EnumFacing> getFacingFacings() {
		return Arrays.asList(facingFacings.split(", ")).stream().map(EnumFacing::valueOf).collect(Collectors.toSet());
	}

	@Name("Framgent Range")
	@Comment("Range to fragment blocks over in Fragment rendering algorithm")
	@RangeInt(min = 0, max = 64)
	public static int fragmentRange = 16;

	public static int getFragmentRange() {
		return fragmentRange;
	}

	@Name("Fragment faces")
	@Comment("Faces of each block to render in Fragment rendering algorithm")
	public static String fragmentFacings = String.join(", ", Arrays.asList(EnumFacing.values()).stream().map(EnumFacing::name).collect(Collectors.toList()));

	public static Set<EnumFacing> getFragmentFacings() {
		return Arrays.asList(fragmentFacings.split(", ")).stream().map(EnumFacing::valueOf).collect(Collectors.toSet());
	}

	@Name("Enabled")
	@Comment("If the mod is enabled")
	public static boolean isEnabled = true;

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
