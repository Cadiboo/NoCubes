package io.github.cadiboo.nocubes.util;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A bad auto-updater until I implement delta-patching
 *
 * @author Cadiboo
 */
public final class BadAutoUpdater {

	public static void update(final ArtifactVersion currentVersion, final String newVersion) throws Exception {

		final boolean developerEnvironment = ModUtil.isDeveloperWorkspace();
		if (developerEnvironment) {
			return;
		}

		final String modFileName = "NoCubes";

		final File modsDir = FMLPaths.MODSDIR.get().toFile();

		final String newJarFileName = modFileName + "-" + newVersion + ".jar";

		final Path pathToNewJar = new File(modsDir, newJarFileName).toPath();

		final URI updateUri = new URI("https://github.com/Cadiboo/" + modFileName + "/releases/download/" + newVersion + "/" + newJarFileName);
//		final URI updateUri = new URI("file:///Users/Cadiboo/Desktop/NoCubesJars/download/" + newJarFileName);

		boolean somethingWasDone = false;

		try (BufferedInputStream inputStream = new BufferedInputStream(updateUri.toURL().openStream())) {
			somethingWasDone = Files.copy(inputStream, pathToNewJar) > 0;
		}

		if (!somethingWasDone) {
			return;
		}

		//delete the current jar
		final String oldJarFileName = modFileName + "-" + currentVersion + ".jar";
		final Path pathToOldJar = new File(modsDir, oldJarFileName).toPath();

		Files.delete(pathToOldJar);

	}

}
