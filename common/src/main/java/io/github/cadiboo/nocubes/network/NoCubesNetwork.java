package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.BlockStateSerializer;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoCubesNetwork {

	/**
	 * From the minecraft wiki.
	 * 1. Ops can bypass spawn protection.
	 * 2. Ops can use /clear, /difficulty, /effect, /gamemode, /gamerule, /give, /summon, /setblock and /tp, and can edit command blocks.
	 * 3. Ops can use /ban, /deop, /whitelist, /kick, and /op.
	 * 4. Ops can use /stop.
	 */
	public static final int REQUIRED_PERMISSION_LEVEL = 2;

	public static final String NETWORK_PROTOCOL_VERSION = "2";

	public static boolean checkPermissionAndNotifyIfUnauthorised(Player player, @Nullable MinecraftServer connectedToServer) {
		if (connectedToServer != null && connectedToServer.isSingleplayerOwner(player.getGameProfile()))
			return true;
		if (player.hasPermissions(REQUIRED_PERMISSION_LEVEL))
			return true;
		ModUtil.warnPlayer(player, NoCubes.MOD_ID + ".command.changeSmoothableNoPermission");
		return false;
	}

	@FunctionalInterface
	public interface SendS2CUpdateSmoothable {
		void send(@Nullable ServerPlayer playerIfNotNullElseEveryone, boolean newValue, BlockState[] states);
	}

	public static void handleC2SRequestUpdateSmoothable(
		ServerPlayer sender,
		boolean newValue, BlockState[] states,
		Consumer<Runnable> enqueueWork,
		SendS2CUpdateSmoothable network
	) {
		if (NoCubesNetwork.checkPermissionAndNotifyIfUnauthorised(sender, sender.server)) {
			var statesToUpdate = Arrays.stream(states)
				.filter(s -> NoCubes.smoothableHandler.isSmoothable(s) != newValue)
				.toArray(BlockState[]::new);
			// Guards against useless config reload and/or someone spamming these packets to the server and the server spamming all clients
			if (statesToUpdate.length == 0)
				// Somehow the client is out of sync, just notify them
				network.send(sender, newValue, states);
			else {
				enqueueWork.accept(() -> ModUtil.platform.updateServerConfigSmoothable(newValue, statesToUpdate));
				// Send back update to all clients
				network.send(null, newValue, statesToUpdate);
			}
		}
	}

	public interface Serializer {

		static void encodeS2CUpdateServerConfig(FriendlyByteBuf buffer, byte[] data) {
			buffer.writeByteArray(data);
		}

		static <T> T decodeS2CUpdateServerConfig(FriendlyByteBuf buffer, Function<byte[], T> constructor) {
			var data = buffer.readByteArray();
			return constructor.apply(data);
		}

		static void encodeUpdateSmoothable(FriendlyByteBuf buffer, boolean newValue, BlockState[] states) {
			buffer.writeBoolean(newValue);
			BlockStateSerializer.writeBlockStatesTo(buffer, states);
		}

		static <T> T decodeUpdateSmoothable(FriendlyByteBuf buffer, UpdateSmoothableConstructor<T> constructor) {
			var newValue = buffer.readBoolean();
			var states = BlockStateSerializer.readBlockStatesFrom(buffer);
			return constructor.apply(newValue, states);
		}

		interface UpdateSmoothableConstructor<T> {
			T apply(boolean newValue, BlockState[] states);
		}
	}
}
