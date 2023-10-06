package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.optifine.OptiFineLocator;
import io.github.cadiboo.nocubes.client.render.SmoothLightingFluidBlockRenderer;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.repackage.net.minecraftforge.fml.config.ConfigTracker;
import io.github.cadiboo.nocubes.repackage.net.minecraftforge.fml.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.IRegistryDelegate;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import static io.github.cadiboo.nocubes.client.optifine.OptiFineLocator.SUPPORTED_SERIES;
import static io.github.cadiboo.nocubes.util.StateHolder.GRASS_BLOCK_DEFAULT;
import static io.github.cadiboo.nocubes.util.StateHolder.GRASS_BLOCK_SNOWY;
import static io.github.cadiboo.nocubes.util.StateHolder.PODZOL_SNOWY;
import static io.github.cadiboo.nocubes.util.StateHolder.SNOW_LAYER_DEFAULT;
import static net.minecraft.util.BlockRenderLayer.CUTOUT;
import static net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED;

/**
 * Util that is only used on the Physical Client i.e. Rendering code
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ClientUtil {

	public static final BlockRenderLayer[] BLOCK_RENDER_LAYER_VALUES = BlockRenderLayer.values();
	public static final int BLOCK_RENDER_LAYER_VALUES_LENGTH = BLOCK_RENDER_LAYER_VALUES.length;
	static final int[] NEGATIVE_1_8000 = new int[8000];
	private static final int[][] OFFSETS_ORDERED = {
			// check 6 immediate neighbours
			{+0, -1, +0},
			{+0, +1, +0},
			{-1, +0, +0},
			{+1, +0, +0},
			{+0, +0, -1},
			{+0, +0, +1},
			// check 12 non-immediate, non-corner neighbours
			{-1, -1, +0},
			{-1, +0, -1},
			{-1, +0, +1},
			{-1, +1, +0},
			{+0, -1, -1},
			{+0, -1, +1},
			// {+0, +0, +0}, // Don't check self
			{+0, +1, -1},
			{+0, +1, +1},
			{+1, -1, +0},
			{+1, +0, -1},
			{+1, +0, +1},
			{+1, +1, +0},
			// check 8 corner neighbours
			{+1, +1, +1},
			{+1, +1, -1},
			{-1, +1, +1},
			{-1, +1, -1},
			{+1, -1, +1},
			{+1, -1, -1},
			{-1, -1, +1},
			{-1, -1, -1},
	};
	static {
		Arrays.fill(ClientUtil.NEGATIVE_1_8000, -1);
	}

	// Added by Forge, no SRG name
	private static final Field BLOCK_COLOR_MAP = ObfuscationReflectionHelper.findField(BlockColors.class, "blockColorMap");

	public static void warnPlayer(String translationKey, Object... formatArgs) {
		ModUtil.warnPlayer(Minecraft.getMinecraft().player, translationKey, formatArgs);
	}

	public static boolean isStateSnow(final IBlockState checkState) {
		if (checkState == SNOW_LAYER_DEFAULT) return true;
		if (checkState == GRASS_BLOCK_SNOWY) return true;
		return checkState == PODZOL_SNOWY;
	}

	private static boolean isStateGrass(final IBlockState checkState) {
		return checkState == GRASS_BLOCK_DEFAULT;
	}

	@Nonnull
	public static BlockRenderLayer getCorrectRenderLayer(@Nonnull final IBlockState state) {
		return getCorrectRenderLayer(state.getBlock().getRenderLayer());
	}

//	@Nonnull
//	public static BlockRenderLayer getCorrectRenderLayer(@Nonnull final IFluidState state) {
//		return getCorrectRenderLayer(state.getRenderLayer());
//	}

	// TODO: Optimise `Minecraft.getInstance().gameSettings.mipmapLevels`? Store it in a field somewhere?
	@Nonnull
	public static BlockRenderLayer getCorrectRenderLayer(@Nonnull final BlockRenderLayer blockRenderLayer) {
		switch (blockRenderLayer) {
			default:
			case SOLID:
			case TRANSLUCENT:
				return blockRenderLayer;
			case CUTOUT_MIPPED:
				return Minecraft.getMinecraft().gameSettings.mipmapLevels == 0 ? CUTOUT : CUTOUT_MIPPED;
			case CUTOUT:
				return Minecraft.getMinecraft().gameSettings.mipmapLevels != 0 ? CUTOUT_MIPPED : CUTOUT;
		}
	}

	public static BufferBuilder startOrContinueBufferBuilder(final ChunkCompileTaskGenerator generator, final CompiledChunk compiledChunk, final BlockRenderLayer blockRenderLayer, RenderChunk chunkRender, BlockPos renderChunkPosition) {
		final BufferBuilder bufferBuilder = generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(blockRenderLayer);
		if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
			compiledChunk.setLayerStarted(blockRenderLayer);
			chunkRender.preRenderBlocks(bufferBuilder, renderChunkPosition);
		}
		return bufferBuilder;
	}

	public static void tryReloadRenderers() {
		final RenderGlobal worldRenderer = Minecraft.getMinecraft().renderGlobal;
		if (worldRenderer != null) {
			worldRenderer.loadRenderers();
		}
	}

//	public static Chunk getChunk(final int currentChunkPosX, final int currentChunkPosZ, final IBlockAccess reader) {
////		if (reader instanceof IWorld) { // This should never be the case...
////			return ((IWorld) reader).getChunk(currentChunkPosX, currentChunkPosZ);
////		} else
//		if (reader instanceof ChunkCache) {
//			final ChunkCache renderChunkCache = (ChunkCache) reader;
//			final int x = currentChunkPosX - renderChunkCache.chunkX;
//			final int z = currentChunkPosZ - renderChunkCache.chunkZ;
//			return renderChunkCache.chunkArray[x][z];
//		} else if (OptiFineCompatibility.PROXY.isChunkCacheOF(reader)) {
//			final ChunkCache renderChunkCache = OptiFineCompatibility.PROXY.getChunkRenderCache(reader);
//			final int x = currentChunkPosX - renderChunkCache.chunkX;
//			final int z = currentChunkPosZ - renderChunkCache.chunkZ;
//			return renderChunkCache.chunkArray[x][z];
//		}
//		final CrashReport crashReport = CrashReport.makeCrashReport(new IllegalStateException(), "Invalid ChunkRenderCache: " + reader);
//		crashReport.makeCategory("NoCubes getting Chunk");
//		throw new ReportedException(crashReport);
//	}

//	public static void setupChunkRenderCache(final ChunkRenderCache _this, final int chunkStartX, final int chunkStartZ, final Chunk[][] chunks, final BlockPos start, final BlockPos end) {
//		final int startX = start.getX();
//		final int startY = start.getY();
//		final int startZ = start.getZ();
//
//		final int cacheSizeX = end.getX() - startX + 1;
//		final int cacheSizeY = end.getY() - startY + 1;
//		final int cacheSizeZ = end.getZ() - startZ + 1;
//
//		final int size = cacheSizeX * cacheSizeY * cacheSizeZ;
//		final BlockState[] blockStates = new BlockState[size];
//		final IFluidState[] fluidStates = new IFluidState[size];
//
//		int cx = (startX >> 4) - chunkStartX;
//		int cz = (startZ >> 4) - chunkStartZ;
//		Chunk currentChunk = chunks[cx][cz];
//
//		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
//			int index = 0;
//			for (int z = 0; z < cacheSizeZ; ++z) {
//				for (int y = 0; y < cacheSizeY; ++y) {
//					for (int x = 0; x < cacheSizeX; ++x, ++index) {
//
//						final int posX = startX + x;
//						final int posY = startY + y;
//						final int posZ = startZ + z;
//
//						final int ccx = ((startX + x) >> 4) - chunkStartX;
//						final int ccz = ((startZ + z) >> 4) - chunkStartZ;
//
//						boolean changed = false;
//						if (cx != ccx) {
//							cx = ccx;
//							changed = true;
//						}
//						if (cz != ccz) {
//							cz = ccz;
//							changed = true;
//						}
//						if (changed) {
//							currentChunk = chunks[cx][cz];
//						}
//
//						// TODO: Use System.arrayCopy on the chunk sections
//						pooledMutableBlockPos.setPos(posX, posY, posZ);
////						blockStates[index] = currentChunk.getBlockState(pooledMutableBlockPos);
////						fluidStates[index] = currentChunk.getFluidState(posX, posY, posZ);
//						final BlockState blockState = currentChunk.getBlockState(pooledMutableBlockPos);
//						blockStates[index] = blockState;
//						fluidStates[index] = blockState.getFluidState();
//					}
//				}
//			}
//		}
//
//		_this.cacheSizeX = cacheSizeX;
//		_this.cacheSizeY = cacheSizeY;
//		_this.cacheSizeZ = cacheSizeZ;
//
//		_this.blockStates = blockStates;
//		_this.fluidStates = fluidStates;
//	}

	public static void replaceFluidRenderer() {
		NoCubes.LOGGER.debug("Replacing fluid renderer");
		Minecraft.getMinecraft().getBlockRendererDispatcher().fluidRenderer = ClientEventSubscriber.smoothLightingBlockFluidRenderer = new SmoothLightingFluidBlockRenderer();
		NoCubes.LOGGER.debug("Replaced fluid renderer");
	}

	public static void crashIfIncompatibleOptiFine() {
		String optiFineVersion = OptiFineLocator.getOptiFineVersion();
		final String message = "Incompatible OptiFine version detected! Please use the " + SUPPORTED_SERIES + " series (Installed: " + optiFineVersion + ")";
		throw new CustomModLoadingErrorDisplayException(message, new IllegalStateException(message)) {

			private final String[] lines = new String[]{
					"Incompatible OptiFine version detected!",
					"Please use the " + SUPPORTED_SERIES + " series",
					"(Installed: " + optiFineVersion + ")"
			};

			@Override
			public void initGui(final GuiErrorScreen errorScreen, final FontRenderer fontRenderer) {
			}

			@Override
			public void drawScreen(final GuiErrorScreen errorScreen, final FontRenderer fontRenderer, final int mouseRelX, final int mouseRelY, final float tickTime) {
				final int x = errorScreen.width / 2;
				final int y = errorScreen.height / 2 / 2;
				final String[] lines = this.lines;
				for (int i = 0, linesLength = lines.length; i < linesLength; i++)
					errorScreen.drawCenteredString(fontRenderer, lines[i], x, y + i * 10, 0xFFFFFF);
			}
		};
	}

	public static void crashIfRCRCHInstalled() {
		final String message = "NoCubes Dependency Error! RenderChunk rebuildChunk Hooks CANNOT be installed! Remove RenderChunk rebuildChunk Hooks from the mods folder and then restart the game.";
		throw new CustomModLoadingErrorDisplayException(message, new IllegalStateException(message)) {

			private final String[] lines = new String[]{
					"NoCubes Dependency Error!",
					"",
					"RenderChunk rebuildChunk Hooks CANNOT be installed!",
					"",
					"Remove RenderChunk rebuildChunk Hooks from",
					"the mods folder and then restart the game."
			};

			@Override
			public void initGui(final GuiErrorScreen errorScreen, final FontRenderer fontRenderer) {
			}

			@Override
			public void drawScreen(final GuiErrorScreen errorScreen, final FontRenderer fontRenderer, final int mouseRelX, final int mouseRelY, final float tickTime) {
				final int x = errorScreen.width / 2;
				final int y = errorScreen.height / 2 / 2;
				final String[] lines = this.lines;
				for (int i = 0, linesLength = lines.length; i < linesLength; i++)
					errorScreen.drawCenteredString(fontRenderer, lines[i], x, y + i * 10, 0xFFFFFF);
			}
		};
	}

	public static void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately) {
		final RenderGlobal worldRenderer = Minecraft.getMinecraft().renderGlobal;
		if (worldRenderer != null && worldRenderer.world != null && worldRenderer.viewFrustum != null)
			worldRenderer.markBlocksForUpdate(minX, minY, minZ, maxX, maxY, maxZ, updateImmediately);
	}

    public static Map<IRegistryDelegate<Block>, IBlockColor> getBlockColorsMap() {
		try {
			return (Map<IRegistryDelegate<Block>, IBlockColor>) BLOCK_COLOR_MAP.get(Minecraft.getMinecraft().getBlockColors());
		} catch (IllegalAccessException e) {
			throw new RuntimeException("IllegalAccessException while getting the BlockColorsMap... how? It should have been made accessible", e);
		}
	}

	public static void warnPlayerIfVisualsDisabled() {
		if (!NoCubesConfig.Client.render)
			warnPlayer(
				NoCubes.MOD_ID + ".notification.visualsDisabled",
				ClientEventSubscriber.translate(ClientEventSubscriber.TOGGLE_VISUALS),
				NoCubesConfig.Client.RENDER,
				clientConfigComponent()
			);
	}

	public static void sendPlayerInfoMessage() {
		if (NoCubesConfig.Client.infoMessage)
			Minecraft.getMinecraft().player.sendMessage(
				new TextComponentTranslation(NoCubes.MOD_ID + ".notification.infoMessage",
					ClientEventSubscriber.translate(ClientEventSubscriber.TOGGLE_SMOOTHABLE),
					NoCubesConfig.Client.INFO_MESSAGE,
					clientConfigComponent()
				).setStyle(new Style().setColor(TextFormatting.GREEN))
			);
	}

	private static ITextComponent clientConfigComponent() {
		File configFile = new File(ConfigTracker.INSTANCE.getConfigFileName(NoCubes.MOD_ID, ModConfig.Type.CLIENT));
		return new TextComponentString(configFile.getName())
			.setStyle(new Style()
				.setUnderlined(true)
				.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, configFile.getAbsolutePath()))
			);
	}

}
