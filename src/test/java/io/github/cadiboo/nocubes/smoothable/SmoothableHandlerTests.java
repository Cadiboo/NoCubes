package io.github.cadiboo.nocubes.smoothable;

import io.github.cadiboo.nocubes.TestUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Cadiboo
 */
public class SmoothableHandlerTests {

	@Test
	public void asmShouldWork() {
		final SmoothableHandler asm = SmoothableHandler.create(TestUtils.TEST_1);
		assertEquals(asm.getClass(), SmoothableHandler.ASM.class);
		shouldWork(asm);
	}

	@Test
	public void setShouldWork() {
		shouldWork(new SmoothableHandler.Set());
	}

	private void shouldWork(final SmoothableHandler handler) {
		var test = TestUtils.TEST_1;
		assertFalse(handler.isSmoothable(test));
		handler.addSmoothable(test);
		assertTrue(handler.isSmoothable(test));
		handler.removeSmoothable(test);
		assertFalse(handler.isSmoothable(test));
	}

}
