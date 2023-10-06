package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientEventSubscriber;
import io.github.cadiboo.nocubes.client.render.RenderDispatcher;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
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
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

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
		if (!NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(iblockstate)) {
			return true;
		}
		RenderDispatcher.renderBreakingTexture(tessellatorIn, bufferBuilderIn, blockpos, iblockstate, world, textureatlassprite);
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
		return !NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(blockstate);
	}

	/**
	 * Called from: {@link net.minecraftforge.fml.common.network.internal.FMLNetworkHandler#checkModList(FMLHandshakeMessage.ModList, Side)} when it is invoked during client/server connection handshake.
	 * Calls: Nothing
	 * Lets us find out if the server we are connecting to has NoCubes installed.
	 * NB: This is not called when looking at the list of servers in the 'Multiplayer' menu, only when actually connecting to a server.
	 * NB: This is also called for singleplayer.
	 */
	public static void onCheckModList(FMLHandshakeMessage.ModList modListPacket, Side side) {
		// We only care about the server's mod list, not the client's
		if (side == Side.SERVER) {
			NoCubesNetwork.currentServerHasNoCubes = modListPacket.modList().containsKey(NoCubes.MOD_ID);
		}
	}

	/**
	 * Called from: the very end of {@link NetHandlerPlayClient#handleJoinGame} when it is invoked during client/server connection handshake.
	 * Calls: {@link ClientEventSubscriber#onClientJoinServer()}
	 */
	@SideOnly(Side.CLIENT)
	public static void handleJoinGame(NetHandlerPlayClient handler) {
		ClientEventSubscriber.onClientJoinServer(handler);
	}

}
