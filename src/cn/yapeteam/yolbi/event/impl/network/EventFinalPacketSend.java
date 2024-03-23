package cn.yapeteam.yolbi.event.impl.network;

import cn.yapeteam.yolbi.event.type.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;

@Setter
@AllArgsConstructor
@SuppressWarnings("unchecked")
public class EventFinalPacketSend extends CancellableEvent {

    private Packet<INetHandlerPlayServer> packet;

    public <T extends Packet<INetHandlerPlayServer>> T getPacket() {
        return (T) packet;
    }

}
