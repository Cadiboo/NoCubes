package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientProxy;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.server.ServerProxy;
import io.github.cadiboo.nocubes.util.IProxy;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.renderchunkrebuildchunkhooks.RenderChunkRebuildChunkHooks;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBTBase;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;

@Mod(MOD_ID)
public final class NoCubes {

	public static final Logger NO_CUBES_LOG = LogManager.getLogger(MOD_ID);

	private static final Logger LOGGER = LogManager.getLogger();

	private static final ArrayList<ModProfiler> PROFILERS = new ArrayList<>();

	private static final ThreadLocal<ModProfiler> PROFILER = ThreadLocal.withInitial(() -> {
		final ModProfiler profiler = new ModProfiler();
		PROFILERS.add(profiler);
		return profiler;
	});

//	@Mod.Instance(MOD_ID)
//	public static NoCubes instance;

	public static IProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());

	public NoCubes() {

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		RenderChunkRebuildChunkHooks.HookConfig.enableRebuildChunkPreRenderEvent();
		RenderChunkRebuildChunkHooks.HookConfig.enableRebuildChunkBlockEvent();

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, NoCubesConfig.CLIENT_CONFIG);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, NoCubesConfig.SERVER_CONFIG);



		NoCubesConfig.loadConfig(NoCubesConfig.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("mymod-client.toml"));
		NoCubesConfig.loadConfig(NoCubesConfig.SERVER_CONFIG, FMLPaths.CONFIGDIR.get().resolve("mymod-server.toml"));

		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> GuiHandler::getClientGuiElement);

	}

	public static boolean isEnabled() {
		return ModConfig.isEnabled;
	}

	public static ModProfiler getProfiler() {
		return PROFILER.get();
	}

	public void setup(final FMLCommonSetupEvent event) {
		//ModLoadingContext.get().getActiveContainer()
		ModUtil.launchUpdateDaemon(ModList.get().getModContainerById(MOD_ID).get());
	}

}
