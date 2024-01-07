package cn.yapeteam.yolbi.a_pretoload;

import cn.yapeteam.yolbi.a_pretoload.logger.Logger;
import cn.yapeteam.yolbi.a_pretoload.mixin.Transformer;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Mixin;
import cn.yapeteam.yolbi.a_pretoload.utils.ClassUtils;
import cn.yapeteam.yolbi.injections.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
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

    public static void load(Instrumentation instrumentation) throws Throwable {
        dir.mkdirs();
        Map<String, byte[]> map = transformer.transform();
        for (Class<?> mixin : mixins) {
            String value = mixin.getAnnotation(Mixin.class).value();
            Class<?> theClass = ClassUtils.getClass(Mapper.map(null, value, null, Mapper.Type.Class));
            byte[] classfile = map.get(theClass.getName());
            Files.write(new File(dir, theClass.getName()).toPath(), classfile);
            ClassDefinition definition = new ClassDefinition(theClass, classfile);
            instrumentation.redefineClasses(definition);
            Logger.success("Redefined {}", theClass);
        }
    }

    private static void add(Class<?> clazz) throws Throwable {
        mixins.add(clazz);
        transformer.addMixin(clazz.getName());
    }
}
