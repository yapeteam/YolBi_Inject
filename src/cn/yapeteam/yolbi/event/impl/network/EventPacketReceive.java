package cn.yapeteam.yolbi.event.impl.network;

import cn.yapeteam.yolbi.event.type.CancellableEvent;
import lombok.AllArgsConstructor;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;

@AllArgsConstructor
public class EventPacketReceive extends CancellableEvent {
    private Packet<INetHandlerPlayClient> packet;

    public <T extends Packet<INetHandlerPlayClient>> T getPacket() {
        return (T) packet;
    }
}
