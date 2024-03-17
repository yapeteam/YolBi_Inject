package cn.yapeteam.yolbi.handler.packet;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.Priority;
import cn.yapeteam.yolbi.event.impl.network.EventPacketSend;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.utils.IMinecraft;
import cn.yapeteam.yolbi.utils.network.PacketUtil;
import lombok.Getter;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class PacketDelayHandler implements IMinecraft {

    private long pingDelay;
    private long otherDelay;

    private boolean delayingPing;
    private boolean delayingOther;

    private boolean clearedPackets;

    private final CopyOnWriteArrayList<DelayedPacket> delayedPackets = new CopyOnWriteArrayList<>();

    public PacketDelayHandler() {
        YolBi.instance.getEventManager().register(this);
    }

    @Listener(Priority.MAX)
    public void onSend(EventPacketSend event) {
        if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 5) {
            if (!clearedPackets) {
                delayedPackets.clear();
                stopAll();
                clearedPackets = true;
            }
        } else {
            clearedPackets = false;
        }

        if (!event.isCancelled()) {
            if (isPing(event.getPacket())) {
                if (delayingPing) {
                    event.setCancelled(true);
                    delayedPackets.add(new DelayedPacket(event.getPacket()));
                }
            } else {
                if (delayingOther) {
                    event.setCancelled(true);
                    delayedPackets.add(new DelayedPacket(event.getPacket()));
                }
            }
        }
    }

    @Listener(Priority.MAX)
    public void onMotion(EventMotion event) {
        if (!delayedPackets.isEmpty()) {
            ArrayList<DelayedPacket> toRemove = new ArrayList<>();

            for (DelayedPacket p : delayedPackets) {
                if (isPing(p.getPacket())) {
                    if (p.getTimer().getTimeElapsed() >= pingDelay) {
                        toRemove.add(p);
                        PacketUtil.sendPacketFinal(p.getPacket());
                    }
                } else {
                    if (p.getTimer().getTimeElapsed() >= otherDelay) {
                        toRemove.add(p);
                        PacketUtil.sendPacketFinal(p.getPacket());
                    }
                }
            }

            if (!toRemove.isEmpty()) {
                for (DelayedPacket p : toRemove) {
                    delayedPackets.remove(p);
                }
            }

            toRemove.clear();
        }
    }

    public void startDelayingPing(long delay) {
        this.delayingPing = true;
        this.pingDelay = delay;
    }

    public void stopDelayingPing() {
        if (!delayedPackets.isEmpty()) {
            ArrayList<DelayedPacket> toRemove = new ArrayList<>();

            for (DelayedPacket p : delayedPackets) {
                if (isPing(p.getPacket())) {
                    PacketUtil.sendPacketFinal(p.getPacket());
                    toRemove.add(p);
                }
            }

            if (!toRemove.isEmpty()) {
                for (DelayedPacket p : toRemove) {
                    delayedPackets.remove(p);
                }
            }

            toRemove.clear();
        }

        this.delayingPing = false;
        this.pingDelay = 0;
    }

    public void clearPing() {
        if (!delayedPackets.isEmpty()) {
            ArrayList<DelayedPacket> toRemove = new ArrayList<>();

            for (DelayedPacket p : delayedPackets) {
                if (isPing(p.getPacket())) {
                    toRemove.add(p);
                }
            }

            if (!toRemove.isEmpty()) {
                for (DelayedPacket p : toRemove) {
                    delayedPackets.remove(p);
                }
            }

            toRemove.clear();
        }
    }

    public void startDelayingOther(long delay) {
        this.delayingOther = true;
        this.otherDelay = delay;
    }

    public void stopDelayingOther() {
        if (!delayedPackets.isEmpty()) {
            ArrayList<DelayedPacket> toRemove = new ArrayList<>();

            for (DelayedPacket p : delayedPackets) {
                if (!isPing(p.getPacket())) {
                    PacketUtil.sendPacketFinal(p.getPacket());
                    toRemove.add(p);
                }
            }

            if (!toRemove.isEmpty()) {
                for (DelayedPacket p : toRemove) {
                    delayedPackets.remove(p);
                }
            }

            toRemove.clear();
        }

        this.delayingOther = false;
        this.otherDelay = 0;
    }

    public void clearOther() {
        if (!delayedPackets.isEmpty()) {
            ArrayList<DelayedPacket> toRemove = new ArrayList<>();

            for (DelayedPacket p : delayedPackets) {
                if (!isPing(p.getPacket())) {
                    toRemove.add(p);
                }
            }

            if (!toRemove.isEmpty()) {
                for (DelayedPacket p : toRemove) {
                    delayedPackets.remove(p);
                }
            }

            toRemove.clear();
        }
    }

    public void releaseAll() {
        if (!delayedPackets.isEmpty()) {
            for (DelayedPacket p : delayedPackets) {
                PacketUtil.sendPacketFinal(p.getPacket());
            }

            delayedPackets.clear();
        }
    }

    public void stopAll() {
        releaseAll();

        this.delayingPing = false;
        this.delayingOther = false;
    }

    public boolean isPing(Packet p) {
        return p instanceof C0FPacketConfirmTransaction || p instanceof C00PacketKeepAlive;
    }

}