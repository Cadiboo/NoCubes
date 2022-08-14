package io.github.cadiboo.nocubes.config;

import io.github.cadiboo.nocubes.repackage.net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class ConfigHolder {

	@Nonnull
	public static final ForgeConfigSpec CLIENT_SPEC;
	@Nonnull
	public static final ForgeConfigSpec SERVER_SPEC;
	@Nonnull
	public static final ClientConfig CLIENT;
	@Nonnull
	public static final ServerConfig SERVER;
	static {
		{
			final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
			CLIENT = specPair.getLeft();
			CLIENT_SPEC = specPair.getRight();
		}
		{
			final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
			SERVER = specPair.getLeft();
			SERVER_SPEC = specPair.getRight();
		}
	}
}
