package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.IProxy;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;

/**
 * The version of IProxy that gets injected into {@link NoCubes#proxy} on a PHYSICAL CLIENT
 *
 * @author Cadiboo
 */
public final class ClientProxy implements IProxy {

	private static final int KEY_CODE_N = 49;

	public static final KeyBinding toggleSmoothableBlockstate = new KeyBinding(MOD_ID + ".key.toggleSmoothableBlockstate", KeyConflictContext.IN_GAME, KEY_CODE_N, "key.categories.misc");

	static {
		ClientRegistry.registerKeyBinding(toggleSmoothableBlockstate);
	}

	@Override
	public String localize(final String unlocalized) {
		return this.localizeAndFormat(unlocalized);
	}

	@Override
	public String localizeAndFormat(final String unlocalized, final Object... args) {
		return I18n.format(unlocalized, args);
	}

	@Override
	public Side getPhysicalSide() {
		return Side.CLIENT;
	}

}
