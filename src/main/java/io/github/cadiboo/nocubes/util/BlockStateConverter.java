package io.github.cadiboo.nocubes.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * @author Cadiboo
 */
public interface BlockStateConverter {

//	BlockStateArgument PARSER = new BlockStateArgument();

	static IBlockState fromId(int id) {
		@SuppressWarnings("deprecation")
		IBlockState state = Block.BLOCK_STATE_IDS.getByValue(id);
		if (state == null)
			throw new IllegalStateException("Unknown blockstate id" + id);
		return state;
	}

	static int toId(IBlockState state) {
		@SuppressWarnings("deprecation")
//		int id = Block.BLOCK_STATE_IDS.getId(state);
		int id = Block.BLOCK_STATE_IDS.get(state);
		if (id == -1)
			throw new IllegalStateException("Unknown blockstate " + state);
		return id;
	}

	@Nullable
	static IBlockState fromStringOrNull(String string) {
//		try {
//			return PARSER.parse(new StringReader(string)).getState();
//		} catch (CommandSyntaxException e) {
//			LOGGER.warn("Failed to parse blockstate \"{}\": {}", string, e.getMessage());
//			return null;
//		}
		try {
			final String[] splitBlockStateString = StringUtils.split(string, "[");
			final String blockString = splitBlockStateString[0];
			final String variantsString;
			if (splitBlockStateString.length == 1)
				variantsString = "default";
			else if (splitBlockStateString.length == 2)
				variantsString = StringUtils.reverse(StringUtils.reverse(StringUtils.split(string, "[")[1]).replaceFirst("]", ""));
			else {
				getLogger().error("Block/BlockState Parsing error for \"" + string + "\"");
				return null;
			}

			final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockString));
			if (block == null || block == Blocks.AIR) {
				getLogger().error("Block Parsing error for \"" + blockString + "\". Block does not exist!");
				return null;
			}
			try {
				return CommandBase.convertArgToBlockState(block, variantsString);
			} catch (NumberInvalidException e) {
				getLogger().error("BlockState Parsing error " + e + " for \"" + variantsString + "\". Invalid Number!");
				return null;
			} catch (InvalidBlockStateException e) {
				getLogger().error("BlockState Parsing error " + e + " for \"" + variantsString + "\". Invalid BlockState!");
				return null;
			}
		} catch (Exception e) {
			getLogger().error("Failed to parse blockstate \"" + string + "\"!", e);
			return null;
		}
	}

	static String toString(IBlockState state) {
//		String block = state.getBlock().getRegistryName().toString();
//		final ImmutableMap<Property<?>, Comparable<?>> values = state.getValues();
//		if (values.isEmpty())
//			return block;
//		return values.entrySet().stream()
//			.map(e -> e.getKey().getName() + "=" + Util.getValueName(e.getKey(), e.getValue()))
//			.collect(Collectors.joining(",", block + "[", "]"));
		return state.toString();
	}

	static Set<IBlockState> fromStrings(Collection<? extends String> stateStrings) {
		return stateStrings.parallelStream()
				.map(BlockStateConverter::fromStringOrNull)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}
}
