package cn.yapeteam.yolbi.event.impl.player;

import cn.yapeteam.yolbi.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yuxiangll
 * @since 2024/1/7 21:38
 * IntelliJ IDEA
 */
@Getter
@Setter
@AllArgsConstructor
public class EventJump extends Event {

    private double motionY;
    private float yaw;
    private boolean boosting;

}
