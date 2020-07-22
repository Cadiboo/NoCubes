package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.state.Property;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

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
		try {
			final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			Unsafe unsafe = (Unsafe) theUnsafe.get(null);
			return (TestBlockState) unsafe.allocateInstance(TestBlockState.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
