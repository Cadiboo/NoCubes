package io.github.cadiboo.nocubes;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URI;

/**
 * Modified from https://github.com/CaffeineMC/sodium-fabric/blob/55503937143a72a6c06676ed63f6de7a048e72ee/src/desktop/java/net/caffeinemc/mods/sodium/desktop/LaunchWarn.java
 */
public final class DoNotRunThisFromTheConsoleDialog {

	public static void main(String... args) {
		final var modName = "NoCubes";
		final var installLinkBtnLabel = "Help";
		final var installationUrl = "https://www.wikihow.com/Install-Minecraft-Forge";
		final var mainMsg = "You have tried to launch " + modName + " (a Minecraft mod) directly, but it is not an executable program or mod installer. You must install Forge Loader for Minecraft, and place this file in your mods directory instead.";
		final var guiMsg = mainMsg + "\nIf this is your first time installing mods for Forge Loader, click \"" + installLinkBtnLabel + "\" for a guide on how to do this.";
		final var cmdMsg = mainMsg + "\nIf this is your first time installing mods for Forge Loader, open \"" + installationUrl + "\" for a guide on how to do this.";
		if (GraphicsEnvironment.isHeadless()) {
			System.err.println(cmdMsg);
		} else {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ReflectiveOperationException | UnsupportedLookAndFeelException ignored) {
				// Ignored
			}

			if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				int option = JOptionPane.showOptionDialog(null, guiMsg, modName, JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, new Object[] { installLinkBtnLabel, "Cancel" }, JOptionPane.YES_OPTION);

				if (option == JOptionPane.YES_OPTION) {
					try {
						Desktop.getDesktop().browse(URI.create(installationUrl));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				// Fallback for Linux, etc users with no "default" browser
				JOptionPane.showMessageDialog(null, cmdMsg);
			}
		}
		System.exit(0);
	}
}
