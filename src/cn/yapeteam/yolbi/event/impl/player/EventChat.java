package cn.yapeteam.yolbi.event.impl.player;

import cn.yapeteam.yolbi.event.type.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventChat extends CancellableEvent {
    private String message;
}