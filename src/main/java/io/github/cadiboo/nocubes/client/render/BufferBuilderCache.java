package io.github.cadiboo.nocubes.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.BlockRenderLayer;

/**
 * @author Cadiboo
 */
public class BufferBuilderCache {

	protected final BufferBuilder[] worldRenderers = new BufferBuilder[BlockRenderLayer.values().length];

	public BufferBuilderCache() {
		this(0x200000, 0x20000, 0x20000, 0x40000);
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
