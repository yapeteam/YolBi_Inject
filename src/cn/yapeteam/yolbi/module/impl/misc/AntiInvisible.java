package cn.yapeteam.yolbi.module.impl.misc;

import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.ModuleInfo;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.misc.ObjectStore;

@ModuleInfo(name = "AntiInvisible", category = ModuleCategory.MISC)
public class AntiInvisible extends Module {
    @Override
    protected void onEnable() {
        ObjectStore.objects.put("AntiInvisible", true);
    }

    @Override
    protected void onDisable() {
        ObjectStore.objects.put("AntiInvisible", false);
    }
}
