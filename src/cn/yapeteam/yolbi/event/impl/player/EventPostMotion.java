package cn.yapeteam.yolbi.event.impl.player;

import cn.yapeteam.yolbi.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventPostMotion extends Event {
    private final double x, y, z;
    private final float yaw, pitch;
    private final boolean onGround;
}
