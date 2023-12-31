package cn.yapeteam.yolbi.module.impl;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.font.cfont.CFontRenderer;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.ModuleInfo;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ModuleInfo(name = "HUD", category = ModuleCategory.VISUAL)
public class HeadUpDisplay extends Module {
    @Listener
    private void onRender(EventRender2D e) {
        GlStateManager.color(1, 1, 1, 1);
        CFontRenderer fontRenderer = YolBi.instance.getFontManager().getJelloRegular18();
        fontRenderer.drawString(YolBi.name + " " + YolBi.version, 2, 2, -1, true);
        List<Module> activeModules = YolBi.instance.getModuleManager().getModules().stream().filter(Module::isEnabled).sorted(Comparator.comparingInt(m -> m.getName().length())).collect(Collectors.toList());
        for (int i = 0; i < activeModules.size(); i++) {
            Module module = activeModules.get(i);
            fontRenderer.drawString(module.getName(), e.getScaledresolution().getScaledWidth() - fontRenderer.getStringWidth(module.getName()) - 2, 2 + i * (fontRenderer.getStringHeight() + 2), -1, true);
        }
    }
}
