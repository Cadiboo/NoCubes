package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
/**
 * This is a copy and stub of the original minecraft BlockState class with a couple modifications so that NoCubes
 * can compile against it's ASM-added 'nocubes_isTerrainSmoothable' field and the BlockState class in unit tests.
 */
public class BlockState extends BlockBehaviour.BlockStateBase implements net.minecraftforge.common.extensions.IForgeBlockState {
//	/* Commented out for unit tests to work */ public static final Codec<BlockState> CODEC = codec(Registry.BLOCK, Block::defaultBlockState).stable();

	public BlockState(Block p_61042_, ImmutableMap<Property<?>, Comparable<?>> p_61043_, MapCodec<BlockState> p_61044_) {
		super(p_61042_, p_61043_, p_61044_);
	}

	protected BlockState asState() {
		return this;
	}

	// Added by ASM at runtime, see nocubes-transformer.js
	public boolean nocubes_isTerrainSmoothable;
}
