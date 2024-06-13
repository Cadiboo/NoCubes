package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.platform.Services;

public class NoCubes {

	public static void init() {
		Constants.LOG.info("Firebrick: " + Services.PLATFORM.parseColor("firebrick"));
	}
}
