package io.github.cadiboo.nocubes.client.util;

import io.github.cadiboo.nocubes.util.Vec;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Cadiboo
 */
public class VecTests {

	@Test
	public void vertexOrderShouldBeCorrect() {
		Vec vec0 = new Vec(new Vec(0, 1, 2));
		assertEquals(0, vec0.x, 0);
		assertEquals(1, vec0.y, 0);
		assertEquals(2, vec0.z, 0);
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

	@Test
	public void interpolateSanityCheck() {
		int start = 10;
		int end = 20;
		float interpVal = 0.5F;
		int expected = 15;

		Vec startVec = new Vec(start, 0, 0);
		Vec endVec = new Vec(end, 0, 0);

		Vec actual = new Vec().interpolate(interpVal, startVec, endVec);
		assertEquals(expected, actual.x, 0.01);
	}

}
