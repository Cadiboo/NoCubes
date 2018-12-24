package io.github.cadiboo.nocubes_mmd_winterjam.util;

import io.github.cadiboo.nocubes_mmd_winterjam.NoCubes;
import net.minecraft.crash.CrashReport;

/**
 * Holds mod-wide constant values
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ModReference {

	/**
	 * This is our Mod's Name.
	 */
	public static final String MOD_NAME = "NoCubes MMD Winterjam";

	/**
	 * This is our Mod's Mod Id that is used for stuff like resource locations.
	 */
	public static final String MOD_ID = "nocubes_mmd_winterjam";
	/**
	 * The fully qualified name of the version of IProxy that gets injected into {@link NoCubes#proxy} on a PHYSICAL CLIENT
	 */
	public static final String CLIENT_PROXY_CLASS = "io.github.cadiboo.nocubes_mmd_winterjam.client.ClientProxy";
	/**
	 * The fully qualified name of the version of IProxy that gets injected into {@link NoCubes#proxy} on a PHYSICAL/DEDICATED SERVER
	 */
	public static final String SERVER_PROXY_CLASS = "io.github.cadiboo.nocubes_mmd_winterjam.server.ServerProxy";
	/**
	 * @see "https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html"
	 */
	public static final String ACCEPTED_VERSIONS = "[1.12.2]";
	/**
	 * @see "https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html"
	 */
	public static final String DEPENDENCIES = "" +
			"required-after:minecraft;" +
			"required-after:forge@[14.23.4.2704,);" +
			"required-after:render_chunk_rebuild_chunk_hooks@[0.1.1,);" +
			"";
	/**
	 * "@VERSION@" is replaced by build.gradle with the actual version
	 *
	 * @see <a href= "https://mcforge.readthedocs.io/en/latest/conventions/versioning/">Forge Versioning Docs</a>
	 */
	public static final String VERSION = "@VERSION@";
	static {
		if (MOD_ID.length() > 64) {
			final IllegalStateException exception = new IllegalStateException("Mod Id is too long!");
			CrashReport crashReport = new CrashReport("Mod Id must be 64 characters or shorter!", exception);
			crashReport.makeCategory("Constructing Mod");
		}
	}

}
