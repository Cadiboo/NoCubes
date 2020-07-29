package io.github.cadiboo.nocubes.client.render.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author Cadiboo
 */
public class VecTests {

	@Test
	public void vertexOrderShouldBeCorrect() {
		Vec unpooled = new Vec(0, 1, 2);
		assertEquals(0, unpooled.x, 0);
		assertEquals(1, unpooled.y, 0);
		assertEquals(2, unpooled.z, 0);

		final Vec vec = new Vec(-1, -1, -1);
		Vec.POOL.offer(vec);
		Vec pooled = Vec.of(0, 1, 2);
		assertEquals(vec, pooled);
		assertEquals(0, pooled.x, 0);
		assertEquals(1, pooled.y, 0);
		assertEquals(2, pooled.z, 0);
	}

	@Test
	public void poolSanityCheck() {
		int max = 3000;
		for (int i = 0; i < max; i++) {
			int x = 0;
			int y = 0;
			int z = 0;
			Vec v0 = Vec.of(x + 0.5, y - 1, z + 0.5);
			Vec v1 = Vec.of(x - 0.5, y - 1, z + 0.5);
			Vec v2 = Vec.of(x - 0.5, y - 1, z - 0.5);
			Vec v3 = Vec.of(x + 0.5, y - 1, z - 0.5);
			assertNotSame(v0, v1);
			assertNotSame(v0, v2);
			assertNotSame(v0, v3);
			assertNotSame(v1, v2);
			assertNotSame(v1, v3);
			assertNotSame(v2, v3);

			assertNotEquals(v0.x, v1.x, 0);
			assertNotEquals(v0.x, v2.x, 0);
			assertEquals(v0.x, v3.x, 0);

			assertEquals(v0.y, v1.y, 0);
			assertEquals(v0.y, v2.y, 0);
			assertEquals(v0.y, v3.y, 0);

			assertEquals(v0.z, v1.z, 0);
			assertNotEquals(v0.z, v2.z, 0);
			assertNotEquals(v0.z, v3.z, 0);
		}
	}

	@Test
	public void offsetSanityCheck() {
		final int x = -1;
		final int y = -2;
		final int z = -3;
		final Vec vec = new Vec(x, y, z);
		assertEquals(vec.x, x, 0);
		assertEquals(vec.y, y, 0);
		assertEquals(vec.z, z, 0);
		final int offsetX = 3;
		final int offsetY = 11;
		final int offsetZ = 23;
		vec.add(offsetX, offsetY, offsetZ);
		assertEquals(vec.x, x + offsetX, 0);
		assertEquals(vec.y, y + offsetY, 0);
		assertEquals(vec.z, z + offsetZ, 0);
	}

}
