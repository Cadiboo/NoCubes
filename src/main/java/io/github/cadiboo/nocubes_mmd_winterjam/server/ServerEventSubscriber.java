package io.github.cadiboo.nocubes_mmd_winterjam.server;

import io.github.cadiboo.nocubes_mmd_winterjam.util.ModReference;
import net.minecraftforge.fml.common.Mod;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

/**
 * Subscribe to events that should be handled on the PHYSICAL/DEDICATED SERVER in this class
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID, value = CLIENT)
public final class ServerEventSubscriber {

}
