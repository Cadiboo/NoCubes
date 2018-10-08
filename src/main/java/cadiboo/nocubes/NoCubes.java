package cadiboo.nocubes;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadiboo.nocubes.client.render.ModChunkRenderDispatcher;
import cadiboo.nocubes.client.render.ModChunkRenderWorker;
import cadiboo.nocubes.util.ModReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

//@SideOnly(Side.CLIENT)
@Mod(modid = ModReference.MOD_ID, name = ModReference.MOD_NAME, version = "0.0", clientSideOnly = true)
public class NoCubes {

	public static KeyBinding	KEYBIND_SETTINGS;
	public static KeyBinding	KEYBIND_DEBUG;

	@Instance(ModReference.MOD_ID)
	public static NoCubes		INSTANCE;

	public static Logger		LOGGER	= LogManager.getLogger(ModReference.MOD_ID);

	@EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		// TODO: remove this when I'm done - its only needed cause the old no-cubes also has an event subscriber & forge can't determine the owning mod for my event subscriber
		//		MinecraftForge.EVENT_BUS.register(new EventSubscriber());
	}

	@EventHandler
	public void init(final FMLInitializationEvent event) {
		//		try {
		//			Minecraft.getMinecraft().renderGlobal = new ModRenderGlobal(Minecraft.getMinecraft());
		//			LOGGER.info("Successfully replaced Minecraft's RenderGlobal");
		//		} catch (final Throwable throwable) {
		//			LOGGER.error("Failed to replace Minecraft's RenderGlobal");
		//			// This should only happen rarely (never honestly) - so printing the Stack Trace shoudn't spam any logs
		//			throwable.printStackTrace();
		//			// TODO: throw the throwable? Maybe, keep it commented out for now
		//			// throw throwable;
		//		}



	}

	@EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		try {
			final Minecraft minecraft =  Minecraft.getMinecraft();

			//getRenderGlobal
			final RenderGlobal renderGlobal = minecraft.renderGlobal;

			//getChunkRenderDispatcher
			final Field chunkRenderDispatcherField = ReflectionHelper.findField(RenderGlobal.class, "renderDispatcher");
			final ChunkRenderDispatcher chunkRenderDispatcher =(ChunkRenderDispatcher) chunkRenderDispatcherField.get(renderGlobal);

			//create Mod version of ChunkRenderDispatcher
			final ModChunkRenderDispatcher modChunkRenderDispatcher= new ModChunkRenderDispatcher(ReflectionHelper.getPrivateValue(ChunkRenderDispatcher.class, chunkRenderDispatcher, "countRenderBuilders"));


			//listThreadedWorkersField from ChunkRenderDispatcher
			final Field listThreadedWorkersField = ReflectionHelper.findField(ChunkRenderDispatcher.class, "listThreadedWorkers");
			final List<ChunkRenderWorker> listThreadedWorkers = (List<ChunkRenderWorker>) listThreadedWorkersField.get(chunkRenderDispatcher);


			//			ReflectionHelper.getPrivateValue(ChunkRenderDispatcher.class, chunkRenderDispatcher, );
			final Field renderWorkerField = ReflectionHelper.findField(ChunkRenderDispatcher.class, "renderWorker");
			//			final ChunkRenderWorker renderWorker = ReflectionHelper.getPrivateValue(ChunkRenderDispatcher.class, chunkRenderDispatcher, "renderWorker");

			final Iterator<ChunkRenderWorker> it = listThreadedWorkers.iterator();

			while(it.hasNext()) {
				it.remove();
			}

			final int size = listThreadedWorkers.size();

			for(int i=0; i<size; i++) {

				final RegionRenderCacheBuilder regionRenderCacheBuilder = ReflectionHelper.getPrivateValue(ChunkRenderWorker.class, listThreadedWorkers.get(i), "regionRenderCacheBuilder");

				listThreadedWorkers.set(i, new ModChunkRenderWorker(modChunkRenderDispatcher, regionRenderCacheBuilder));
			}

			//			renderGlobal->ChunkRenderDispatcher{listThreadedWorkers,renderWorker}->ChunkRenderWorker->


			LOGGER.info("Successfully added our hook into RebuildChunk");
		} catch (final Throwable throwable) {
			LOGGER.error("Failed to add our hook into RebuildChunk");
			// This should only happen rarely (never honestly) - so printing the Stack Trace shoudn't spam any logs
			throwable.printStackTrace();
			// TODO: throw the throwable? Maybe, keep it commented out for now
			// throw throwable;
		}
	}


	//	protected static void openSettingsGui() {
	//		Minecraft.getMinecraft().displayGuiScreen(new GuiNoCubes());
	//	}
	//
	//	public static boolean shouldSmooth(final Block block) {
	//		if (!ModConfig.MOD_ENABLED) {
	//			return false;
	//		}
	//		if (ModConfig.SMOOTHBLOCKS_IDS.contains(Block.getIdFromBlock(block))) {
	//			return true;
	//		}
	//		return false;
	//	}

}
