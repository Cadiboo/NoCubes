package io.github.cadiboo.renderchunkrebuildchunkhooks;

import io.github.cadiboo.renderchunkrebuildchunkhooks.util.Refs;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Cadiboo
 */
//@Mod(Refs.MOD_ID)
public class RenderChunkRebuildChunkHooks {

	private static final Logger LOGGER = LogManager.getLogger(Refs.MOD_ID);

	public RenderChunkRebuildChunkHooks() {
		LOGGER.info("Pre-loading RenderChunk");
		RenderChunk.class.getName();
		LOGGER.info("Successfully Pre-loaded RenderChunk");
		LOGGER.info("Initialising RenderChunk");
		final int unused = RenderChunk.renderChunksUpdated;
		LOGGER.info("Successfully initialised RenderChunk");
	}

}
