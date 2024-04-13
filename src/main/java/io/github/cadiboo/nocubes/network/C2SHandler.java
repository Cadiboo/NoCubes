package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class C2SHandler {

	/**
	 * From the minecraft wiki.
	 * 1. Ops can bypass spawn protection.
	 * 2. Ops can use /clear, /difficulty, /effect, /gamemode, /gamerule, /give, /summon, /setblock and /tp, and can edit command blocks.
	 * 3. Ops can use /ban, /deop, /whitelist, /kick, and /op.
	 * 4. Ops can use /stop.
	 */
	public static final int REQUIRED_PERMISSION_LEVEL = 2;

	public interface C2SRequestUpdateSmoothableReplier {
		void senderClientIsOutOfSyncNotifyThemOfNewValue(ServerPlayer sender, boolean newValue, BlockState[] states);
		void updateAllClients(boolean newValue, BlockState[] states);
		void enqueueWork(Runnable work);
	}

	public static void onC2SRequestUpdateSmoothable(
		boolean newValue, BlockState[] states,
		ServerPlayer sender,
		C2SRequestUpdateSmoothableReplier reply
	) {
		Objects.requireNonNull(sender, "Command sender was null");
		if (checkPermissionAndNotifyIfUnauthorised(sender, sender.server)) {
			var statesToUpdate = Arrays.stream(states)
				.filter(s -> NoCubes.smoothableHandler.isSmoothable(s) != newValue)
				.toArray(BlockState[]::new);
			// Guards against useless config reload and/or someone spamming these packets to the server and the server spamming all clients
			if (statesToUpdate.length == 0)
				// Somehow the client is out of sync, just notify them
				reply.senderClientIsOutOfSyncNotifyThemOfNewValue(sender, newValue, states);
			else {
				reply.enqueueWork(() -> NoCubesConfig.Server.updateSmoothable(newValue, statesToUpdate));
				// Send back update to all clients
				reply.updateAllClients(newValue, statesToUpdate);
			}
		}
	}

	public static boolean checkPermissionAndNotifyIfUnauthorised(Player player, @Nullable MinecraftServer connectedToServer) {
		if (connectedToServer != null && connectedToServer.isSingleplayerOwner(player.getGameProfile()))
			return true;
		if (player.hasPermissions(REQUIRED_PERMISSION_LEVEL))
			return true;
		ModUtil.warnPlayer(player, NoCubes.MOD_ID + ".command.changeSmoothableNoPermission");
		return false;
	}
}
