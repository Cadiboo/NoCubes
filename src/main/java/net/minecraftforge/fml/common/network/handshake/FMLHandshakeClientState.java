//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraftforge.fml.common.network.handshake;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Futures;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ConfigHandshakeHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage.ClientHello;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage.HandshakeAck;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage.HandshakeReset;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage.ModList;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage.RegistryData;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage.ServerHello;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.network.internal.FMLMessage.CompleteHandshake;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.ForgeRegistry.Snapshot;

import javax.annotation.Nullable;

enum FMLHandshakeClientState implements IHandshakeState<FMLHandshakeClientState> {
    START { // 1
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeClientState> cons) {
            cons.accept(HELLO);
            NetworkDispatcher dispatcher = (NetworkDispatcher)ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
            dispatcher.clientListenForServerHandshake();
        }
    },
    HELLO { // 3
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeClientState> cons) {
            boolean isVanilla = msg == null;
            if (isVanilla) {
                cons.accept(DONE);
            } else {
                cons.accept(WAITINGSERVERDATA);
            }

            ctx.writeAndFlush(FMLHandshakeMessage.makeCustomChannelRegistration(NetworkRegistry.INSTANCE.channelNamesFor(Side.CLIENT)));
            if (isVanilla) {
                NetworkDispatcher dispatcherx = (NetworkDispatcher)ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
                dispatcherx.abortClientHandshake("VANILLA");
            } else {
                ServerHello serverHelloPacket = (ServerHello)msg;
                FMLLog.log.info("Server protocol version {}", Integer.toHexString(serverHelloPacket.protocolVersion()));
                if (serverHelloPacket.protocolVersion() > 1) {
                    NetworkDispatcher dispatcher = (NetworkDispatcher)ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
                    dispatcher.setOverrideDimension(serverHelloPacket.overrideDim());
                }

                ctx.writeAndFlush(new ClientHello()).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                ctx.writeAndFlush(new ModList(Loader.instance().getActiveModList())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
        }
    },
    WAITINGSERVERDATA { // 5
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeClientState> cons) {
            String modRejections = FMLNetworkHandler.checkModList((ModList)msg, Side.SERVER);
            if (modRejections != null) {
                cons.accept(ERROR);
                NetworkDispatcher dispatcher = (NetworkDispatcher)ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
                dispatcher.rejectHandshake(modRejections);
            } else {
                if (!(Boolean)ctx.channel().attr(NetworkDispatcher.IS_LOCAL).get()) {
                    cons.accept(WAITINGSERVERCOMPLETE);
                } else {
                    cons.accept(PENDINGCOMPLETE);
                }

                ctx.writeAndFlush(new HandshakeAck(this.ordinal())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
        }
    },
    WAITINGSERVERCOMPLETE { // 7
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeClientState> cons) {
            RegistryData pkt = (RegistryData)msg;
            Map<ResourceLocation, Snapshot> snap1 = (Map)ctx.channel().attr(NetworkDispatcher.FML_GAMEDATA_SNAPSHOT).get();
            if (snap1 == null) {
                snap1 = Maps.newHashMap();
                ctx.channel().attr(NetworkDispatcher.FML_GAMEDATA_SNAPSHOT).set(snap1);
            }

            final Map<ResourceLocation, Snapshot> snap = snap1;

            Snapshot entry = new Snapshot();
            entry.ids.putAll(pkt.getIdMap());
            entry.dummied.addAll(pkt.getDummied());
            entry.overrides.putAll(pkt.getOverrides());
            ((Map)snap).put(pkt.getName(), entry);
            if (pkt.hasMore()) {
                cons.accept(WAITINGSERVERCOMPLETE);
                FMLLog.log.debug("Received Mod Registry mapping for {}: {} IDs {} overrides {} dummied", pkt.getName(), entry.ids.size(), entry.overrides.size(), entry.dummied.size());
            } else {
                ctx.channel().attr(NetworkDispatcher.FML_GAMEDATA_SNAPSHOT).set(null);
                Multimap<ResourceLocation, ResourceLocation> locallyMissing = (Multimap)Futures.getUnchecked(Minecraft.getMinecraft().addScheduledTask(() -> {
                    return GameData.injectSnapshot(snap, false, false);
                }));
                if (!locallyMissing.isEmpty()) {
                    cons.accept(ERROR);
                    NetworkDispatcher dispatcher = (NetworkDispatcher)ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
                    dispatcher.rejectHandshake("Fatally missing registry entries");
                    FMLLog.log.fatal("Failed to connect to server: there are {} missing registry items", locallyMissing.size());
                    locallyMissing.asMap().forEach((key, value) -> {
                        FMLLog.log.debug("Missing {} Entries: {}", key, value);
                    });
                } else {
                    // NoCubes - Go to PENDINGCONFIG instead of PENDINGCOMPLETE
//                    cons.accept(PENDINGCOMPLETE);
                    cons.accept(PENDINGCONFIG);
                    ctx.writeAndFlush(new HandshakeAck(this.ordinal())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                }
            }
        }
    },
    // Config here
    PENDINGCOMPLETE { // 11
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeClientState> cons) {
            cons.accept(COMPLETE);
            ctx.writeAndFlush(new HandshakeAck(this.ordinal())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    },
    COMPLETE { // 13
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeClientState> cons) {
            cons.accept(DONE);
            NetworkDispatcher dispatcher = (NetworkDispatcher)ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
            dispatcher.completeClientHandshake();
            CompleteHandshake complete = new CompleteHandshake(Side.CLIENT);
            ctx.fireChannelRead(complete);
            ctx.writeAndFlush(new HandshakeAck(this.ordinal())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    },
    DONE { // 15
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeClientState> cons) {
            if (msg instanceof HandshakeReset) {
                cons.accept(HELLO);
                Minecraft.getMinecraft().addScheduledTask(GameData::revertToFrozen);
            }

        }
    },
    ERROR {
        public void accept(ChannelHandlerContext ctx, FMLHandshakeMessage msg, Consumer<? super FMLHandshakeClientState> cons) {
        }
    },
    // NoCubes Start
    PENDINGCONFIG { // 9

	    private boolean hasRecievedHandshakeAckPacket = false;

        @Override
        public void accept(final ChannelHandlerContext ctx, @Nullable final FMLHandshakeMessage msg, final Consumer<? super FMLHandshakeClientState> cons) {
            // Receive a HandshakeAck, then n S2CConfigData, then a HandshakeAck
            if (msg instanceof HandshakeAck) {
	            if (!hasRecievedHandshakeAckPacket) {
                    hasRecievedHandshakeAckPacket = true;
		            return;
	            }
                hasRecievedHandshakeAckPacket = false; // reset for next connection
	            cons.accept(PENDINGCOMPLETE);
                ctx.writeAndFlush(new HandshakeAck(this.ordinal())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            } else {
                FMLHandshakeMessage.S2CConfigData pkt = (FMLHandshakeMessage.S2CConfigData) msg;
                NoCubes.LOGGER.info("Received Config: {}", pkt.getFileName());
                ConfigHandshakeHandler.handleConfigSync(pkt);
            }
        }
    }
    // NoCubes End
    ;

    private FMLHandshakeClientState() {
    }
}
