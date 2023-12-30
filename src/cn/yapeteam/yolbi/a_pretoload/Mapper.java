package cn.yapeteam.yolbi.a_pretoload;

import cn.yapeteam.yolbi.a_pretoload.utils.ASMUtils;
import cn.yapeteam.yolbi.a_pretoload.utils.ClassUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Mapper {
    @Getter
    @AllArgsConstructor
    public static class Map {
        private final String owner, name, desc, obf;
        private final Type type;
    }

    public enum Type {
        Class, Field, Method
    }

    public enum Mode {
        None, Vanilla, Searge
    }

    /**
     * friendly→notch
     **/
    @Getter
    private static final ArrayList<Map> mappings = new ArrayList<>();
    /**
     * friendly→searges
     **/
    @Getter
    private static final java.util.Map<String, String> searges = new HashMap<>();

    @Getter
    @Setter
    public static Mode mode = null;

    public static void readMappings() {
        mappings.clear();
        searges.clear();

        String joined = new String(ResourceManager.resources.get("joined.srg"), StandardCharsets.UTF_8);
        String fields = new String(ResourceManager.resources.get("fields.csv"), StandardCharsets.UTF_8);
        String methods = new String(ResourceManager.resources.get("methods.csv"), StandardCharsets.UTF_8);

        for (String line : joined.split("\n")) {
            line = line.replace(String.valueOf((char) 13), "");
            String[] values = line.substring(4).split(" ");
            String[] obf, friendly;
            switch (line.substring(0, 2)) {
                case "CL":
                    mappings.add(new Map(null, values[1], null, values[0], Type.Class));
                    break;
                case "FD":
                    obf = ASMUtils.split(values[0], "/");
                    friendly = ASMUtils.split(values[2], "/");
                    mappings.add(new Map(
                            values[2].replace("/" + friendly[friendly.length - 1], ""),
                            friendly[friendly.length - 1],
                            values[3],
                            obf[obf.length - 1],
                            Type.Field
                    ));
                    break;
                case "MD":
                    obf = ASMUtils.split(values[0], "/");
                    friendly = ASMUtils.split(values[2], "/");
                    mappings.add(new Map(
                            values[2].replace("/" + friendly[friendly.length - 1], ""),
                            friendly[friendly.length - 1],
                            values[3],
                            obf[obf.length - 1],
                            Type.Method
                    ));
            }
        }
        for (String line : ASMUtils.split(fields + "\n" + methods, "\n")) {
            if (line.isEmpty()) continue;
            String[] values = line.split(",");
            if (values.length >= 2)
                searges.put(values[1], values[0]);
        }
    }

    /**
     * @param owner Class except
     * @param name  Name
     * @param desc  Class except
     * @param type  Class, Field or Method
     * @return ObfName
     */
    public static String map(@Nullable String owner, String name, @Nullable String desc, Type type) {
        switch (mode) {
            case Vanilla:
                return mappings.stream().filter(m ->
                        (m.type == type) &&
                        (type == Type.Class || owner == null || m.owner.equals(owner.replace('.', '/'))) &&
                        (m.name.equals(name.replace('.', '/'))) &&
                        (type == Type.Class || desc == null || m.desc.equals(desc))
                ).findFirst().orElse(new Map(null, null, null, name, null)).obf;
            case None:
                return name;
            case Searge:
                if (type == Type.Class)
                    return name;
                return searges.get(name);
        }
        return name;
    }

    public static Mode guessMappingMode() throws Throwable {
        byte[] bytes = ClassUtils.getClassBytes("net.minecraft.client.Minecraft");
        if (bytes == null) return Mode.Vanilla;
        ClassNode node = ASMUtils.node(bytes);
        if (node.methods.stream().anyMatch(m -> m.name.equals("runTick")))
            return Mode.None;
        return Mode.Searge;
    }

    public static String getObfClass(String name) {
        return map(null, name, null, Type.Class);
    }
}
