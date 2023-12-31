package cn.yapeteam.yolbi.command.impl;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.command.AbstractCommand;

public class CommandToggle extends AbstractCommand {
    public CommandToggle() {
        super("toggle");
    }

    @Override
    public void process(String[] args) {
        if (args.length == 1) {
            YolBi.instance.getModuleManager().getModuleByName(args[0]).toggle();
        }
    }
}
