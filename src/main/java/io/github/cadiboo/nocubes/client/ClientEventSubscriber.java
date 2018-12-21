package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.ModReference;
import net.minecraftforge.fml.common.Mod;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

/**
 * Subscribe to events that should be handled on the PHYSICAL CLIENT in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

}
