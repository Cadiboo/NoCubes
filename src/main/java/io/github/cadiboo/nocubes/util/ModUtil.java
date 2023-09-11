package io.github.cadiboo.nocubes.util;

import com.google.common.collect.ImmutableList;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.tempcore.NoCubesLoadingPlugin;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStem;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.NoCubes.LOGGER;
import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static net.minecraft.init.Blocks.BEDROCK;
import static net.minecraft.init.Blocks.SNOW_LAYER;

/**
 * @author Cadiboo
 */
public class ModUtil {

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
	public static final int COMMAND_PERMISSION_LEVEL = 2;
	public static final String COMMAND_PERMISSION_NAME = "give";

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
	 * Give the vec3 some (pseudo) random offset based on its location.
	 * This code is from {link MathHelper#getCoordinateRandom} and Block#getOffset
	 *
	 * @param vec3 the vec3
	 */
	public static Vec3 offsetVertex(final Vec3 vec3) {
		long rand = (long) (vec3.x * 3129871.0D) ^ (long) vec3.z * 116129781L ^ (long) vec3.y;
		rand = rand * rand * 42317861L + rand * 11;
		vec3.x += ((double) ((float) (rand >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
		vec3.y += ((double) ((float) (rand >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
		vec3.z += ((double) ((float) (rand >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;
		return vec3;
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
//	public static byte getMeshSizeX(final int initialSize, final Mesher meshGenerator) {
//		return (byte) (initialSize + meshGenerator.getSizeXExtension());
//	}
//
//	/**
//	 * We add 1 because idk (it fixes seams in between chunks)
//	 * and then surface nets needs another +1 because reasons
//	 */
//	public static byte getMeshSizeY(final int initialSize, final Mesher meshGenerator) {
//		return (byte) (initialSize + meshGenerator.getSizeYExtension());
//	}
//
//	/**
//	 * We add 1 because idk (it fixes seams in between chunks)
//	 * and then surface nets needs another +1 because reasons
//	 */
//	public static byte getMeshSizeZ(final int initialSize, final Mesher meshGenerator) {
//		return (byte) (initialSize + meshGenerator.getSizeZExtension());
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
		try {
			LOGGER.info("Loading class \"" + simpleName + "\"...");
			final ClassLoader classLoader = NoCubes.class.getClassLoader();
			final long startTime = System.nanoTime();
			Class.forName(qualifiedName, false, classLoader);
			LOGGER.info("Loaded class \"" + simpleName + "\" in " + (System.nanoTime() - startTime) + " nano seconds");
			LOGGER.info("Initialising class \"" + simpleName + "\"...");
			Class.forName(qualifiedName, true, classLoader);
			LOGGER.info("Initialised \"" + simpleName + "\"");
		} catch (final ClassNotFoundException e) {
			final CrashReport crashReport = CrashReport.makeCrashReport(e, "Failed to load class \"" + simpleName + "\". This should not be possible!");
			crashReport.makeCategory("Loading class");
			throw new ReportedException(crashReport);
		}
	}




	public static final BlockPos VEC_ZERO = new BlockPos(0, 0, 0);
	public static final BlockPos VEC_ONE = new BlockPos(1, 1, 1);
	public static final BlockPos VEC_TWO = new BlockPos(2, 2, 2);
	public static final BlockPos CHUNK_SIZE = new BlockPos(16, 16, 16);
	public static final EnumFacing[] DIRECTIONS = EnumFacing.values();
	public static final float FULLY_SMOOTHABLE = 1;
	public static final float NOT_SMOOTHABLE = -FULLY_SMOOTHABLE;

	public static ImmutableList<IBlockState> getStates(Block block) {
		return block.getBlockState().getValidStates();
	}

	public static int length(BlockPos size) {
		return size.getX() * size.getY() * size.getZ();
	}

	public static void warnPlayer(@Nullable EntityPlayer player, String translationKey, Object... formatArgs) {
		TextComponentTranslation msg = new TextComponentTranslation(translationKey, formatArgs);
		if (player != null) {
			msg.getStyle().setColor(TextFormatting.RED);
			Minecraft.getMinecraft().player.sendMessage(msg);
		} else
			LogManager.getLogger("NoCubes notification fallback").warn(msg.getFormattedText());
	}

	public static float getBlockDensity(Predicate<IBlockState> isSmoothable, IBlockState state) {
		return getBlockDensity(isSmoothable.test(state), state);
	}

	/**
	 * @return Positive density if the block is smoothable (and will be at least partially inside the isosurface)
	 */
	public static float getBlockDensity(boolean shouldSmooth, IBlockState state) {
		if (!shouldSmooth)
			return NOT_SMOOTHABLE;
		if (isSnowLayer(state))
			// Snow layer, not the actual whole snow block
			return mapSnowHeight(state.getValue(BlockSnow.LAYERS));
		return FULLY_SMOOTHABLE;
	}

	/** Map snow height between 1-8 to between -1 and 1. */
	private static float mapSnowHeight(int value) {
		return -1 + (value - 1) * 0.25F;
	}

	public static boolean isSnowLayer(IBlockState state) {
		return state.getProperties().containsKey(BlockSnow.LAYERS);
	}

	public static boolean isShortPlant(IBlockState state) {
		Block block = state.getBlock();
		return block instanceof BlockBush && !(block instanceof BlockDoublePlant || block instanceof BlockCrops || block instanceof BlockStem);
	}

	public static boolean isPlant(IBlockState state) {
		return state.getBlock() instanceof IPlantable;
	}

	/**
	 * Assumes the array is indexed [z][y][x].
	 */
	public static int get3dIndexInto1dArray(int x, int y, int z, int xSize, int ySize) {
		return (xSize * ySize * z) + (xSize * y) + x;
	}

}
