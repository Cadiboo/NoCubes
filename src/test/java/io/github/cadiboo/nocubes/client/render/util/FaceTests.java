package io.github.cadiboo.nocubes.client.render.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Cadiboo
 */
public class FaceTests {

	Vec v0 = new Vec(0, 0, 0);
	Vec v1 = new Vec(1, 1, 1);
	Vec v2 = new Vec(2, 2, 2);
	Vec v3 = new Vec(3, 3, 3);

	@Test
	public void vertexOrderShouldBeCorrect() {
		Face unpooled = new Face(v0, v1, v2, v3);
		assertEquals(v0, unpooled.v0);
		assertEquals(v1, unpooled.v1);
		assertEquals(v2, unpooled.v2);
		assertEquals(v3, unpooled.v3);

		final Face face = new Face(null, null, null, null);
		Face.POOL.offer(face);
		Face pooled = Face.of(v0, v1, v2, v3);
		assertEquals(face, pooled);
		assertEquals(v0, pooled.v0);
		assertEquals(v1, pooled.v1);
		assertEquals(v2, pooled.v2);
		assertEquals(v3, pooled.v3);
	}

}
