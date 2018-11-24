package cadiboo.nocubes.config;

import cadiboo.nocubes.util.ModEnums.RenderAlgorithm;
import cadiboo.nocubes.util.ModEnums.RenderType;
import cadiboo.nocubes.util.ModReference;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
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

	@Name(ModReference.MOD_ID + ".config.enabled.name")
	@Comment(ModReference.MOD_ID + ".config.enabled.comment")
	public static boolean isEnabled = true;

	@Name("nocubes.config.algorithm.name")
	@Comment({ "nocubes.config.algorithm.comment" })
	public static RenderAlgorithm activeRenderingAlgorithm = RenderAlgorithm.MARCHING_CUBES;

	@Name("nocubes.config.forcechunkreload.name")
	@Comment({ "nocubes.config.forcechunkreload.comment" })
	public static boolean shouldForceChunkReload = true;

	@Name("nocubes.config.fixcullfacing.name")
	@Comment({ "nocubes.config.fixcullfacing.comment" })
	public static boolean shouldFixCullFacing = true;

	@Name("nocubes.config.smoothliquids.name")
	@Comment({ "nocubes.config.smoothliquids.comment" })
	public static boolean shouldSmoothLiquids = false;

	@Name("nocubes.config.drawwireframe.name")
	@Comment({ "nocubes.config.drawwireframe.comment" })
	public static boolean shouldDrawWireframe = false;

	@Name("nocubes.config.approximatelighting.name")
	@Comment({ "nocubes.config.approximatelighting.comment" })
	public static boolean shouldAproximateLighting = true;

	@Name("nocubes.config.smoothableblockstates.name")
	@Comment({ "nocubes.config.smoothableblockstates.comment" })
	public static String[] smoothableBlockStates = new String[] {

		Blocks.GRASS.getDefaultState().withProperty(BlockGrass.SNOWY, false).toString(), Blocks.GRASS.getDefaultState().withProperty(BlockGrass.SNOWY, true).toString(),

		Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE).toString(), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE).toString(), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE_SMOOTH).toString(), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE).toString(),
		Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH).toString(), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE).toString(), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE_SMOOTH).toString(),

		Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, net.minecraft.block.BlockSand.EnumType.SAND).toString(), Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, net.minecraft.block.BlockSand.EnumType.RED_SAND).toString(),

		Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, net.minecraft.block.BlockSandStone.EnumType.DEFAULT).toString(), Blocks.RED_SANDSTONE.getDefaultState().withProperty(BlockRedSandstone.TYPE, net.minecraft.block.BlockRedSandstone.EnumType.DEFAULT).toString(),

		Blocks.GRAVEL.getDefaultState().toString(),

		Blocks.COAL_ORE.getDefaultState().toString(), Blocks.IRON_ORE.getDefaultState().toString(), Blocks.GOLD_ORE.getDefaultState().toString(), Blocks.REDSTONE_ORE.getDefaultState().toString(), Blocks.LIT_REDSTONE_ORE.getDefaultState().toString(), Blocks.DIAMOND_ORE.getDefaultState().toString(), Blocks.EMERALD_ORE.getDefaultState().toString(), Blocks.QUARTZ_ORE.getDefaultState().toString(),

		Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.STONE).toString(), Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.COBBLESTONE).toString(), Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.STONEBRICK).toString(),
		Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.MOSSY_STONEBRICK).toString(), Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.CRACKED_STONEBRICK).toString(), Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.CHISELED_STONEBRICK).toString(),

		Blocks.GRASS_PATH.getDefaultState().toString(), Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT).toString(), Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT).toString(), Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL).toString(),

		Blocks.CLAY.getDefaultState().toString(), Blocks.HARDENED_CLAY.getDefaultState().toString(),

		Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.WHITE).toString(), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.ORANGE).toString(), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.MAGENTA).toString(),
		Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.LIGHT_BLUE).toString(), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.YELLOW).toString(), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.LIME).toString(),
		Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.PINK).toString(), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.GRAY).toString(), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.SILVER).toString(),
		Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.CYAN).toString(), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.PURPLE).toString(), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.BLUE).toString(),
		Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.BROWN).toString(), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.GREEN).toString(), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.RED).toString(),
		Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.BLACK).toString(),

		Blocks.SNOW.getDefaultState().toString(),

		Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 1).toString(), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 2).toString(), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 3).toString(), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 4).toString(), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 5).toString(), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 6).toString(),
		Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 7).toString(), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 8).toString(),

		Blocks.BEDROCK.getDefaultState().toString(),

		Blocks.NETHERRACK.getDefaultState().toString(), Blocks.GLOWSTONE.getDefaultState().toString(),

		Blocks.END_STONE.getDefaultState().toString(),

		Blocks.MYCELIUM.getDefaultState().withProperty(BlockMycelium.SNOWY, false).toString(), Blocks.MYCELIUM.getDefaultState().withProperty(BlockMycelium.SNOWY, true).toString(),

	};

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
