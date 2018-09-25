package cadiboo.nocubes.client.renderer.chunk;

import cadiboo.nocubes.client.renderer.ModRenderGlobal;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModListedRenderChunk extends ModRenderChunk {
	private final int baseDisplayList = GLAllocation.generateDisplayLists(BlockRenderLayer.values().length);

	public ModListedRenderChunk(final World worldIn, final ModRenderGlobal renderGlobalIn, final int index) {
		super(worldIn, renderGlobalIn, index);
	}

	public int getDisplayList(final BlockRenderLayer layer, final ModCompiledChunk p_178600_2_) {
		return !p_178600_2_.isLayerEmpty(layer) ? this.baseDisplayList + layer.ordinal() : -1;
	}

	@Override
	public void deleteGlResources() {
		super.deleteGlResources();
		GLAllocation.deleteDisplayLists(this.baseDisplayList, BlockRenderLayer.values().length);
	}
}