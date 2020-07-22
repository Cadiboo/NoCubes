package io.github.cadiboo.nocubes.client.render.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Cadiboo
 */
public class VecTest {

	@Test
	public void vertexOrderShouldBeCorrect() {
		Vec unpooled = new Vec(0, 1, 2);
		assertEquals(0, unpooled.x, 0);
		assertEquals(1, unpooled.y, 0);
		assertEquals(2, unpooled.z, 0);

		Vec.POOL.add(new Vec(-1, -1, -1));
		Vec pooled = Vec.of(0, 1, 2);
		assertEquals(0, pooled.x, 0);
		assertEquals(1, pooled.y, 0);
		assertEquals(2, pooled.z, 0);
	}

}
