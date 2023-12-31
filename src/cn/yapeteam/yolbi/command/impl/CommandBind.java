package cn.yapeteam.yolbi.command.impl;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.command.AbstractCommand;
import org.lwjgl.input.Keyboard;

public class CommandBind extends AbstractCommand {
    public CommandBind() {
        super("bind");
    }

    @Override
    public void process(String[] args) {
        if (args.length == 2) {
            YolBi.instance.getModuleManager().getModuleByName(args[0]).setKey(Keyboard.getKeyIndex(args[1]));
        }
    }
}
