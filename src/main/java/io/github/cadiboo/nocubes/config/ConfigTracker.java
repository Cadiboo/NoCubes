/*
 * Minecraft Forge
 * Copyright (c) 2016-2019.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package io.github.cadiboo.nocubes.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import io.github.cadiboo.nocubes.network.S2CSyncConfig;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ConfigTracker {

	public static final ConfigTracker INSTANCE = new ConfigTracker();
	//	static final Marker CONFIG = MarkerManager.getMarker("CONFIG");
	private static final Logger LOGGER = LogManager.getLogger();
	private final ConcurrentHashMap<String, ModConfig> fileMap;
	private final EnumMap<ModConfig.Type, Set<ModConfig>> configSets;

	private ConfigTracker() {
		this.fileMap = new ConcurrentHashMap<>();
		this.configSets = new EnumMap<>(ModConfig.Type.class);
		this.configSets.put(ModConfig.Type.CLIENT, Collections.synchronizedSet(new LinkedHashSet<>()));
		this.configSets.put(ModConfig.Type.COMMON, Collections.synchronizedSet(new LinkedHashSet<>()));
//        this.configSets.put(ModConfig.Type.PLAYER, new ConcurrentSkipListSet<>());
		this.configSets.put(ModConfig.Type.SERVER, Collections.synchronizedSet(new LinkedHashSet<>()));
	}

	void trackConfig(final ModConfig config) {
		if (this.fileMap.containsKey(config.getFileName())) {
			LOGGER.error("Detected config file conflict {} between {} and {}", config.getFileName(), this.fileMap.get(config.getFileName()).getModId(), config.getModId());
			throw new RuntimeException("Config conflict detected!");
		}
		this.fileMap.put(config.getFileName(), config);
		this.configSets.get(config.getType()).add(config);
		LOGGER.debug("Config file {} for {} tracking", config.getFileName(), config.getModId());
	}

	public void loadConfigs(ModConfig.Type type, Path configBasePath) {
		LOGGER.debug("Loading configs type {}", type);
		this.configSets.get(type).forEach(config -> openConfig(config, configBasePath));
	}

//	public List<Pair<String, FMLHandshakeMessage.S2CConfigData>> syncConfigs(boolean isLocal) {
//		final Map<String, byte[]> configData = configSets.get(ModConfig.Type.SERVER).stream().collect(Collectors.toMap(ModConfig::getFileName, mc -> { //TODO: Test cpw's LambdaExceptionUtils on Oracle javac.
//			try {
//				return Files.readAllBytes(mc.getFullPath());
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}));
//		return configData.entrySet().stream().map(e -> Pair.of("Config " + e.getKey(), new FMLHandshakeMessage.S2CConfigData(e.getKey(), e.getValue()))).collect(Collectors.toList());
//	}

	public List<Pair<String, S2CSyncConfig>> syncConfigs(boolean isLocal) {
		final Map<String, byte[]> configData = configSets.get(ModConfig.Type.SERVER).stream().collect(Collectors.toMap(ModConfig::getFileName, mc -> { //TODO: Test cpw's LambdaExceptionUtils on Oracle javac.
			try {
				return Files.readAllBytes(mc.getFullPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}));
		return configData.entrySet().stream().map(e -> Pair.of("Config " + e.getKey(), new S2CSyncConfig(e.getKey(), e.getValue()))).collect(Collectors.toList());
	}

	private void openConfig(final ModConfig config, final Path configBasePath) {
		LOGGER.debug("Loading config file type {} at {} for {}", config.getType(), config.getFileName(), config.getModId());
		final CommentedFileConfig configData = config.getHandler().reader(configBasePath).apply(config);
		config.setConfigData(configData);
		config.fireEvent(new ModConfig.Loading(config));
		config.save();
	}

//	public void receiveSyncedConfig(final FMLHandshakeMessage.S2CConfigData s2CConfigData) {
//		if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
//			Optional.ofNullable(fileMap.get(s2CConfigData.getFileName())).ifPresent(mc -> {
//				mc.setConfigData(TomlFormat.instance().createParser().parse(new ByteArrayInputStream(s2CConfigData.getBytes())));
//				mc.fireEvent(new ModConfig.ConfigReloading(mc));
//			});
//		}
//	}

	public void receiveSyncedConfig(final S2CSyncConfig s2CConfigData) {
		if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
			Optional.ofNullable(fileMap.get(s2CConfigData.getFileName())).ifPresent(mc -> {
				mc.setConfigData(TomlFormat.instance().createParser().parse(new ByteArrayInputStream(s2CConfigData.getBytes())));
				mc.fireEvent(new ModConfig.ConfigReloading(mc));
			});
		}
	}

	public void loadDefaultServerConfigs() {
		configSets.get(ModConfig.Type.SERVER).forEach(modConfig -> {
			final CommentedConfig commentedConfig = CommentedConfig.inMemory();
			modConfig.getSpec().correct(commentedConfig);
			modConfig.setConfigData(commentedConfig);
			// This isn't in forge, but appears to be necessary?
			modConfig.fireEvent(new ModConfig.Loading(modConfig));
		});
	}

}
