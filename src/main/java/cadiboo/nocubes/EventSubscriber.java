package cadiboo.nocubes;

import cadiboo.nocubes.config.ModConfig;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

@Mod.EventBusSubscriber(Side.CLIENT)
public class EventSubscriber {

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {

		//		NoCubes.LOGGER.info("onRebuildChunkBlockEvent");

		if (! NoCubes.isEnabled()) {
			return;
		}

		event.setCanceled(true);

		ModConfig.activeRenderingAlgorithm.renderBlock(event);

	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockRenderInLayerEvent(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	@SubscribeEvent
	public static void onRegisterBlocksEvent(final RegistryEvent.Register<Block> event) {

		final BlockAir blockAir = (BlockAir) (new BlockAir() {

			@Override
			public BlockRenderLayer getRenderLayer() {

				return airRenderLayerHook();

			}

			@Override
			public EnumBlockRenderType getRenderType(IBlockState state) {

				return airRenderTypeHook(state);

			}
		})

			.setRegistryName(Blocks.AIR.getRegistryName()).setTranslationKey(Blocks.AIR.getTranslationKey());

		//		final Class<?> forgeRegistry = ReflectionHelper.getClass(Loader.instance().getModClassLoader(), "net.minecraftforge.registries.ForgeRegistry");

		final Class<?> namespacedDefaultedWrapper = ReflectionHelper.getClass(Loader.instance().getModClassLoader(), "net.minecraftforge.registries.NamespacedDefaultedWrapper");

		final IntIdentityHashBiMap<Block> underlyingIntegerMap = ReflectionHelper.getPrivateValue(RegistryNamespaced.class, Block.REGISTRY, "underlyingIntegerMap", "field_148759_a");
		final Map<Block, ResourceLocation> inverseObjectRegistry = ReflectionHelper.getPrivateValue(RegistryNamespaced.class, Block.REGISTRY, "inverseObjectRegistry", "field_148758_b");
		final Map<ResourceLocation, Block> registryObjects = ReflectionHelper.getPrivateValue(RegistrySimple.class, Block.REGISTRY, "registryObjects", "field_82596_a");

		final ModContainer oldContainer = Loader.instance().activeModContainer();
		Loader.instance().setActiveModContainer(Loader.instance().getMinecraftModContainer());

		underlyingIntegerMap.put(blockAir, 0);
		inverseObjectRegistry.put(blockAir, Blocks.AIR.getRegistryName());
		registryObjects.put(Blocks.AIR.getRegistryName(), blockAir);

		Loader.instance().setActiveModContainer(oldContainer);

		//		//unlock registry
		//		try {
		//			ReflectionHelper.findField(namespacedDefaultedWrapper, "locked").set(Block.REGISTRY, false);
		//		} catch (IllegalAccessException e) {
		//			throw new RuntimeException(e);
		//		}
		//
		//		//set default value to null for RegistryNamespacedDefaultedByKey
		//		try {
		//			//			ReflectionHelper.findField(forgeRegistry, "defaultValue").set(Block.REGISTRY, null);
		//			ReflectionHelper.findField(RegistryNamespacedDefaultedByKey.class, "defaultValue").set(Block.REGISTRY, null);
		//		} catch (IllegalAccessException e) {
		//			throw new RuntimeException(e);
		//		}
		//
		//		//set default value to null for delegate
		//		try {
		//
		//			final ForgeRegistry<?> delegate = (ForgeRegistry<?>) ReflectionHelper.findField(namespacedDefaultedWrapper, "delegate").get(Block.REGISTRY);
		//
		//			ReflectionHelper.findField(ForgeRegistry.class, "defaultValue").set(delegate, null);
		//		} catch (IllegalAccessException e) {
		//			throw new RuntimeException(e);
		//		}
		//
		//		final ModContainer oldContainer = Loader.instance().activeModContainer();
		//
		//		Loader.instance().setActiveModContainer(Loader.instance().getMinecraftModContainer());
		//
		//		Block.REGISTRY.register(0, Blocks.AIR.getRegistryName(), blockAir);
		//
		//		Loader.instance().setActiveModContainer(oldContainer);
		//
		//		//				event.getRegistry().register(blockAir);
		//
		//		//re-lock registry
		//		try {
		//			ReflectionHelper.findField(namespacedDefaultedWrapper, "locked").set(Block.REGISTRY, true);
		//		} catch (IllegalAccessException e) {
		//			throw new RuntimeException(e);
		//		}

	}

	private static EnumBlockRenderType airRenderTypeHook(IBlockState state) {

		return EnumBlockRenderType.MODEL;

	}

	private static BlockRenderLayer airRenderLayerHook() {

		return BlockRenderLayer.CUTOUT;

	}

}
