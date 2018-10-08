package cadiboo.nocubes.client.renderer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModRegionRenderCacheBuilder extends RegionRenderCacheBuilder {
	public BufferBuilder[] worldRenderers = new BufferBuilder[BlockRenderLayer.values().length];

	public ModRegionRenderCacheBuilder() {
		this.worldRenderers[BlockRenderLayer.SOLID.ordinal()] = new BufferBuilder(2097152);
		this.worldRenderers[BlockRenderLayer.CUTOUT.ordinal()] = new BufferBuilder(131072);
		this.worldRenderers[BlockRenderLayer.CUTOUT_MIPPED.ordinal()] = new BufferBuilder(131072);
		this.worldRenderers[BlockRenderLayer.TRANSLUCENT.ordinal()] = new BufferBuilder(262144);
	}

	public ModRegionRenderCacheBuilder(final RegionRenderCacheBuilder regionRenderCacheBuilder) {
		this();
//		this.worldRenderers = ReflectionHelper.getPrivateValue(RegionRenderCacheBuilder.class, regionRenderCacheBuilder, "worldRenderers");
	}

	@Override
	public BufferBuilder getWorldRendererByLayer(final BlockRenderLayer layer) {
		return this.worldRenderers[layer.ordinal()];
	}

	@Override
	public BufferBuilder getWorldRendererByLayerId(final int id) {
		return this.worldRenderers[id];
	}
}