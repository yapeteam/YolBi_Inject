package cn.yapeteam.yolbi.event.impl.player;

import cn.yapeteam.yolbi.event.type.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

/**
 * @author yuxiangll
 * @since 2024/1/7 20:52
 * IntelliJ IDEA
 */

@Getter
@Setter
@AllArgsConstructor
public class EventAttack extends CancellableEvent {
    private Entity targetEntity;
}
