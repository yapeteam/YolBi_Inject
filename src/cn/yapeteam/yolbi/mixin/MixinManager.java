package cn.yapeteam.yolbi.mixin;

import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.NativeWrapper;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.mixin.Transformer;
import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.utils.ClassUtils;
import cn.yapeteam.yolbi.mixin.injection.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;

public class MixinManager {
    public static final ArrayList<Class<?>> mixins = new ArrayList<>();
    public static Transformer transformer;

    public static void init() throws Throwable {
        transformer = new Transformer(MixinManager::getClassBytes);
        add(MixinMinecraft.class);
        add(MixinGuiIngame.class);
        add(MixinEntityPlayerSP.class);
        add(MixinEntityRenderer.class);
        add(MixinPlayerControllerMP.class);
        add(MixinBlock.class);
        add(MixinEntityLivingBase.class);
    }

    public static byte[] getClassBytes(String name) throws Throwable {
        InputStream classStream = MixinManager.class.getResourceAsStream("/" + name.replace('.', '/') + ".class");
        if (classStream == null) {
            byte[] bytes = ClassUtils.getClassBytes(name);
            if (bytes != null)
                return bytes;
            throw new ClassNotFoundException(name);
        } else {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];

            int len;
            while ((len = classStream.read(buf)) != -1) {
                os.write(buf, 0, len);
            }

            classStream.close();
            return os.toByteArray();
        }
    }

    private static final File dir = new File("generatedClasses");

    public static void load() throws Throwable {
        dir.mkdirs();
        Map<String, byte[]> map = transformer.transform();
        for (Class<?> mixin : mixins) {
            String value = mixin.getAnnotation(Mixin.class).value();
            Class<?> theClass = ClassUtils.getClass(Mapper.map(null, value, null, Mapper.Type.Class));
            if (theClass != null) {
                byte[] bytes = map.get(theClass.getName());
                Files.write(new File(dir, theClass.getName()).toPath(), bytes);
                int code = NativeWrapper.redefineClass(theClass, bytes);
                Logger.success("Redefined {}, Return Code {}.", theClass, code);
            }
        }
    }

    private static void add(Class<?> clazz) throws Throwable {
        mixins.add(clazz);
        transformer.addMixin(clazz.getName());
    }
}
