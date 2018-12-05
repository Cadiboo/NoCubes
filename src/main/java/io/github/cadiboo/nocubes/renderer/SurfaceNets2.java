package io.github.cadiboo.nocubes.renderer;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.util.math.BlockPos;

public class SurfaceNets2 {

	public static void renderPre(final RebuildChunkPreEvent event) {
	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {
		SurfaceNets.renderLayer(event);
	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {
		SurfaceNets.renderType(event);
	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {
		final BlockPos pos = event.getBlockPos();
		final BlockPos renderChunkPos = event.getRenderChunkPosition();

		if (pos.equals(renderChunkPos)) {  //first block to be rendered

		} else if (pos.equals(renderChunkPos.add(15, 15, 15))) { //last block to be rendered

		} else {  //any block in between

		}
	}

	public static void renderPost(final RebuildChunkPostEvent event) {
	}

}
