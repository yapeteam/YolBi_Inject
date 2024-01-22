package cn.yapeteam.yolbi.notification;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yuxiangll
 * @since 2024/1/8 04:59
 * IntelliJ IDEA
 */
@SuppressWarnings("unused")
public class NotificationManager {
    private final CopyOnWriteArrayList<Notification> notificationArrayList;

    public NotificationManager() {
        notificationArrayList = new CopyOnWriteArrayList<>();
        YolBi.instance.getEventManager().register(this);
    }

    public void clearAll() {
        notificationArrayList.clear();
    }

    public void post(Notification notification) {
        notificationArrayList.add(notification);
    }

    @Listener
    public void onRender(final EventRender2D event) {
        val sr = new ScaledResolution(Minecraft.getMinecraft());
        int pre_size = notificationArrayList.size();
        for (int j = 0; j < pre_size; j++)
            for (int i = 0; i < notificationArrayList.size(); i++)
                if (notificationArrayList.get(i) != null && notificationArrayList.get(i).isDone()) {
                    notificationArrayList.remove(notificationArrayList.get(i));
                    i--;
                }
        for (int i = 0; i < notificationArrayList.size(); i++)
            notificationArrayList.get(i).render(sr, i);
    }
}
