package io.github.cadiboo.nocubes.client.optifine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This compatibility system isn't perfect and leads to a lot of code duplication
 * However, it's a lot better than the previous system.
 */
public class OptiFineCompatibility {

	private static final OptiFineProxy[] PROXIES = {
		new HD_U_H5(),
		new HD_U_G8(),
	};
	private static volatile OptiFineProxy instance;

	public static OptiFineProxy proxy() {
		if (instance == null) {
			synchronized (OptiFineCompatibility.class) {
				if (instance == null) {
					var log = LogManager.getLogger("NoCubes OptiFine Compatibility");
					instance = createProxy(log);
					log.info("Using {} proxy", instance.getClass().getSimpleName());
				}
			}
		}
		return instance;
	}

	private static OptiFineProxy createProxy(Logger log) {
		for (var proxy : PROXIES) {
			var because = proxy.notUsableBecause();
			if (because == null)
				return proxy;
			log.info("{} proxy not usable because {}", proxy.getClass().getSimpleName(), because);
		}
		return new Dummy();
	}
}
