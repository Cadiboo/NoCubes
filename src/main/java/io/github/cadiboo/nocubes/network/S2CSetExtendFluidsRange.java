package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public final class S2CSetExtendFluidsRange {

	private final ExtendFluidsRange newRange;

	public S2CSetExtendFluidsRange(final ExtendFluidsRange extendFluidsRange) {
		this.newRange = extendFluidsRange;
	}

	public static void encode(final S2CSetExtendFluidsRange msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.newRange.ordinal());
	}

	public static S2CSetExtendFluidsRange decode(final PacketBuffer packetBuffer) {
		return new S2CSetExtendFluidsRange(ExtendFluidsRange.VALUES[packetBuffer.readInt()]);
	}

	public static void handle(final S2CSetExtendFluidsRange msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			final ExtendFluidsRange newRange = msg.newRange;
			Config.extendFluidsRange = newRange;
			Minecraft.getInstance().player.sendMessage(new TranslationTextComponent(MOD_ID + ".setExtendFluidsRange", newRange));
			ClientUtil.tryReloadRenderers();
		}));
		context.setPacketHandled(true);
	}

}
