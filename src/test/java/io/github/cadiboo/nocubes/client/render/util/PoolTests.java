package io.github.cadiboo.nocubes.client.render.util;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Cadiboo
 */
public class PoolTests {

	@Test
	public void addingShouldWorkProperly() {
		final int maxSize = 10;
		Pool<Object> pool = new Pool<>(maxSize);
		for (int i = 0; i < maxSize; i++)
			pool.offer(new Object());
		assertEquals(maxSize, pool.getSize());
		pool.offer(new Object());
		assertEquals(maxSize, pool.getSize());
	}

	@Test
	public void multithreadedAddingShouldWorkProperly() {
		final int maxSize = 100000;
		Pool<Object> pool = new Pool<>(maxSize);
		IntStream.range(0, maxSize).parallel()
			.forEach(i -> pool.offer(new Object()));
		assertEquals(maxSize, pool.getSize());
		pool.offer(new Object());
		assertEquals(maxSize, pool.getSize());
	}

	@Test
	public void multithreadedAddingShouldCreateNoDuplciates() {
		final int maxSize = 100000;
		Pool<Object> pool = new Pool<>(maxSize);
		IntStream.range(0, maxSize).parallel()
			.forEach(i -> pool.offer(new Object()));
		assertEquals(maxSize, pool.getSize());
		Set<Object> set = new HashSet<>();
		IntStream.range(0, maxSize).parallel()
			.forEach(i -> assertTrue(set.add(pool.get())));
	}

}
