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
		Face face = new Face(v0, v1, v2, v3);
		assertEquals(v0, face.v0);
		assertEquals(v1, face.v1);
		assertEquals(v2, face.v2);
		assertEquals(v3, face.v3);
	}

}
