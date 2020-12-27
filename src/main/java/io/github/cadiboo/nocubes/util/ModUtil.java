package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.tempcore.NoCubesLoadingPlugin;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.BiConsumer;

import static net.minecraft.init.Blocks.BEDROCK;
import static net.minecraft.init.Blocks.SNOW_LAYER;
import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Util that is used on BOTH physical sides
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ModUtil {

	// TODO: Remove once Direction.VALUES is ATed
	public static final EnumFacing[] DIRECTION_VALUES = EnumFacing.VALUES;
	public static final int DIRECTION_VALUES_LENGTH = DIRECTION_VALUES.length;
	public static final Random RANDOM = new Random();
	/**
	 * 1. Ops can bypass spawn protection.
	 * 2. Ops can use /clear, /difficulty, /effect, /gamemode, /gamerule, /give, /summon, /setblock and /tp, and can edit command blocks.
	 * 3. Ops can use /ban, /deop, /whitelist, /kick, and /op.
	 * 4. Ops can use /stop.
	 */
	private static final int COMMAND_PERMISSION_LEVEL = 2;
	private static final String COMMAND_PERMISSION_NAME = "give";

	public static boolean doesPlayerHavePermission(EntityPlayer player) {
		return player.canUseCommand(COMMAND_PERMISSION_LEVEL, COMMAND_PERMISSION_NAME);
	}

	/**
	 * @return Negative density if the block is smoothable (inside the isosurface), positive if it isn't
	 */
	public static float getIndividualBlockDensity(final boolean shouldSmooth, final IBlockState state) {
		if (shouldSmooth) {
			if (state.getBlock() == SNOW_LAYER) { // Snow layer
				final int value = state.getValue(BlockSnow.LAYERS);
				if (value == 1) { // zero-height snow layer
					return 1;
				} else { // snow height between 0-8 to between -0.25F and -1
					return -((value - 1) * 0.125F);
				}
			} else {
				return state.getBlock() == BEDROCK ? -1.0005F : -1;
			}
		} else if (state.isNormalCube() || state.isBlockNormalCube()) {
//		} else if (state.isSolid()) {
			return 0F;
		} else {
			return 1;
		}
	}

	/**
	 * Give the vec some (pseudo) random offset based on its location.
	 * This code is from {@link net.minecraft.util.math.MathHelper#getCoordinateRandom} and {@link net.minecraft.block.Block#getOffset}
	 */
	public static Vec offsetVertex(final Vec vec) {
		long rand = (long) (vec.x * 3129871.0D) ^ (long) vec.z * 116129781L ^ (long) vec.y;
		rand = rand * rand * 42317861L + rand * 11;
		vec.x += ((double) ((float) (rand >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
		vec.y += ((double) ((float) (rand >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
		vec.z += ((double) ((float) (rand >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;
		return vec;
	}

	/**
	 * Ew
	 *
	 * @param modContainer the {@link ModContainer} for {@link NoCubes}
	 */
	public static void launchUpdateDaemon(@Nonnull final ModContainer modContainer) {

		new Thread(() -> {
			while (true) {

//				final VersionChecker.CheckResult checkResult = VersionChecker.getResult(modContainer.getModInfo());
				final ForgeVersion.CheckResult checkResult = ForgeVersion.getResult(modContainer);
				switch (checkResult.status) {
					default:
					case PENDING:
						try {
							Thread.sleep(500L);
						} catch (InterruptedException var4) {
							Thread.currentThread().interrupt();
						}
						break;
					case OUTDATED:
						try {
							BadAutoUpdater.update(modContainer, checkResult.target.toString(), "Cadiboo");
						} catch (Exception var3) {
							throw new RuntimeException(var3);
						}
					case FAILED:
					case UP_TO_DATE:
					case AHEAD:
					case BETA:
					case BETA_OUTDATED:
						return;
				}
			}

//		}, modContainer.getModInfo().getDisplayName() + " Update Daemon").start();
		}, modContainer.getName() + " Update Daemon").start();

	}

	public static boolean isDeveloperWorkspace() {
//		final String target = System.getenv().get("target");
//		if (target == null) {
//			return false;
//		}
//		return target.contains("userdev");
		return NoCubesLoadingPlugin.DEVELOPER_ENVIRONMENT;
	}

//	/**
//	 * We add 1 because idk (it fixes seams in between chunks)
//	 * and then surface nets needs another +1 because reasons
//	 */
//	public static byte getMeshSizeX(final int initialSize, final MeshGenerator meshGenerator) {
//		return (byte) (initialSize + meshGenerator.getSizeXExtension());
//	}
//
//	/**
//	 * We add 1 because idk (it fixes seams in between chunks)
//	 * and then surface nets needs another +1 because reasons
//	 */
//	public static byte getMeshSizeY(final int initialSize, final MeshGenerator meshGenerator) {
//		return (byte) (initialSize + meshGenerator.getSizeYExtension());
//	}
//
//	/**
//	 * We add 1 because idk (it fixes seams in between chunks)
//	 * and then surface nets needs another +1 because reasons
//	 */
//	public static byte getMeshSizeZ(final int initialSize, final MeshGenerator meshGenerator) {
//		return (byte) (initialSize + meshGenerator.getSizeZExtension());
//	}

//	public static IFluidState getFluidState(final World world, final BlockPos pos) {
//		final int posX = pos.getX();
//		final int posY = pos.getY();
//		final int posZ = pos.getZ();
//
//		int currentChunkPosX = posX >> 4;
//		int currentChunkPosZ = posZ >> 4;
//		Chunk currentChunk = world.getChunk(currentChunkPosX, currentChunkPosZ);
//
//		final int extendRange = Config.extendFluidsRange.getRange();
//
//		if (extendRange == 0) {
//			return currentChunk.getFluidState(posX, posY, posZ);
//		}
//
//		final BlockState state = currentChunk.getBlockState(pos);
//
//		// Do not extend if not terrain smoothable
//		if (!state.nocubes_isTerrainSmoothable()) {
//			return state.getFluidState();
//		}
//
//		final IFluidState fluidState = state.getFluidState();
//		if (!fluidState.isEmpty()) {
//			return fluidState;
//		}
//
//		// For offset = -1 or -2 to offset = 1 or 2;
//		final int maxXOffset = extendRange;
//		final int maxZOffset = extendRange;
//
//		// Check up
//		{
//			final IFluidState state1 = currentChunk.getFluidState(posX, posY + 1, posZ);
//			if (state1.isSource()) {
//				return state1;
//			}
//		}
//
//		for (int xOffset = -maxXOffset; xOffset <= maxXOffset; ++xOffset) {
//			for (int zOffset = -maxZOffset; zOffset <= maxZOffset; ++zOffset) {
//
//				// No point in checking myself
//				if (xOffset == 0 && zOffset == 0) {
//					continue;
//				}
//
//				final int checkX = posX + xOffset;
//				final int checkZ = posZ + zOffset;
//
//				if (currentChunkPosX != checkX >> 4 || currentChunkPosZ != checkZ >> 4) {
//					currentChunkPosX = checkX >> 4;
//					currentChunkPosZ = checkZ >> 4;
//					currentChunk = world.getChunk(currentChunkPosX, currentChunkPosZ);
//				}
//
//				final IFluidState state1 = currentChunk.getFluidState(checkX, posY, checkZ);
//				if (state1.isSource()) {
//					return state1;
//				}
//
//			}
//		}
//		return fluidState;
//	}

//	/**
//	 * Mostly copied from StolenReposeCode.getDensity
//	 */
//	public static boolean doesTerrainCauseSuffocation(final IBlockReader reader, final BlockPos pos) {
//		float density = 0;
//		try (
//				ModProfiler ignored = ModProfiler.get().start("Collisions calculate cube density");
//				PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()
//		) {
////			final WorldBorder worldBorder = reader.getWorldBorder();
//
//			final int startX = pos.getX();
//			final int startY = pos.getY();
//			final int startZ = pos.getZ();
//
//			for (int zOffset = 0; zOffset < 2; ++zOffset) {
//				for (int yOffset = 0; yOffset < 2; ++yOffset) {
//					for (int xOffset = 0; xOffset < 2; ++xOffset) {
//
//						pooledMutableBlockPos.setPos(
//								startX - xOffset,
//								startY - yOffset,
//								startZ - zOffset
//						);
//
////						// Return a fully solid cube if its not loaded
////						if (!reader.isBlockLoaded(pooledMutableBlockPos) || !worldBorder.contains(pooledMutableBlockPos)) {
////							density += 1;
////							continue;
////						}
//
//						final BlockState testState = reader.getBlockState(pooledMutableBlockPos);
//						density += ModUtil.getIndividualBlockDensity(TERRAIN.test(testState), testState);
//					}
//				}
//			}
//		}
//
//		// > 0 means outside isosurface
//		// > -4 means mostly outside isosurface
//		return density > -4;
//	}

	/**
	 * @param material The {@link Material} to check
	 * @return If the material is tallgrass/grass plant/grass block
	 */
	public static boolean isMaterialGrass(final Material material) {
//		return material == Material.TALL_PLANTS || // tall grass, grass plant
//				material == Material.ORGANIC; // grass block
		return material == Material.VINE || // tall grass, grass plant
				material == Material.GRASS; // grass block
	}

	public static boolean isMaterialLeaves(final Material material) {
		return material == Material.LEAVES;
	}

	/**
	 * @param chunkPos The chunk position as a {@link BlockPos}
	 * @param blockPos The {@link BlockPos}
	 * @return The position relative to the chunkPos
	 */
	public static byte getRelativePos(final int chunkPos, final int blockPos) {
		final int blockPosChunkPos = (blockPos >> 4) << 4;
		if (chunkPos == blockPosChunkPos) { // if blockpos is in chunkpos's chunk
			return getRelativePos(blockPos);
		} else {
			// can be anything. usually between -1 and 16
			return (byte) (blockPos - chunkPos);
		}
	}

	/**
	 * @param blockPos The {@link BlockPos}
	 * @return The position (between 0-15) relative to the blockPos's chunk position
	 */
	public static byte getRelativePos(final int blockPos) {
		return (byte) (blockPos & 15);
	}

	public static void preloadClass(@Nonnull final String qualifiedName, @Nonnull final String simpleName) {
		Logger logger = getLogger();
		try {
			logger.debug("Loading class \"" + simpleName + "\"...");
			final ClassLoader classLoader = NoCubes.class.getClassLoader();
			final long startTime = System.nanoTime();
			Class.forName(qualifiedName, false, classLoader);
			logger.debug("Loaded class \"" + simpleName + "\" in " + (System.nanoTime() - startTime) + " nano seconds");
			logger.debug("Initialising class \"" + simpleName + "\"...");
			Class.forName(qualifiedName, true, classLoader);
			logger.debug("Initialised \"" + simpleName + "\"");
		} catch (final ClassNotFoundException e) {
			final CrashReport crashReport = CrashReport.makeCrashReport(e, "Failed to load class \"" + simpleName + "\". This should not be possible!");
			crashReport.makeCategory("Loading class");
			throw new ReportedException(crashReport);
		}
	}

	/**
	 * Assumes the array is indexed [z][y][x].
	 */
	public static int get3dIndexInto1dArray(int x, int y, int z, int xSize, int ySize) {
		return (z * xSize * ySize) + (y * xSize) + x;
	}

	public static void traverseArea(BlockPos startInclusive, BlockPos endInclusive, MutableBlockPos currentPosition, World world, BiConsumer<IBlockState, MutableBlockPos> func) {
		traverseArea(startInclusive.getX(), startInclusive.getY(), startInclusive.getZ(), endInclusive.getX(), endInclusive.getY(), endInclusive.getZ(), currentPosition, world, func);
	}

	/** Copied and tweaked from "https://github.com/Cadiboo/BiggerReactors/blob/1f0e0c48cdd16b8ecc0d2bc5f6c41db272dd8b7c/Phosphophyllite/src/main/java/net/roguelogix/phosphophyllite/util/Util.java#L76-L104". */
	public static void traverseArea(
			int startXInclusive, int startYInclusive, int startZInclusive,
			int endXInclusive, int endYInclusive, int endZInclusive,
			MutableBlockPos currentPosition, World world, BiConsumer<IBlockState, MutableBlockPos> func
	) {
		final IBlockState air = Blocks.AIR.getDefaultState();
		int endXPlus1 = endXInclusive + 1;
		int endYPlus1 = endYInclusive + 1;
		int endZPlus1 = endZInclusive + 1;
		int maxX = (endXInclusive + 16) & 0xFFFFFFF0;
		int maxY = (endYInclusive + 16) & 0xFFFFFFF0;
		int maxZ = (endZInclusive + 16) & 0xFFFFFFF0;
		for (int blockChunkX = startXInclusive; blockChunkX < maxX; blockChunkX += 16) {
			int maskedBlockChunkX = blockChunkX & 0xFFFFFFF0;
			int maskedNextBlockChunkX = (blockChunkX + 16) & 0xFFFFFFF0;
			for (int blockChunkZ = startZInclusive; blockChunkZ < maxZ; blockChunkZ += 16) {
				int maskedBlockChunkZ = blockChunkZ & 0xFFFFFFF0;
				int maskedNextBlockChunkZ = (blockChunkZ + 16) & 0xFFFFFFF0;
				int chunkX = blockChunkX >> 4;
				int chunkZ = blockChunkZ >> 4;
//				@Nullable
//				IChunk chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
				@Nullable
				Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
//				@Nullable
//				ChunkSection[] chunkSections = chunk == null ? null : chunk.getSections();
				@Nullable
				ExtendedBlockStorage[] chunkSections = chunk == null ? null : chunk.getBlockStorageArray();
				for (int blockChunkY = startYInclusive; blockChunkY < maxY; blockChunkY += 16) {
					int maskedBlockChunkY = blockChunkY & 0xFFFFFFF0;
					int maskedNextBlockChunkY = (blockChunkY + 16) & 0xFFFFFFF0;
					int chunkSectionIndex = blockChunkY >> 4;
//					@Nullable
//					ChunkSection chunkSection = chunkSections == null ? null : chunkSections[chunkSectionIndex];
					// If chunkSectionIndex is out of range we want to continue supplying air to the func
					// No clue how this will work with cubic chunks...
//					@Nullable
//					ChunkSection chunkSection = chunkSections == null || (chunkSectionIndex < 0 || chunkSectionIndex >= chunkSections.length) ? null : chunkSections[chunkSectionIndex];
					@Nullable
					ExtendedBlockStorage chunkSection = chunkSections == null || (chunkSectionIndex < 0 || chunkSectionIndex >= chunkSections.length) ? null : chunkSections[chunkSectionIndex];
					int sectionMinX = Math.max(maskedBlockChunkX, startXInclusive);
					int sectionMinY = Math.max(maskedBlockChunkY, startYInclusive);
					int sectionMinZ = Math.max(maskedBlockChunkZ, startZInclusive);
					int sectionMaxX = Math.min(maskedNextBlockChunkX, endXPlus1);
					int sectionMaxY = Math.min(maskedNextBlockChunkY, endYPlus1);
					int sectionMaxZ = Math.min(maskedNextBlockChunkZ, endZPlus1);
					for (int x = sectionMinX; x < sectionMaxX; ++x) {
						int maskedX = x & 15;
						for (int y = sectionMinY; y < sectionMaxY; ++y) {
							int maskedY = y & 15;
							for (int z = sectionMinZ; z < sectionMaxZ; ++z) {
								currentPosition.setPos(x, y, z);
								IBlockState blockState = chunkSection == null ? air : chunkSection.get(maskedX, maskedY, z & 15);
								func.accept(blockState, currentPosition);
							}
						}
					}
				}
			}
		}
	}

}
