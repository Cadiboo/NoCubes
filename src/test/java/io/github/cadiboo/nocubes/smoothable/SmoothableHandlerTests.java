package io.github.cadiboo.nocubes.smoothable;

import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import static net.minecraft.block.AbstractBlock.AbstractBlockState;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Cadiboo
 */
public class SmoothableHandlerTests {

	@Test
	public void asmShouldWork() {
		SmoothableHandler handler = SmoothableHandler.create();
		AbstractBlockState state = createStubbedStateForAsm();
		assertFalse(handler.isSmoothable(state));
		handler.setSmoothable(true, state);
		assertTrue(handler.isSmoothable(state));
		handler.setSmoothable(false, state);
		assertFalse(handler.isSmoothable(state));
	}

	private AbstractBlockState createStubbedStateForAsm() {
		boolean[] smoothableRef = {false};
		AbstractBlockState mockedState = Mockito.mock(AbstractBlockState.class, Mockito.withSettings().extraInterfaces(INoCubesBlockState.class));
		INoCubesBlockState mock = (INoCubesBlockState) mockedState;

		// This code would have been added into the BlockState via ASM
		Mockito.when(mock.isTerrainSmoothable()).thenAnswer(invocation -> smoothableRef[0]);
		Mockito.doAnswer(invocation -> setStubbedStateSmoothable(invocation, smoothableRef)).when(mock).setTerrainSmoothable(true);
		Mockito.doAnswer(invocation -> setStubbedStateSmoothable(invocation, smoothableRef)).when(mock).setTerrainSmoothable(false);

		return mockedState;
	}

	private Object setStubbedStateSmoothable(InvocationOnMock invocation, boolean[] smoothableRef) {
		smoothableRef[0] = invocation.getArgument(0);
		return null;
	}

}
