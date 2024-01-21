package cn.yapeteam.yolbi.notification;

import cn.yapeteam.yolbi.utils.animation.Animation;
import cn.yapeteam.yolbi.utils.animation.Easing;
import lombok.Getter;

/**
 * @author yuxiangll
 * @since 2024/1/8 04:58
 * IntelliJ IDEA
 */
@Getter
public class Notification {
    private final String title;
    private final String content;
    private final Animation animation;
    private final NotificationType type;


    public Notification(String title, String content, Animation animation, NotificationType type) {
        this.title = title;
        this.content = content;
        this.animation = animation;
        this.type = type;
        this.animation.run(1);
    }

    public Notification(String title,  Animation animation, NotificationType type) {
        this.title = title;
        this.content = "";
        this.animation = animation;
        this.type = type;
        this.animation.run(1);

    }
    public Notification(String title, String content, Animation animation) {
        this.title = title;
        this.content = content;
        this.animation = animation;
        this.type = NotificationType.SUCCESS;
        this.animation.run(1);

    }
    public Notification(String title, String content, NotificationType type) {
        this.title = title;
        this.content = content;
        this.animation = new Animation(Easing.EASE_IN_OUT_CIRC,1500);
        this.type = type;
        this.animation.run(1);

    }
}
