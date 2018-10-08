package cadiboo.nocubes.client.renderer.chunk;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModCompiledChunk extends net.minecraft.client.renderer.chunk.CompiledChunk {
	public static final ModCompiledChunk DUMMY = new ModCompiledChunk() {
		@Override
		public void setLayerUsed(final BlockRenderLayer layer) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setLayerStarted(final BlockRenderLayer layer) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isVisible(final EnumFacing facing, final EnumFacing facing2) {
			return false;
		}
	};

	public final boolean[]			layersUsed		= new boolean[BlockRenderLayer.values().length];
	public final boolean[]			layersStarted	= new boolean[BlockRenderLayer.values().length];
	public boolean					empty			= true;
	public final List<TileEntity>	tileEntities	= Lists.<TileEntity>newArrayList();
	public SetVisibility			setVisibility	= new SetVisibility();
	public BufferBuilder.State		state;

	public ModCompiledChunk(final net.minecraft.client.renderer.chunk.CompiledChunk compiledChunkIn) {
		this();
		this.tileEntities.clear();
		this.tileEntities.addAll(compiledChunkIn.getTileEntities());
	}

	public ModCompiledChunk() {
	}

	@Override
	public boolean isEmpty() {
		return this.empty;
	}

	@Override
	public void setLayerUsed(final BlockRenderLayer layer) {
		this.empty = false;
		this.layersUsed[layer.ordinal()] = true;
	}

	@Override
	public boolean isLayerEmpty(final BlockRenderLayer layer) {
		return !this.layersUsed[layer.ordinal()];
	}

	@Override
	public void setLayerStarted(final BlockRenderLayer layer) {
		this.layersStarted[layer.ordinal()] = true;
	}

	@Override
	public boolean isLayerStarted(final BlockRenderLayer layer) {
		return this.layersStarted[layer.ordinal()];
	}

	@Override
	public List<TileEntity> getTileEntities() {
		return this.tileEntities;
	}

	@Override
	public void addTileEntity(final TileEntity tileEntityIn) {
		this.tileEntities.add(tileEntityIn);
	}

	@Override
	public boolean isVisible(final EnumFacing facing, final EnumFacing facing2) {
		return this.setVisibility.isVisible(facing, facing2);
	}

	@Override
	public void setVisibility(final SetVisibility visibility) {
		this.setVisibility = visibility;
	}

	@Override
	public BufferBuilder.State getState() {
		return this.state;
	}

	@Override
	public void setState(final BufferBuilder.State stateIn) {
		this.state = stateIn;
	}
}