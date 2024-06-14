package io.github.cadiboo.nocubes.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Cadiboo
 */
public interface BlockStateSerializer {

	BlockStateArgument PARSER = new BlockStateArgument(CommandBuildContext.simple(VanillaRegistries.createLookup(), FeatureFlags.REGISTRY.allFlags()));

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
		var block = ModUtil.platform.getRegistryName(state.getBlock()).toString();
		var values = state.getValues();
		if (values.isEmpty())
			return block;
		return values.entrySet().stream()
			.map(e -> e.getKey().getName() + "=" + Util.getPropertyName(e.getKey(), e.getValue()))
			.collect(Collectors.joining(",", block + "[", "]"));
	}

	static void writeBlockStatesTo(FriendlyByteBuf buffer, BlockState[] states) {
		var ids = Arrays.stream(states)
			.mapToInt(BlockStateSerializer::toId)
			.toArray();
		buffer.writeVarIntArray(ids);
	}

	static BlockState[] readBlockStatesFrom(FriendlyByteBuf buffer) {
		return Arrays.stream(buffer.readVarIntArray())
			.mapToObj(BlockStateSerializer::fromId)
			.toArray(BlockState[]::new);
	}

}
