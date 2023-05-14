package io.github.cadiboo.nocubes.smoothable;

import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Cadiboo
 */
public class SmoothableHandlerTests {

	@Test
	public void asmShouldWork() {
		var handler = SmoothableHandler.create();
		var state = createStubbedStateForAsm();
		assertFalse(handler.isSmoothable(state));
		handler.setSmoothable(true, state);
		assertTrue(handler.isSmoothable(state));
		handler.setSmoothable(false, state);
		assertFalse(handler.isSmoothable(state));
	}

	private BlockStateBase createStubbedStateForAsm() {
		boolean[] smoothableRef = {false};
		var mockedState = Mockito.mock(BlockStateBase.class, Mockito.withSettings().extraInterfaces(INoCubesBlockState.class));
		var mock = (INoCubesBlockState) mockedState;

		// This code would have been added into the BlockState via ASM
		Mockito.when(mock.isSmoothable()).thenAnswer(invocation -> smoothableRef[0]);
		Mockito.doAnswer(invocation -> setStubbedStateSmoothable(invocation, smoothableRef)).when(mock).setSmoothable(true);
		Mockito.doAnswer(invocation -> setStubbedStateSmoothable(invocation, smoothableRef)).when(mock).setSmoothable(false);

		return mockedState;
	}

	private Object setStubbedStateSmoothable(InvocationOnMock invocation, boolean[] smoothableRef) {
		smoothableRef[0] = invocation.getArgument(0);
		return null;
	}

}
