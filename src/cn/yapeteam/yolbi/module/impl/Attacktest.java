package cn.yapeteam.yolbi.module.impl;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventAttack;
import cn.yapeteam.yolbi.event.impl.player.EventJump;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.ModuleInfo;
import org.lwjgl.input.Keyboard;

/**
 * @author yuxiangll
 * @since 2024/1/7 21:06
 * IntelliJ IDEA
 */
@ModuleInfo(name = "Attacktest", category = ModuleCategory.COMBAT, key = Keyboard.KEY_Z)
public class Attacktest extends Module {
    @Listener
    public void onJUmp(EventJump eventJump){
        System.out.println("JUMP");
    }


}
