package cn.yapeteam.yolbi.mixin;

import cn.yapeteam.loader.JVMTIWrapper;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.mixin.Transformer;
import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.yolbi.mixin.injection.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;

public class MixinManager {
    public static final ArrayList<Class<?>> mixins = new ArrayList<>();
    public static Transformer transformer;

    public static void init() throws Throwable {
        transformer = new Transformer(JVMTIWrapper.instance::getClassBytes);
        add(MixinMinecraft.class);
        add(MixinGuiIngame.class);
        add(MixinEntityPlayerSP.class);
        add(MixinEntityRenderer.class);
        add(MixinPlayerControllerMP.class);
        add(MixinBlock.class);
        add(MixinEntityLivingBase.class);
        add(MixinRendererLivingEntity.class);
    }

    //for debug
    private static final File dir = new File("generatedClasses");

    public static void load() throws Throwable {
        boolean ignored = dir.mkdirs();
        Map<String, byte[]> map = transformer.transform();
        for (Class<?> mixin : mixins) {
            Class<?> targetClass = mixin.getAnnotation(Mixin.class).value();
            if (targetClass != null) {
                byte[] bytes = map.get(targetClass.getName());
                Files.write(new File(dir, targetClass.getName()).toPath(), bytes);
                int code = JVMTIWrapper.instance.redefineClass(targetClass, bytes);
                Logger.success("Redefined {}, Return Code {}.", targetClass, code);
            }
        }
    }

    private static void add(Class<?> clazz) throws Throwable {
        mixins.add(clazz);
        transformer.addMixin(clazz);
    }
}
