package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.client.render.RenderDispatcher;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static io.github.cadiboo.nocubes.util.IsSmoothable.LEAVES;
import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN;

/**
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess") // Hooks are called with ASM
public final class Hooks {

	/**
	 * Called from: RenderChunk#rebuildChunk right before the BlockPos.getAllInBoxMutable iteration
	 * Calls: RenderDispatcher.renderChunk to render all our fluids and smooth terrain
	 */
	@SideOnly(Side.CLIENT)
	public static void preIteration(final RenderChunk renderChunk, final float x, final float y, final float z, final ChunkCompileTaskGenerator generator, final CompiledChunk compiledchunk, final BlockPos blockpos, final BlockPos blockpos1, final World world, final IBlockAccess lvt_10_1_, final VisGraph lvt_11_1_, final HashSet lvt_12_1_, final boolean[] aboolean, final BlockRendererDispatcher blockrendererdispatcher) {
		RenderDispatcher.renderChunk(renderChunk, blockpos, generator, compiledchunk, world, lvt_10_1_, aboolean, new Random(), blockrendererdispatcher);
	}

	/**
	 * Called from: RenderGlobal#drawBlockDamageTexture before BlockRendererDispatcher#renderBlockDamage is called
	 * Calls: RenderDispatcher.renderSmoothBlockDamage if the blockstate is smoothable
	 *
	 * @return If normal rendering should be cancelled (i.e. normal rendering should NOT happen)
	 */
	@SideOnly(Side.CLIENT)
	public static boolean renderBlockDamage(final Tessellator tessellatorIn, final BufferBuilder bufferBuilderIn, final BlockPos blockpos, final IBlockState iblockstate, final WorldClient world, final TextureAtlasSprite textureatlassprite, final BlockRendererDispatcher blockrendererdispatcher) {
		if (!Config.renderSmoothTerrain || !TERRAIN.test(iblockstate)) {
			if (!Config.renderSmoothLeaves || !LEAVES.test(iblockstate)) {
				return true;
			}
		}
		RenderDispatcher.renderSmoothBlockDamage(tessellatorIn, bufferBuilderIn, blockpos, iblockstate, world, textureatlassprite);
		return false;
	}

	/**
	 * Called from: World#getCollisionBoxes(Entity, AxisAlignedBB, boolean, List) instead of Blocks.STONE being gotten
	 * Calls: CollisionHandler.getCollisionBoxes to handle mesh, repose and vanilla collisions
	 *
	 * @return If any box intersects (i.e. resultList is not empty)
	 */
	public static boolean getCollisionBoxes(final World _this, final Entity entityIn, final AxisAlignedBB aabb, final boolean p_191504_3_, final List<AxisAlignedBB> outList, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean flag, final boolean flag1) {
		return CollisionHandler.getCollisionBoxes(_this, entityIn, aabb, p_191504_3_, outList, i, j, k, l, i1, j1, worldborder, flag, flag1);
	}

	/**
	 * Called from: RenderChunk#rebuildChunk right before IBlockState#getRenderType is called
	 * Calls: Nothing
	 * Disables vanilla rendering for smoothable IBlockStates
	 *
	 * @return If the state can render
	 */
	@SideOnly(Side.CLIENT)
	public static boolean canBlockStateRender(final IBlockState blockstate) {
		if (TERRAIN.test(blockstate) && Config.renderSmoothTerrain) return false;
		if (LEAVES.test(blockstate)) {
			if (Config.renderSmoothLeaves)
				return Config.renderSmoothAndVanillaLeaves;
			return true;
		}
		return true;
	}

}
