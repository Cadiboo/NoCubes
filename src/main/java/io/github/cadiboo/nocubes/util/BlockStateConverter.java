package io.github.cadiboo.nocubes.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Cadiboo
 */
public interface BlockStateConverter {

	BlockStateArgument PARSER = new BlockStateArgument();

	static BlockState fromId(int id) {
		@SuppressWarnings("deprecation")
		var state = Block.BLOCK_STATE_REGISTRY.byId(id);
		if (state == null)
			throw new IllegalStateException("Unknown blockstate id" + id);
		return state;
	}

	static int toId(BlockState state) {
		@SuppressWarnings("deprecation")
		var id = Block.BLOCK_STATE_REGISTRY.getId(state);
		if (id == -1)
			throw new IllegalStateException("Unknown blockstate " + state);
		return id;
	}

	static BlockState fromStringOrNull(String string) {
		try {
			return PARSER.parse(new StringReader(string)).getState();
		} catch (CommandSyntaxException e) {
//			LOGGER.warn("Failed to parse blockstate \"{}\": {}", string, e.getMessage());
			return null;
		}
	}

	static String toString(BlockState state) {
		var block = state.getBlock().getRegistryName().toString();
		var values = state.getValues();
		if (values.isEmpty())
			return block;
		return values.entrySet().stream()
			.map(e -> e.getKey().getName() + "=" + Util.getPropertyName(e.getKey(), e.getValue()))
			.collect(Collectors.joining(",", block + "[", "]"));
	}

	static void writeBlockStatesTo(PacketBuffer buffer, BlockState[] states) {
		var ids = Arrays.stream(states)
			.mapToInt(BlockStateConverter::toId)
			.toArray();
		buffer.writeVarIntArray(ids);
	}

	static BlockState[] readBlockStatesFrom(PacketBuffer buffer) {
		return Arrays.stream(buffer.readVarIntArray())
			.mapToObj(BlockStateConverter::fromId)
			.toArray(BlockState[]::new);
	}

}
