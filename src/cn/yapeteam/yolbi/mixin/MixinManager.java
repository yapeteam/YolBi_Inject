package cn.yapeteam.yolbi.mixin;

import cn.yapeteam.loader.JVMTIWrapper;
import cn.yapeteam.loader.SocketSender;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.mixin.Transformer;
import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.yolbi.mixin.injection.*;

import javax.swing.*;
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
        add(MixinEntityLivingBase.class);
        add(MixinRendererLivingEntity.class);
        add(MixinNetworkManager.class);
        add(MixinPlayerControllerMP.class);
        add(MixinEntityPlayer.class);
        add(MixinBlockNote.class);
        add(MixinBlock.class);
    }

    //for debug
    private static final File dir = new File("generatedClasses");

    public static void transform() throws Throwable {
        boolean ignored = dir.mkdirs();
        Map<String, byte[]> map = transformer.transform();
        SocketSender.send("S2");
        ArrayList<String> failed = new ArrayList<>();
        for (int i = 0; i < mixins.size(); i++) {
            Class<?> mixin = mixins.get(i);
            Class<?> targetClass = mixin.getAnnotation(Mixin.class).value();
            if (targetClass != null) {
                byte[] bytes = map.get(targetClass.getName());
                Files.write(new File(dir, targetClass.getName()).toPath(), bytes);
                int code = JVMTIWrapper.instance.redefineClass(targetClass, bytes);
                SocketSender.send("P2" + " " + (float) mixins.size() / (i + 1) * 100f);
                if (code != 0)
                    failed.add(mixin.getSimpleName());
                Logger.success("Redefined {}, Return Code {}.", targetClass, code);
            }
        }
        if (!failed.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to transform").append(' ').append(failed.size() == 1 ? "class" : "classes").append(' ').append('\n');
            for (String s : failed)
                stringBuilder.append(s).append('\n');
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf("\n"));
            JOptionPane.showMessageDialog(null, stringBuilder, "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void add(Class<?> clazz) throws Throwable {
        mixins.add(clazz);
        transformer.addMixin(clazz);
    }
}
