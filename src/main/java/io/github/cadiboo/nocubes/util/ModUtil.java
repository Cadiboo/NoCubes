package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static net.minecraft.block.Blocks.BEDROCK;
import static net.minecraft.block.Blocks.SNOW;

/**
 * Util that is used on BOTH physical sides
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ModUtil {

	private static final Random RANDOM = new Random();

	/**
	 * @return negative density if the block is smoothable (inside the isosurface), positive if it isn't
	 */
	public static float getIndividualBlockDensity(final boolean shouldSmooth, final BlockState state) {
		if (shouldSmooth) {
			if (state.getBlock() == SNOW) {
				final int value = state.get(SnowBlock.LAYERS);
				if (value == 1) { // zero-height snow layer
					return 1;
				} else { // snow height between 0-8 to between -0.25F and -1
					return -((value - 1) * 0.125F);
				}
			} else {
				return state.getBlock() == BEDROCK ? -1.0005F : -1;
			}
//		} else if (state.isNormalCube() || state.isBlockNormalCube()) {
//			return 0F;
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
	public static Vec3 offsetVertex(Vec3 vec3) {
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

				final VersionChecker.CheckResult checkResult = VersionChecker.getResult(modContainer.getModInfo());
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

		}, modContainer.getModInfo().getDisplayName() + " Update Daemon").start();

	}

	public static boolean isDeveloperWorkspace() {
		final String target = System.getenv().get("target");
		if (target == null) {
			return false;
		}
		return target.contains("userdev");
	}

	/**
	 * Dirty hacks copied from https://gist.github.com/TehNut/2bbdef334a5bb5d6d67b7250987cc6e0
	 * I hate doing this and I hope that I'll be able to properly implement the config gui soon
	 */
	public static void doConfigHacks() {
		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();

			Field _sortedList = ModList.class.getDeclaredField("sortedList");
			_sortedList.setAccessible(true);
			MethodHandle _getSortedList = lookup.unreflectGetter(_sortedList);

			List<ModInfo> sortedList = (List<ModInfo>) _getSortedList.invokeExact((ModList) ModList.get());
			ModInfo wailaInfo = sortedList.stream().filter(modInfo -> modInfo.getModId().equals(MOD_ID)).findFirst().get();
			ConfigModInfo modInfo = new ConfigModInfo(wailaInfo);
			sortedList.set(sortedList.indexOf(wailaInfo), new ConfigModInfo(wailaInfo));

			ModContainer wailaContainer = ModList.get().getModContainerById(MOD_ID).get();
			Field _modInfo = ModContainer.class.getDeclaredField("modInfo");
			_modInfo.setAccessible(true);
			MethodHandle _setModInfo = lookup.unreflectSetter(_modInfo);
			_setModInfo.invokeExact((ModContainer) wailaContainer, (IModInfo) modInfo);
		} catch (Throwable e) {
			NoCubes.LOGGER.error("Failed to replace ModInfo instance with one that supports the mod list config");
		}
	}

	private static class ConfigModInfo extends ModInfo {
		public ConfigModInfo(ModInfo modInfo) {
			super(modInfo.getOwningFile(), modInfo.getModConfig());
		}

		@Override
		public boolean hasConfigUI() {
			return true;
		}
	}

}
