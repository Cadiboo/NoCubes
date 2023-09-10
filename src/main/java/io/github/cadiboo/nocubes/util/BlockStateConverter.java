package io.github.cadiboo.nocubes.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * @author Cadiboo
 */
public interface BlockStateConverter {

	static final Logger LOGGER = LogManager.getLogger("NoCubes Config");

	static IBlockState fromId(int id) {
		IBlockState state = Block.getStateById(id);
		if (state == null || state.getBlock() == Blocks.AIR)
			throw new IllegalStateException("Unknown blockstate id" + id);
		return state;
	}

	static int toId(IBlockState state) {
		int id = Block.getStateId(state);
		if (id == -1)
			throw new IllegalStateException("Unknown blockstate " + state);
		return id;
	}

	static IBlockState fromStringOrNull(String string) {
		try {
			final String[] splitBlockStateString = StringUtils.split(string, "[");
			final String blockString = splitBlockStateString[0];
			final String variantsString;
			if (splitBlockStateString.length == 1) {
				variantsString = "default";
			} else if (splitBlockStateString.length == 2) {
				variantsString = StringUtils.reverse(StringUtils.reverse(StringUtils.split(string, "[")[1]).replaceFirst("]", ""));
			} else {
				LOGGER.error("Block/BlockState Parsing error for \"" + string + "\"");
				return null;
			}

			final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockString));
			if (block == null || block == Blocks.AIR) {
				LOGGER.error("Block Parsing error for \"" + blockString + "\". Block does not exist!");
				return null;
			}
			try {
				return CommandBase.convertArgToBlockState(block, variantsString);
			} catch (NumberInvalidException e) {
				LOGGER.error("BlockState Parsing error " + e + " for \"" + variantsString + "\". Invalid Number!");
				return null;
			} catch (InvalidBlockStateException e) {
				LOGGER.error("BlockState Parsing error " + e + " for \"" + variantsString + "\". Invalid BlockState!");
				return null;
			}
		} catch (Exception e) {
			LOGGER.error("Failed to parse blockstate \"" + string + "\"!", e);
			return null;
		}
	}

	static String toString(IBlockState state) {
		return state.toString();
	}

	static void writeBlockStatesTo(PacketBuffer buffer, IBlockState[] states) {
		int[] ids = Arrays.stream(states)
			.mapToInt(BlockStateConverter::toId)
			.toArray();
		buffer.writeVarIntArray(ids);
	}

	static IBlockState[] readBlockStatesFrom(PacketBuffer buffer) {
		return Arrays.stream(buffer.readVarIntArray())
			.mapToObj(BlockStateConverter::fromId)
			.toArray(IBlockState[]::new);
	}

}
