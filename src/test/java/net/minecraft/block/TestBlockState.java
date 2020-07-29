package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.state.Property;
import org.mockito.Mockito;

/**
 * Just until I get a mocking library.
 *
 * @author Cadiboo
 */
public class TestBlockState extends BlockState {

	public TestBlockState(final Block p_i231876_1_, final ImmutableMap<Property<?>, Comparable<?>> p_i231876_2_, final MapCodec<BlockState> p_i231876_3_) {
		super(p_i231876_1_, p_i231876_2_, p_i231876_3_);
	}

	public static TestBlockState create() {
		return Mockito.mock(TestBlockState.class);
	}

}
