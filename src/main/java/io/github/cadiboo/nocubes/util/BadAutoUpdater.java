package io.github.cadiboo.nocubes.util;

import net.minecraftforge.fml.common.ModContainer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A bad auto-updater until I implement delta-patching
 *
 * @author Cadiboo
 */
@Deprecated
public final class BadAutoUpdater {

	public static boolean update(final ModContainer modContainer, final String updateVersion, final String githubUsername) throws IOException, URISyntaxException {
		if (ModUtil.isDeveloperWorkspace()) {
			return false;
		}

		final String modName = modContainer.getName();
		final File currentJar = modContainer.getSource();

		final String updateJarName = modName + "-" + updateVersion + ".jar";

		final URI updateUri = new URI("https://github.com/" + githubUsername + "/" + modName + "/releases/download/" + updateVersion + "/" + updateJarName);

		final Path updateJarPath = new File(currentJar.getParentFile(), updateJarName).toPath();

		try (BufferedInputStream inputStream = new BufferedInputStream(updateUri.toURL().openStream())) {
			if (Files.copy(inputStream, updateJarPath) <= 0) {
				//file copy failed, abort
				return false;
			}
		}

		Files.delete(currentJar.toPath());
		return true;
	}

}
