package io.github.cadiboo.nocubes.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import java.nio.file.Path;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;

/**
 * Our Mod's configuration
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = MOD_ID)
public final class NoCubesConfig {

	public static final ForgeConfigSpec CLIENT_CONFIG;
	public static final ForgeConfigSpec COMMON_CONFIG;
	public static final ForgeConfigSpec SERVER_CONFIG;

	static {
		final ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
		final ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
		final ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();

		MeshConfig.build(clientBuilder, commonBuilder, serverBuilder);

		CLIENT_CONFIG = clientBuilder.build();
		COMMON_CONFIG = serverBuilder.build();
		SERVER_CONFIG = serverBuilder.build();
	}

	public static void loadConfig(@Nonnull final ForgeConfigSpec spec, @Nonnull final Path path) {
		final CommentedFileConfig configData = CommentedFileConfig.builder(path)
				.sync()
				.autosave()
				.writingMode(WritingMode.REPLACE)
				.build();

		configData.load();
		spec.setConfig(configData);
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {

	}

	@SubscribeEvent
	public static void onFileChange(final ModConfig.ConfigReloading configEvent) {

	}

}
