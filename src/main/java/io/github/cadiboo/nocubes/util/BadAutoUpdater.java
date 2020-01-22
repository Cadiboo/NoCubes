package io.github.cadiboo.nocubes.util;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A bad auto-updater until I implement delta-patching.
 *
 * @author Cadiboo
 */
@Deprecated
public final class BadAutoUpdater {

	public static boolean update(final ModContainer modContainer, final String updateVersion, final String githubUsername) throws IOException, URISyntaxException {
		if (ModUtil.isDeveloperWorkspace())
			return false;

		final IModInfo modInfo = modContainer.getModInfo();
		final File currentJar = ((ModFileInfo) modInfo.getOwningFile()).getFile().getFilePath().toFile();
		final String modName = StringUtils.splitByWholeSeparator(currentJar.getName(), "-" + modInfo.getVersion().toString())[0];

		final String updateJarName = modName + "-" + updateVersion + ".jar";

		final URI updateUri = new URI("https://github.com/" + githubUsername + "/" + modName + "/releases/download/" + updateVersion + "/" + updateJarName);

		final Path updateJarPath = new File(currentJar.getParentFile(), updateJarName).toPath();

		try (BufferedInputStream inputStream = new BufferedInputStream(updateUri.toURL().openStream())) {
			if (Files.copy(inputStream, updateJarPath) <= 0)
				return false; // File copy failed, abort
		}

		Files.delete(currentJar.toPath());
		return true;
	}

}
