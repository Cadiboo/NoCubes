package io.github.cadiboo.nocubes.client.optifine;

import net.minecraftforge.fml.common.EnhancedRuntimeException;

/**
 * @author Cadiboo
 */
public final class OptiFineNotPresentException extends EnhancedRuntimeException {

	@Override
	protected void printStackTrace(final WrappedPrintStream stream) {
		stream.println("OptiFine compatibility methods called when OptiFine isn't installed");
	}

}
