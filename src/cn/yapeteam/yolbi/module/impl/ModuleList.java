package cn.yapeteam.yolbi.module.impl;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.ModuleInfo;

import java.util.List;
import java.util.stream.Collectors;

@ModuleInfo(name = "ModuleList", category = ModuleCategory.VISUAL)
public class ModuleList extends Module {
    @Listener
    private void onRender(EventRender2D e) {
        List<Module> activeModules = YolBi.instance.getModuleManager().getModules().stream().filter(Module::isEnabled).collect(Collectors.toList());
        for (int i = 0; i < activeModules.size(); i++) {
            Module module = activeModules.get(i);
            mc.fontRendererObj.drawString(module.getName(), e.getScaledresolution().getScaledWidth() - mc.fontRendererObj.getStringWidth(module.getName()) - 2, 2 + i * (mc.fontRendererObj.FONT_HEIGHT + 2), -1, true);
        }
    }
}
