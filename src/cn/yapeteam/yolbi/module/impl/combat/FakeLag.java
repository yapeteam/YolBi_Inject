package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.ModuleInfo;
import cn.yapeteam.loader.api.module.values.impl.BooleanValue;
import cn.yapeteam.loader.api.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventLoadWorld;
import cn.yapeteam.yolbi.event.impl.network.EventPacket;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.misc.TimerUtil;
import cn.yapeteam.yolbi.utils.network.PacketUtil;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.*;

import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "FakeLag", category = ModuleCategory.COMBAT)
public class FakeLag extends Module {
    public FakeLag() {
        this.addValues(fakeLagPosValue, fakeLagBlockValue, fakeLagAttackValue, fakeLagSpoofValue, lagDelayValue, lagDurationValue);
    }

    private final BooleanValue fakeLagPosValue = new BooleanValue("fakeLagPos", true);
    private final BooleanValue fakeLagBlockValue = new BooleanValue("fakeLagBloc", true);
    private final BooleanValue fakeLagAttackValue = new BooleanValue("fakeLagAttack", true);
    private final BooleanValue fakeLagSpoofValue = new BooleanValue("fakeLagSpoof", false);
    private final NumberValue<Integer> lagDelayValue = new NumberValue<>("lagDelay", 1, 1, 2000, 1);
    private final NumberValue<Integer> lagDurationValue = new NumberValue<>("lagDuration", 200, 0, 1000, 1);
    private final CopyOnWriteArrayList<Packet<? extends INetHandler>> packetBuffer = new CopyOnWriteArrayList<>();
    private boolean isSent = false;
    private final TimerUtil fakeLagDelay = new TimerUtil();
    private final TimerUtil fakeLagDuration = new TimerUtil();


    @Override
    public void onEnable() {
        isSent = false;
        packetBuffer.clear();
    }

    @Override
    public void onDisable() {
        for (Packet<? extends INetHandler> packet : packetBuffer) {
            PacketUtil.skip(packet);
            mc.getNetHandler().getNetworkManager().sendPacket(packet);
        }
        packetBuffer.clear();
    }

    @Listener
    public void onWorld(EventLoadWorld eventLoadWorld) {
        isSent = false;
        fakeLagDuration.reset();
        fakeLagDelay.reset();
        packetBuffer.clear();
    }

    @Listener
    public void onUpdate(EventUpdate eventUpdate) {
        if (!fakeLagDelay.hasTimePassed(lagDelayValue.getValue().longValue())) fakeLagDuration.reset();
        // Send
        if (fakeLagDuration.hasTimePassed(lagDurationValue.getValue().longValue())) {
            fakeLagDelay.reset();
            fakeLagDuration.reset();

            for (Packet<? extends INetHandler> packet : packetBuffer) {
                PacketUtil.skip(packet);
                mc.getNetHandler().getNetworkManager().sendPacket(packet);
            }
            isSent = true;
            packetBuffer.clear();
        }
    }

    @Listener
    public void onSentPacket(EventPacket eventPacket) {
        Packet<? extends INetHandler> packet = eventPacket.getPacket();
        if (fakeLagDelay.hasTimePassed(lagDelayValue.getValue().longValue())) {
            if (isSent && fakeLagSpoofValue.getValue() && !eventPacket.isServerSide()) {
                Packet<INetHandlerPlayServer> packet1 = new C03PacketPlayer(true);
                PacketUtil.skip(packet1);
                mc.getNetHandler().getNetworkManager().sendPacket(packet1);
                if (lagDurationValue.getValue() >= 300) {
                    Packet<INetHandlerPlayServer> packet2 = new C03PacketPlayer(true);
                    PacketUtil.skip(packet2);
                    mc.getNetHandler().getNetworkManager().sendPacket(packet2);
                }
                isSent = false;
            }
            if (packet instanceof C00PacketKeepAlive || packet instanceof C0FPacketConfirmTransaction) {
                eventPacket.setCancelled(true);
                packetBuffer.add(packet);
            }
            if (fakeLagAttackValue.getValue() && (packet instanceof C02PacketUseEntity || packet instanceof C0APacketAnimation)) {
                eventPacket.setCancelled(true);
                packetBuffer.add(packet);
                if (packet instanceof C0APacketAnimation) return;
            }
            if (fakeLagBlockValue.getValue() && (packet instanceof C07PacketPlayerDigging || packet instanceof C08PacketPlayerBlockPlacement || packet instanceof C0APacketAnimation)) {
                eventPacket.setCancelled(true);
                packetBuffer.add(packet);
            }
            if (fakeLagPosValue.getValue() && (packet instanceof C03PacketPlayer || packet instanceof C0BPacketEntityAction)) {
                eventPacket.setCancelled(true);
                packetBuffer.add(packet);
            }
        }
    }
}
