package cn.yapeteam.yolbi.module.impl.visual;

import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.ModuleInfo;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventLoadWorld;
import cn.yapeteam.yolbi.event.impl.network.EventPacketSend;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.font.AbstractFontRenderer;
import cn.yapeteam.yolbi.module.Module;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;

@ModuleInfo(name = "PacketDebug", category = ModuleCategory.VISUAL)
public class PacketDebug extends Module {
    ArrayList<Pair<Packet<? extends INetHandler>, Long>> list = new ArrayList<>();

    @Listener
    private void onLoadWorld(EventLoadWorld e) {
        list.clear();
    }


    @Listener
    private void onSend(EventPacketSend e) {
        Packet<? extends INetHandler> packet = e.getPacket();
        Optional<Pair<Packet<? extends INetHandler>, Long>> optional = list.stream().filter(p -> p.a.getClass().equals(packet.getClass())).findFirst();
        if (optional.isPresent()) {
            Pair<Packet<? extends INetHandler>, Long> pair = optional.get();
            pair.b++;
            pair.a = packet;
        } else list.add(new Pair<>(packet, 1L));
    }

    @Listener
    private void onRender2D(EventRender2D e) {
        AbstractFontRenderer font = YolBi.instance.getFontManager().getDefault18();
        for (int i = 0; i < list.size(); i++) {
            Pair<Packet<? extends INetHandler>, Long> pair = list.get(i);
            font.drawString(Mapper.getFriendlyClass(pair.a.getClass().getSimpleName()) + ": " + pair.b + " - " + packetToString(pair.a) + ":" + pair.b, 10, 10 + i * font.getHeight(), -1);
        }
    }

    private String packetToString(Packet<? extends INetHandler> packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Field field : packet.getClass().getFields()) {
            field.setAccessible(true);
            try {
                if (field.getType().isPrimitive() || field.getType() == String.class)
                    sb.append(field.getName()).append(": ").append(field.get(packet)).append(",");
            } catch (IllegalAccessException e) {
                Logger.exception(e);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    static class Pair<A, B> {
        A a;
        B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }
}
