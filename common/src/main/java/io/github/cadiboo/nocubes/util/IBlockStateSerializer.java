package io.github.cadiboo.nocubes.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

/**
 * @author Cadiboo
 */
public interface IBlockStateSerializer {

	BlockState fromId(int id);

	int toId(BlockState state);

	BlockState fromStringOrNull(String string);

	String toString(BlockState state);

	default void writeBlockStatesTo(FriendlyByteBuf buffer, BlockState[] states) {
		var ids = Arrays.stream(states)
			.mapToInt(this::toId)
			.toArray();
		buffer.writeVarIntArray(ids);
	}

	default BlockState[] readBlockStatesFrom(FriendlyByteBuf buffer) {
		return Arrays.stream(buffer.readVarIntArray())
			.mapToObj(this::fromId)
			.toArray(BlockState[]::new);
	}

}
