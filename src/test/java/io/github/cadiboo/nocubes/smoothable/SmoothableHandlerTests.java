package io.github.cadiboo.nocubes.smoothable;

import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import static org.junit.Assert.*;

/**
 * @author Cadiboo
 */
public class SmoothableHandlerTests {

	@Test
	public void asmShouldNotBeUsedIfCoremodFails() {
		var state = Mockito.mock(BlockStateBase.class);
		var handler = SmoothableHandler.create(state);
		assertEquals(SmoothableHandler.Set.class, handler.getClass());
	}

	@Test
	public void asmShouldBeUsedIfCoremodSucceeds() {
		var state = createStubbedStateForASM();
		var handler = SmoothableHandler.create(state);
		assertEquals(SmoothableHandler.ASM.class, handler.getClass());
	}

	@Test
	public void asmShouldWork() {
		shouldWork(new SmoothableHandler.ASM(), createStubbedStateForASM());
	}

	@Test
	public void setShouldWork() {
		shouldWork(new SmoothableHandler.Set(), Mockito.mock(BlockStateBase.class));
	}

	private void shouldWork(SmoothableHandler handler, BlockStateBase state) {
		assertFalse(handler.isSmoothable(state));
		handler.setSmoothable(true, state);
		assertTrue(handler.isSmoothable(state));
		handler.setSmoothable(false, state);
		assertFalse(handler.isSmoothable(state));
	}

	private BlockStateBase createStubbedStateForASM() {
		boolean[] smoothableRef = {false};
		var mockedState = Mockito.mock(BlockStateBase.class, Mockito.withSettings().extraInterfaces(INoCubesBlockState.class));
		var mock = (INoCubesBlockState) mockedState;

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
