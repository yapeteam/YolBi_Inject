package cn.yapeteam.yolbi.event.impl.player;

import cn.yapeteam.yolbi.event.Event;
import cn.yapeteam.yolbi.event.type.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yuxiangll
 * @since 2024/1/7 21:34
 * IntelliJ IDEA
 */
@Getter
@Setter
@AllArgsConstructor
public class EventMove extends CancellableEvent {

    private double x, y, z;

}