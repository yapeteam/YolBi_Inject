package cn.yapeteam.yolbi.utils.network;

import cn.yapeteam.yolbi.utils.misc.TimerUtil;
import lombok.Getter;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;

public class DelayedPacket {
    private final Packet<INetHandlerPlayClient> packet;

    @Getter
    private final TimerUtil timer;

    public DelayedPacket(Packet<INetHandlerPlayClient> packet) {
        this.packet = packet;
        this.timer = new TimerUtil();
    }

    public <T extends Packet<INetHandlerPlayClient>> T getPacket() {
        return (T) packet;
    }
}
