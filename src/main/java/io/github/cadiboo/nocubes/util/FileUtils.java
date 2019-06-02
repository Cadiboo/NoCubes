//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.github.cadiboo.nocubes.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

	private static final Logger LOGGER = LogManager.getLogger();

	public FileUtils() {
	}

	public static Path getOrCreateDirectory(Path dirPath, String dirLabel) {
		if (!Files.isDirectory(dirPath.getParent())) {
			getOrCreateDirectory(dirPath.getParent(), "parent of " + dirLabel);
		}

		if (!Files.isDirectory(dirPath)) {
			LOGGER.debug("Making {} directory : {}", dirLabel, dirPath);

			try {
				Files.createDirectory(dirPath);
			} catch (IOException var3) {
				if (var3 instanceof FileAlreadyExistsException) {
					LOGGER.fatal("Failed to create {} directory - there is a file in the way", dirLabel);
				} else {
					LOGGER.fatal("Problem with creating {} directory (Permissions?)", dirLabel, var3);
				}

				throw new RuntimeException("Problem creating directory", var3);
			}

			LOGGER.debug("Created {} directory : {}", dirLabel, dirPath);
		} else {
			LOGGER.debug("Found existing {} directory : {}", dirLabel, dirPath);
		}

		return dirPath;
	}

	public static String fileExtension(Path path) {
		String fileName = path.getFileName().toString();
		int idx = fileName.lastIndexOf(46);
		return idx > -1 ? fileName.substring(idx + 1) : "";
	}

}
