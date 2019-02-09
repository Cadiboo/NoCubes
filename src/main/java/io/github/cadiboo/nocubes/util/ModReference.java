package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;

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
	public static final String MOD_NAME = "NoCubes";

	/**
	 * This is our Mod's Mod Id that is used for stuff like resource locations.
	 */
	public static final String MOD_ID = "nocubes";

	/**
	 * The fully qualified name of the version of IProxy that gets injected into {@link NoCubes#proxy} on a PHYSICAL CLIENT
	 */
	public static final String CLIENT_PROXY_CLASS = "io.github.cadiboo.nocubes.client.ClientProxy";

	/**
	 * The fully qualified name of the version of IProxy that gets injected into {@link NoCubes#proxy} on a PHYSICAL/DEDICATED SERVER
	 */
	public static final String SERVER_PROXY_CLASS = "io.github.cadiboo.nocubes.server.ServerProxy";

	/**
	 * @see "https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html"
	 */
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12.2]";

	/**
	 * @see "https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html"
	 */
	public static final String DEPENDENCIES = "" +
			"required-after:minecraft;" +
//			"required-after:forge@[14.23.5.2795,);" +
			"required-after:forge@[14.23.5.2768,);" +
			"required-after:render_chunk-rebuild_chunk-hooks@[1.12.2-0.3.0,1.12.2-0.4.0);" +
			"";

	/**
	 * "@VERSION@" is replaced by build.gradle with the actual version
	 *
	 * @see <a href= "https://mcforge.readthedocs.io/en/latest/conventions/versioning/">Forge Versioning Docs</a>
	 */
	public static final String VERSION = "@VERSION@";

	/**
	 * "@FINGERPRINT@" is replaced by build.gradle with the actual fingerprint
	 *
	 * @see "https://tutorials.darkhax.net/tutorials/jar_signing/"
	 */
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

	public static final String CONFIG_VERSION = "0.1.8";

	public static final String UPDATE_JSON = "https://raw.githubusercontent.com/Cadiboo/NoCubes/master/update.json";

}
