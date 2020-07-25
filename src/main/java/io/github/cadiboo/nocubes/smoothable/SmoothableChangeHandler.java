package io.github.cadiboo.nocubes.smoothable;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.block.BlockState;


/**
 * Adding/removing a smoothable needs to send a packet to the server.
 * In memory at runtime whether a state is smoothable or not is just stored as a boolean inserted via ASM.
 * When saved (in config.toml), we need to store
 * - The client's personal preference (what they want, what they don't want). Persists between worlds.
 * - The server's config (what is, what isn't). Per-world.
 * <p>
 * For an integrated singleplayer server we need to load the owner's preferences into the server's config
 * This can be done:
 * Each time when the server is started (keeps in sync with changes in other worlds)
 * Once, when the config is first created (unaffected by changes in other worlds)
 * <p>
 * For a dedicated server:
 * OPs (with the right permissions) can add/remove smoothables
 * Non-ops can't
 * <p>
 * For an integrated singleplayer server:
 * The owner can add/remove smoothables
 * <p>
 * For an integrated LAN server:
 * The owner + OPs (with the right permissions) can add/remove smoothables
 * Non-ops can't
 * <p>
 * All of this is made more convoluted by NoCubes being able to run client side only.
 * Therefore we need to detect if NoCubes is installed on the Server and have 2 logic branches if so/if not.
 * <p>
 * This logic only applies to non-singleplayer servers:
 * If nocubes is installed on the server:
 * - Send a packet requesting a smoothable update
 * - Do not update client preferences
 * Otherwise: // Either LAN or Dedicated
 * - Do not send a packet
 * - Add/Remove the state to/from the client's preferences and in-memory
 * <p>
 * The packet should update the server's config + the in-memory flags.
 * In a singleplayer server the in-memory flags are shared between the server and client and used for both rendering and collisions.
 *
 * @author Cadiboo
 */
public interface SmoothableChangeHandler {

	default void addSmoothable(final BlockState state) {
		NoCubes.smoothableHandler.addSmoothable(state);
	}

	default void removeSmoothable(final BlockState state) {
		NoCubes.smoothableHandler.removeSmoothable(state);
	}

	default boolean isSmoothable(final BlockState state) {
		return NoCubes.smoothableHandler.isSmoothable(state);
	}

	default void updateSmoothable(BlockState state, boolean newValue) {
		if (newValue) {
			addStateToWhitelist(state);
			removeStateFromBlacklist(state);
			addSmoothable(state);
		} else {
			removeStateFromWhitelist(state);
			addStateToBlacklist(state);
			removeSmoothable(state);
		}
	}

	void addStateToBlacklist(BlockState state);

	void removeStateFromWhitelist(BlockState state);

	void removeStateFromBlacklist(BlockState state);

	void addStateToWhitelist(BlockState state);

}
