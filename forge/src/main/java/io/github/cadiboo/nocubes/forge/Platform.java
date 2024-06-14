package io.github.cadiboo.nocubes.forge;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cadiboo.nocubes.platform.IPlatform;
import io.github.cadiboo.nocubes.util.IBlockStateSerializer;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Collectors;

public class Platform implements IPlatform {
	private static final BlockStateArgument PARSER = new BlockStateArgument(CommandBuildContext.simple(VanillaRegistries.createLookup(), FeatureFlags.REGISTRY.allFlags()));

	@Override
	public IBlockStateSerializer blockStateSerializer() {
		return new IBlockStateSerializer() {
			@Override
			public BlockState fromId(int id) {
				@SuppressWarnings("deprecation")
				var state = Block.BLOCK_STATE_REGISTRY.byId(id);
				if (state == null)
					throw new IllegalStateException("Unknown blockstate id" + id);
				return state;
			}

			@Override
			public int toId(BlockState state) {
				@SuppressWarnings("deprecation")
				var id = Block.BLOCK_STATE_REGISTRY.getId(state);
				if (id == -1)
					throw new IllegalStateException("Unknown blockstate " + state);
				return id;
			}

			@Override
			public BlockState fromStringOrNull(String string) {
				try {
					return PARSER.parse(new StringReader(string)).getState();
				} catch (CommandSyntaxException e) {
//					LOGGER.warn("Failed to parse blockstate \"{}\": {}", string, e.getMessage());
					return null;
				}
			}

			@Override
			public String toString(BlockState state) {
				var block = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
				var values = state.getValues();
				if (values.isEmpty())
					return block;
				return values.entrySet().stream()
					.map(e -> e.getKey().getName() + "=" + Util.getPropertyName(e.getKey(), e.getValue()))
					.collect(Collectors.joining(",", block + "[", "]"));
			}
		};
	}

	@Override
	public boolean isPlant(BlockState state) {
		return state.getBlock() instanceof IPlantable;
	}
}
