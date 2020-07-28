package io.github.cadiboo.nocubes.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.state.Property;
import net.minecraft.util.Util;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cadiboo
 */
public interface BlockStateConverter {

	BlockStateArgument PARSER = new BlockStateArgument();

	static BlockState fromId(int id) {
		@SuppressWarnings("deprecation")
		BlockState state = Block.BLOCK_STATE_IDS.getByValue(id);
		if (state == null)
			throw new IllegalStateException("Unknown blockstate id" + id);
		return state;
	}

	static int toId(BlockState state) {
		@SuppressWarnings("deprecation")
		int id = Block.BLOCK_STATE_IDS.get(state);
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
		String block = state.getBlock().getRegistryName().toString();
		final ImmutableMap<Property<?>, Comparable<?>> values = state.getValues();
		if (values.isEmpty())
			return block;
		return values.entrySet().stream()
			.map(e -> e.getKey().getName() + "=" + Util.getValueName(e.getKey(), e.getValue()))
			.collect(Collectors.joining(",", block + "[", "]"));
	}

}
