package cn.yapeteam.yolbi.utils.network;

import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.network.EventFinalPacketSend;
import cn.yapeteam.yolbi.utils.IMinecraft;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class PacketUtil implements IMinecraft {
    public static void sendPacket(Packet<INetHandlerPlayServer> packet) {
        mc.getNetHandler().getNetworkManager().sendPacket(packet);
    }

    public static Method flushOutboundQueue = null, dispatchPacket = null;

    static {
        try {
            flushOutboundQueue = NetworkManager.class.getDeclaredMethod(Mapper.map("net.minecraft.network.NetworkManager", "flushOutboundQueue", "()V", Mapper.Type.Method));
            dispatchPacket = NetworkManager.class.getDeclaredMethod(Mapper.map("net.minecraft.network.NetworkManager", "dispatchPacket", "(Lnet/minecraft/network/Packet;[Lio/netty/util/concurrent/GenericFutureListener;)V", Mapper.Type.Method), Packet.class, GenericFutureListener[].class);

            flushOutboundQueue.setAccessible(true);
            dispatchPacket.setAccessible(true);
        } catch (NoSuchMethodException e) {
            Logger.exception(e);
        }
    }

    public static void sendPacketFinal(Packet<INetHandlerPlayServer> packet) {
        if (flushOutboundQueue != null && dispatchPacket != null) {
            EventFinalPacketSend event = new EventFinalPacketSend(packet);
            if (!PacketUtil.shouldIgnorePacket(packet))
                YolBi.instance.getEventManager().post(event);
            if (event.isCancelled()) return;
            try {
                flushOutboundQueue.invoke(mc.getNetHandler().getNetworkManager());
                dispatchPacket.invoke(mc.getNetHandler().getNetworkManager(), event.getPacket(), null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Logger.exception(e);
            }
        }
    }

    public static void sendPacketNoEvent(Packet<INetHandlerPlayServer> packet) {
        PacketUtil.skip(packet);
        mc.getNetHandler().getNetworkManager().sendPacket(packet);
    }

    public static void sendBlocking(boolean callEvent, boolean place) {
        C08PacketPlayerBlockPlacement packet = place ?
                new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.getHeldItem(), 0, 0, 0) :
                new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem());

        if (callEvent) {
            sendPacket(packet);
        } else {
            sendPacketNoEvent(packet);
        }
    }

    public static ArrayList<Packet<? extends INetHandler>> skip_list = new ArrayList<>();

    public static void skip(Packet<? extends INetHandler> packet) {
        if (!skip_list.contains(packet)) {
            skip_list.add(packet);
        }
    }

    public static void remove(Packet<? extends INetHandler> packet) {
        skip_list.remove(packet);
    }

    public static boolean shouldSkip(Packet<? extends INetHandler> packet) {
        return skip_list.contains(packet);
    }

    public static void releaseUseItem(boolean callEvent) {
        C07PacketPlayerDigging packet = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);

        if (callEvent) {
            sendPacket(packet);
        } else {
            sendPacketNoEvent(packet);
        }
    }

    public static boolean shouldIgnorePacket(Packet<? extends INetHandler> packet) {
        return packet instanceof C00PacketLoginStart || packet instanceof C01PacketEncryptionResponse || packet instanceof C00Handshake || packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing;
    }
}
