package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * Dirty hacks copied from https://gist.github.com/TehNut/2bbdef334a5bb5d6d67b7250987cc6e0
 * I hate doing this and I hope that I'll be able to properly implement the config gui soon
 *
 * @author Cadiboo
 */
@SuppressWarnings("all")
public final class TempClientConfigHacks {

	public static void doConfigHacks() {
		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();

			Field _sortedList = ModList.class.getDeclaredField("sortedList");
			_sortedList.setAccessible(true);
			MethodHandle _getSortedList = lookup.unreflectGetter(_sortedList);

			List<ModInfo> sortedList = (List<ModInfo>) _getSortedList.invokeExact((ModList) ModList.get());
			ModInfo configInfo = sortedList.stream().filter(modInfo -> modInfo.getModId().equals(MOD_ID)).findFirst().get();
			ConfigModInfo modInfo = new ConfigModInfo(configInfo);
			sortedList.set(sortedList.indexOf(configInfo), new ConfigModInfo(configInfo));

			ModContainer modContainer = ModList.get().getModContainerById(MOD_ID).get();
			Field _modInfo = ModContainer.class.getDeclaredField("modInfo");
			_modInfo.setAccessible(true);
			MethodHandle _setModInfo = lookup.unreflectSetter(_modInfo);
			_setModInfo.invokeExact((ModContainer) modContainer, (IModInfo) modInfo);
		} catch (Throwable e) {
			NoCubes.LOGGER.error("Failed to replace ModInfo instance with one that supports the mod list config");
		}
	}

	private static class ConfigModInfo extends ModInfo {

		private ConfigModInfo(ModInfo modInfo) {
			super(modInfo.getOwningFile(), modInfo.getModConfig());
		}

		@Override
		public boolean hasConfigUI() {
			return true;
		}

	}

}
