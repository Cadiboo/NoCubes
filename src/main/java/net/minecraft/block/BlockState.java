package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.state.Property;
import net.minecraft.util.registry.Registry;

/**
 * This is a copy and stub of the original minecraft BlockState class with a couple modifications so that NoCubes
 * can compile against it's ASM-added 'nocubes_isTerrainSmoothable' field and the BlockState class in unit tests.
 */
public class BlockState extends AbstractBlock.AbstractBlockState implements net.minecraftforge.common.extensions.IForgeBlockState {
//	/* Commented out for unit tests to work */ public static final Codec<BlockState> CODEC = codec(Registry.BLOCK, Block::defaultBlockState).stable();

	public BlockState(Block p_i231876_1_, ImmutableMap<Property<?>, Comparable<?>> p_i231876_2_, MapCodec<BlockState> p_i231876_3_) {
		super(p_i231876_1_, p_i231876_2_, p_i231876_3_);
	}

	protected BlockState asState() {
		return this;
	}

	// Added by ASM at runtime, see nocubes-transformer.js
	public boolean nocubes_isTerrainSmoothable;
}
