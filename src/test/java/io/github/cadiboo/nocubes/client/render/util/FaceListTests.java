package io.github.cadiboo.nocubes.client.render.util;

import com.google.common.collect.Sets;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Cadiboo
 */
public class FaceListTests {

	@Test
	public void cleanup() {
		FaceList faces = null;
		for (int i = 0; i < 10; i++) {
			if (faces != null) {
				for (final Face face : faces) {
					for (final Vec vertex : face.getVertices())
						vertex.close();
					face.close();
				}
				faces.close();
			}
			faces = FaceList.of();
			assertTrue(faces.isEmpty());
			final Vec v0 = Vec.of(0, 0, 0);
			final Vec v1 = Vec.of(1, 1, 1);
			final Vec v2 = Vec.of(2, 2, 2);
			final Vec v3 = Vec.of(3, 3, 3);
			assertEquals(1, Sets.newHashSet(v0, v0, v0, v0).size());
			assertEquals(4, Sets.newHashSet(v0, v1, v2, v3).size());
			faces.add(Face.of(v0, v1, v2, v3));
			final Vec v4 = Vec.of(4, 4, 4);
			final Vec v5 = Vec.of(5, 5, 5);
			final Vec v6 = Vec.of(6, 6, 6);
			final Vec v7 = Vec.of(7, 7, 7);
			assertEquals(4, Sets.newHashSet(v4, v5, v6, v7).size());
			faces.add(Face.of(v4, v5, v6, v7));
			assertEquals(2, faces.size());
			assertEquals(2, Sets.newHashSet(faces).size());
		}
	}

}
