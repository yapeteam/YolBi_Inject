package cn.yapeteam.yolbi.command.impl;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.command.AbstractCommand;
import cn.yapeteam.yolbi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

public class CommandBind extends AbstractCommand {
    public CommandBind() {
        super("bind");
    }

    @Override
    public void process(String[] args) {
        if (args.length == 2) {
            Module module = YolBi.instance.getModuleManager().getModuleByName(args[0]);
            if (module != null) {
                module.setKey(Keyboard.getKeyIndex(args[1]));
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Bind " + args[0] + " to Key " + args[1]));
            } else
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Module not found " + args[0]));
        }
    }
}
