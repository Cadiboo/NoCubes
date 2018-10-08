package cadiboo.nocubes.client.render;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ModChunkRenderWorker extends ChunkRenderWorker {


	public static final Logger LOGGER = LogManager.getLogger();
	public final ModChunkRenderDispatcher chunkRenderDispatcher;
	public final RegionRenderCacheBuilder regionRenderCacheBuilder;


	public ModChunkRenderWorker(final ModChunkRenderDispatcher chunkRenderDispatcherIn)
	{
		this(chunkRenderDispatcherIn, (RegionRenderCacheBuilder)null);
	}

	public ModChunkRenderWorker(final ModChunkRenderDispatcher chunkRenderDispatcherIn, @Nullable final RegionRenderCacheBuilder regionRenderCacheBuilderIn)
	{
		super(chunkRenderDispatcherIn, regionRenderCacheBuilderIn);
		this.chunkRenderDispatcher = chunkRenderDispatcherIn;
		this.regionRenderCacheBuilder = regionRenderCacheBuilderIn;
	}


	@Override
	public void run() {
		while ((boolean) ReflectionHelper.getPrivateValue(ChunkRenderWorker.class, (ChunkRenderWorker)this, "shouldRun"))
		{
			try
			{
				this.processTask(this.chunkRenderDispatcher.getNextChunkUpdate());
			}
			catch (final InterruptedException var3)
			{
				LOGGER.debug("Stopping chunk worker due to interrupt");
				return;
			}
			catch (final Throwable throwable)
			{
				final CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Batching chunks");
				Minecraft.getMinecraft().crashed(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
				return;
			}
		}
	}
}
