package io.github.cadiboo.nocubes.test;

/**
 * @author Cadiboo
 */
public class Test {

	public final String name;
	public final Runnable action;

	public Test(final String name, final Runnable action) {
		this.name = name;
		this.action = action;
	}

}
