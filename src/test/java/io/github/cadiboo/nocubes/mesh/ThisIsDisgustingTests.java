package io.github.cadiboo.nocubes.mesh;

import com.google.common.collect.Streams;
import io.github.cadiboo.nocubes.mesh.SurfaceNets.ThisIsDisgusting;
import org.junit.Test;

/**
 * @author Cadiboo
 */
public class ThisIsDisgustingTests {

	@Test
	public void test() {
		ThisIsDisgusting<String> disgusting = new ThisIsDisgusting<String>() {
			@Override
			protected void generate() {
				yield("Hello");
				yield("world!");
				yield("");
				yield("What");
				yield("I've");
				yield("done");
				yield("is");
				yield("so");
				yield("disgusting");
			}
		};
		Streams.stream(disgusting)
			.forEach(System.out::println);
	}

}
