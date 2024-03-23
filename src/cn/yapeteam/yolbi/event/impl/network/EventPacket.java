package cn.yapeteam.yolbi.event.impl.network;

import cn.yapeteam.yolbi.event.type.CancellableEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

@Getter
@Setter
public class EventPacket extends CancellableEvent {
    private Packet<? extends INetHandler> packet;
    private Type type;

    public enum Type {
        Send, Receive
    }

    public EventPacket(Packet<? extends INetHandler> packet, Type type) {
        this.packet = packet;
        this.type = type;
    }

    public boolean isServerSide() {
        return type == Type.Receive;
    }
}
