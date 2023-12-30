package cn.yapeteam.yolbi.a_pretoload.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class ASMUtils {
    @Contract(pure = true)
    public static @NotNull String slash(@NotNull String s) {
        return s.replace('.', '/');
    }

    public static ClassNode node(byte[] bytes) {
        if (bytes != null && bytes.length != 0) {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            return node;
        }

        return null;
    }

    public static String readClassName(byte[] bytes) {
        return new ClassReader(bytes).getClassName();
    }

    public static byte[] rewriteClass(@NotNull ClassNode node) {
        ClassWriter writer = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES) {
            @Override
            protected @NotNull String getCommonSuperClass(@NotNull String type1, @NotNull String type2) {
                Class<?> class1 = ClassUtils.getClass(type1);
                Class<?> class2 = ClassUtils.getClass(type2);
                if (class1 != null && class2 != null) {
                    if (class1.isAssignableFrom(class2)) {
                        return type1;
                    } else if (class2.isAssignableFrom(class1)) {
                        return type2;
                    } else if (!class1.isInterface() && !class2.isInterface()) {
                        do {
                            class1 = class1.getSuperclass();
                        } while (!class1.isAssignableFrom(class2));
                        return class1.getName().replace('.', '/');
                    }
                }
                return "java/lang/Object";
            }
        };
        node.accept(writer);
        return writer.toByteArray();
    }

    public static @Nullable <T> T getAnnotationValue(AnnotationNode node, String name) {
        if (node != null)
            for (int i = 0; i < node.values.size(); i++)
                if (node.values.get(i).equals(name))
                    return (T) node.values.get(i + 1);
        return null;
    }

    public static @NotNull String @NotNull [] split(@NotNull String str, String splitter) {
        if (!str.contains(splitter))
            return new String[]{};
        ArrayList<String> result = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder passed = new StringBuilder();
        for (int i = 0; i < str.length() - (splitter.length() - 1); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + splitter.length(); j++)
                sb.append(str.charAt(j));
            if (sb.toString().equals(splitter)) {
                result.add(stringBuilder.toString());
                passed.append(stringBuilder);
                passed.append(splitter);
                stringBuilder = new StringBuilder();
                i += splitter.length();
            }
            if (i < str.length() - 1)
                stringBuilder.append(str.charAt(i));
        }
        String last = str.replace(passed.toString(), "");
        if (!last.isEmpty())
            result.add(last);
        return result.toArray(new String[0]);
    }
}
