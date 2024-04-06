package cn.yapeteam.yolbi.module.impl.visual;

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
    ArrayList<Group<Class<?>, String, Long>> list = new ArrayList<>();

    @Listener
    private void onLoadWorld(EventLoadWorld e) {
        list.clear();
    }


    @Listener
    private void onSend(EventPacketSend e) {
        Packet<? extends INetHandler> packet = e.getPacket();
        Optional<Group<Class<?>, String, Long>> optional = list.stream().filter(p -> p.a.equals(packet.getClass())).findFirst();
        if (optional.isPresent()) {
            Group<Class<?>, String, Long> group = optional.get();
            group.c++;
            group.b = group.a.getSimpleName() + ": " + group.c + " - " + packetToString(packet);
        } else
            list.add(new Group<>(packet.getClass(), packet.getClass().getSimpleName() + ": " + 1 + " - " + packetToString(packet), 1L));
    }

    @Listener
    private void onRender2D(EventRender2D e) {
        AbstractFontRenderer font = YolBi.instance.getFontManager().getPingFang12();
        for (int i = 0; i < list.size(); i++) {
            Group<Class<?>, String, Long> group = list.get(i);
            font.drawString(group.b, 10, 20 + i * font.getHeight(), -1);
        }
    }

    private String packetToString(Packet<? extends INetHandler> packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Field field : packet.getClass().getDeclaredFields()) {
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

    static class Group<A, B, C> {
        A a;
        B b;
        C c;

        public Group(A a, B b, C c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
}
