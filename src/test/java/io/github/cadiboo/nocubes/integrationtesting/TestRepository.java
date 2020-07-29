package io.github.cadiboo.nocubes.integrationtesting;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Cadiboo
 */
public final class TestRepository {

	final List<Test> tests = new LinkedList<>();

	public TestRepository() {
//		tests.addAll();
		NoCubesTest.addTests(tests);
	}

}
