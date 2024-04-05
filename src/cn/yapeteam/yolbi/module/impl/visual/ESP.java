package cn.yapeteam.yolbi.module.impl.visual;

import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.ModuleInfo;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.render.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;

@ModuleInfo(name = "ESP", category = ModuleCategory.VISUAL)
public class ESP extends Module {
    private ClientTheme theme;

    @Override
    protected void onEnable() {
        theme = YolBi.instance.getModuleManager().getModule(ClientTheme.class);
    }

    @Listener
    private void onRender3D(EventRender3D event) {
        if (mc.theWorld != null)
            for (Entity entity : mc.theWorld.loadedEntityList)
                if (entity != mc.thePlayer && entity instanceof EntityLivingBase)
                    RenderUtil.drawEntityBox((EntityLivingBase) entity, new Color(theme.getColor(0)), true, true, 1, event.getPartialTicks());
    }
}
