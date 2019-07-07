package io.github.cadiboo.nocubes.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.BlockRenderLayer;

import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES_LENGTH;

/**
 * @author Cadiboo
 */
public final class BufferBuilderCache {

	protected final BufferBuilder[] worldRenderers = new BufferBuilder[BLOCK_RENDER_LAYER_VALUES_LENGTH];

	public BufferBuilderCache() {
		this(0x200_000, 0x20_000, 0x20_000, 0x40_000);
	}

	public BufferBuilderCache(final int solidSize, final int cutoutSize, final int cutoutMippedSize, final int translucentSize) {
		this.worldRenderers[BlockRenderLayer.SOLID.ordinal()] = new BufferBuilder(solidSize);
		this.worldRenderers[BlockRenderLayer.CUTOUT.ordinal()] = new BufferBuilder(cutoutSize);
		this.worldRenderers[BlockRenderLayer.CUTOUT_MIPPED.ordinal()] = new BufferBuilder(cutoutMippedSize);
		this.worldRenderers[BlockRenderLayer.TRANSLUCENT.ordinal()] = new BufferBuilder(translucentSize);
	}

	public BufferBuilder get(BlockRenderLayer layer) {
		return get(layer.ordinal());
	}

	public BufferBuilder get(int id) {
		return this.worldRenderers[id];
	}

}
