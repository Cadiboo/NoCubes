/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.network;

import io.github.cadiboo.nocubes.network.S2CSyncConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConfigSync {
    public static final ConfigSync INSTANCE = new ConfigSync(ConfigTracker.INSTANCE);
    private final ConfigTracker tracker;

    private ConfigSync(final ConfigTracker tracker) {
        this.tracker = tracker;
    }

    public List<Pair<String, S2CSyncConfig>> syncConfigs(boolean isLocal) {
        final Map<String, byte[]> configData = tracker.configSets().get(ModConfig.Type.SERVER).stream().collect(Collectors.toMap(ModConfig::getFileName, mc -> {
            try {
                return Files.readAllBytes(mc.getFullPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        return configData.entrySet().stream().map(e->Pair.of("Config "+e.getKey(), new S2CSyncConfig(e.getKey(), e.getValue()))).collect(Collectors.toList());
    }

    public void receiveSyncedConfig(final S2CSyncConfig s2CConfigData) {
        if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
            Optional.ofNullable(tracker.fileMap().get(s2CConfigData.getFileName())).ifPresent(mc-> mc.acceptSyncedConfig(s2CConfigData.getBytes()));
        }
    }
}