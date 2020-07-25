package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.state.Property;
import net.minecraft.util.registry.Registry;

public class BlockState extends AbstractBlock.AbstractBlockState implements net.minecraftforge.common.extensions.IForgeBlockState {
//	/* Commented out for unit tests to work */ public static final Codec<BlockState> field_235877_b_ = func_235897_a_(Registry.BLOCK, Block::getDefaultState).stable();

	public BlockState(Block p_i231876_1_, ImmutableMap<Property<?>, Comparable<?>> p_i231876_2_, MapCodec<BlockState> p_i231876_3_) {
		super(p_i231876_1_, p_i231876_2_, p_i231876_3_);
	}

	protected BlockState getSelf() {
		return this;
	}

	// Added by ASM at runtime, see nocubes-transformer.js
	public boolean nocubes_isTerrainSmoothable;

}
