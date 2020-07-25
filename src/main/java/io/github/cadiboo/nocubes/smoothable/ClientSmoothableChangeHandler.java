package io.github.cadiboo.nocubes.smoothable;

import net.minecraft.block.BlockState;

public interface ClientSmoothableChangeHandler extends SmoothableChangeHandler {

	boolean serverHasNoCubes();

	default void playerRequestedToggleSmoothable(BlockState state) {
		boolean newValue = !isSmoothable(state);
		if (serverHasNoCubes()) {
			sendUpdateSmoothableRequestToServer(state, newValue);
		} else {
			// Server doesn't have NoCubes, allow the player to have visuals anyway
			updateSmoothable(state, newValue);
		}
	}

	void sendUpdateSmoothableRequestToServer(BlockState state, boolean newValue);

}
