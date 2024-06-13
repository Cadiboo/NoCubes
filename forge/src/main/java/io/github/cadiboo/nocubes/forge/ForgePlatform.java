package io.github.cadiboo.nocubes.forge;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.config.NoCubesConfigImpl;
import io.github.cadiboo.nocubes.network.C2SRequestUpdateSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.platform.IPlatform;
import io.github.cadiboo.nocubes.util.IBlockStateSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.beryx.awt.color.ColorFactory;

import java.io.File;
import java.util.stream.Collectors;

public class ForgePlatform implements IPlatform {

	private static final Logger LOG = LogManager.getLogger();

	@Override
	public String getPlatformName() {
		return "Forge";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}

	@Override
	public Color parseColor(String color) {
		try {
			final java.awt.Color parsed = ColorFactory.valueOf(color);
			return new Color(parsed.getRed(), parsed.getGreen(), parsed.getBlue(), parsed.getAlpha());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unable to parse color '" + color + "'", e);
		}
	}

	@Override
	public IBlockStateSerializer blockStateSerializer() {
		var parser = new BlockStateArgument(CommandBuildContext.simple(VanillaRegistries.createLookup(), FeatureFlags.REGISTRY.allFlags()));
		;
		return new IBlockStateSerializer() {
			@Override
			public BlockState fromId(int id) {
				@SuppressWarnings("deprecation")
				var state = Block.BLOCK_STATE_REGISTRY.byId(id);
				if (state == null)
					throw new IllegalStateException("Unknown blockstate id" + id);
				return state;
			}

			@Override
			public int toId(BlockState state) {
				@SuppressWarnings("deprecation")
				var id = Block.BLOCK_STATE_REGISTRY.getId(state);
				if (id == -1)
					throw new IllegalStateException("Unknown blockstate " + state);
				return id;
			}

			@Override
			public BlockState fromStringOrNull(String string) {
				try {
					return parser.parse(new StringReader(string)).getState();
				} catch (CommandSyntaxException e) {
//					LOGGER.warn("Failed to parse blockstate \"{}\": {}", string, e.getMessage());
					return null;
				}
			}

			@Override
			public String toString(BlockState state) {
				var block = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
				var values = state.getValues();
				if (values.isEmpty())
					return block;
				return values.entrySet().stream()
					.map(e -> e.getKey().getName() + "=" + Util.getPropertyName(e.getKey(), e.getValue()))
					.collect(Collectors.joining(",", block + "[", "]"));
			}
		};
	}

	@Override
	public boolean isPlant(BlockState state) {
		return state.getBlock() instanceof IPlantable;
	}

	@Override
	public void updateClientVisuals(boolean render) {
		NoCubesConfigImpl.Client.updateRender(render);
	}

	@Override
	public boolean trySendC2SRequestUpdateSmoothable(Player player, boolean newValue, BlockState[] states) {
		LOG.debug("toggleLookedAtSmoothable currentServerHasNoCubes={}", NoCubesNetwork.currentServerHasNoCubes);
		if (!NoCubesNetwork.currentServerHasNoCubes) {
			// The server doesn't have NoCubes, directly modify the smoothable state to hackily allow the player to have visuals
			return false;
		} else {
			// We're on a server (possibly singleplayer) with NoCubes installed
			if (C2SRequestUpdateSmoothable.checkPermissionAndNotifyIfUnauthorised(player, Minecraft.getInstance().getSingleplayerServer()))
				// Only send the packet if we have permission, don't send a packet that will be denied
				NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestUpdateSmoothable(newValue, states));
		}
		return true;
	}

	@Override
	public Component clientConfigComponent() {
		var configFile = new File(ConfigTracker.INSTANCE.getConfigFileName(NoCubes.MOD_ID, ModConfig.Type.CLIENT));
		return Component.literal(configFile.getName())
			.withStyle(ChatFormatting.UNDERLINE)
			.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, configFile.getAbsolutePath())));
	}
}
