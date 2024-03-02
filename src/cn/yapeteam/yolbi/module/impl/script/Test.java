package cn.yapeteam.yolbi.module.impl.script;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventAttack;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.ModuleInfo;

/**
 * @author yuxiangll
 * @since 2024/1/7 21:06
 * IntelliJ IDEA
 */
@ModuleInfo(name = "Test", category = ModuleCategory.SCRIPT)
public class Test extends Module {
    @Listener
    public void attack(EventAttack e) {
        System.out.println(e.getTargetEntity().getName());
        e.setCancelled(true);
    }
}
