package cn.yapeteam.yolbi.notification;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.utils.animation.Easing;
import cn.yapeteam.yolbi.utils.animation.EasingAnimation;
import cn.yapeteam.yolbi.utils.render.RenderUtil;
import lombok.Getter;
import lombok.val;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

/**
 * @author yuxiangll
 * @since 2024/1/8 04:58
 * IntelliJ IDEA
 */
@Getter
public class Notification {
    private final String title;
    private final String content;
    private final EasingAnimation animationX, animationY;
    private final NotificationType type;
    private final long begin_time, duration;
    private boolean initialized = false;
    private final float height = 35;

    public Notification(String title, String content, Easing easingX, Easing easingY, long duration, NotificationType type) {
        this.title = title;
        this.content = content;
        this.animationX = new EasingAnimation(easingX, (long) (duration * 0.2), 0);
        this.animationY = new EasingAnimation(easingY, (long) (duration * 0.2), 0);
        this.type = type;
        begin_time = System.currentTimeMillis();
        this.duration = duration;
    }

    public boolean isDone() {
        return System.currentTimeMillis() >= begin_time + duration;
    }

    public void render(ScaledResolution sr, int index) {
        val font = YolBi.instance.getFontManager().getJelloRegular18();

        float width = font.getStringWidth(title) + 5 * 2;
        if (!initialized) {
            animationX.setStartValue(sr.getScaledWidth());
            animationY.setStartValue(sr.getScaledHeight());
            initialized = true;
        }
        float targetX = sr.getScaledWidth() - width - 2;
        if (System.currentTimeMillis() >= begin_time + duration - 200) {
            targetX = sr.getScaledWidth() + 2;
            animationX.setDuration((long) (duration * 0.1));
        }
        float targetY = sr.getScaledHeight() - (height + 2) * (index + 1);
        RenderUtil.drawBloomShadow((float) animationX.getValue(targetX), (float) animationY.getValue(targetY), width, height, 2, new Color(0));
        font.drawString(title, animationX.getValue(targetX) + 5, animationY.getValue(targetY) + (height - font.getHeight()) / 2f, -1);
    }
}
