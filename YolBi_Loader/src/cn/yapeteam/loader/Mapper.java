package cn.yapeteam.loader;

import cn.yapeteam.loader.utils.ASMUtils;
import cn.yapeteam.loader.utils.ClassUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
        return applyMode(mappings.stream().filter(m ->
                (m.type == type) &&
                (type == Type.Class || owner == null || m.owner.equals(owner.replace('.', '/'))) &&
                (m.name.equals(name.replace('.', '/'))) &&
                (type == Type.Class || desc == null || m.desc.equals(desc))
        ).findFirst().orElse(new Map(owner, name, desc, name, type)));
    }

    public static Mode guessMappingMode() {
        byte[] bytes = ClassUtils.getClassBytes("net.minecraft.client.Minecraft");
        if (bytes == null) return Mode.Vanilla;
        ClassNode node = ASMUtils.node(bytes);
        if (node.methods.stream().anyMatch(m -> m.name.equals("runTick")))
            return Mode.None;
        return Mode.Searge;
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    public static void main(String[] args) throws Throwable {
        /*ResourceManager.resources.put("joined.srg", readStream(Objects.requireNonNull(Mapper.class.getResourceAsStream("/joined.srg"))));
        ResourceManager.resources.put("fields.csv", readStream(Objects.requireNonNull(Mapper.class.getResourceAsStream("/fields.csv"))));
        ResourceManager.resources.put("methods.csv", readStream(Objects.requireNonNull(Mapper.class.getResourceAsStream("/methods.csv"))));
        readMappings();
        setMode(Mode.Vanilla);
        System.out.println(mapFieldWithSuper(EntityLivingBase.class.getName(), "posX", null));*/
    }

    public static String applyMode(Map map) {
        switch (mode) {
            case Vanilla:
                return map.obf;
            case None:
                return map.name;
            case Searge:
                if (map.type == Type.Class)
                    return map.name;
                return searges.get(map.name);
        }
        return map.name;
    }

    public static String mapWithSuper(String owner, String name, String desc, Type type) {
        owner = owner.replace('.', '/');
        name = name.replace('.', '/');
        String finalName = name;
        java.util.Map<String, Map> owners = new HashMap<>();
        mappings.stream().filter(m ->
                m.type == type && m.name.equals(finalName) && (desc == null || m.desc.equals(desc))
        ).forEach(m -> owners.put(m.owner, m));
        String mappedOwner = map(null, owner, null, Type.Class);
        Class<?> theClass = ClassUtils.getClass(mappedOwner);
        while (theClass != Object.class) {
            if (theClass != null) {
                Class<?> finalTheClass = theClass;
                java.util.Map.Entry<String, Map> entry = owners.entrySet().stream().filter(m ->
                                map(null, m.getKey(), null, Type.Class).equals(finalTheClass.getName().replace('.', '/')))
                        .findFirst().orElse(null);
                if (entry != null) return applyMode(entry.getValue());
                theClass = theClass.getSuperclass();
            }
        }
        return name;
    }

    public static String mapMethodWithSuper(String owner, String name, String desc) {
        return mapWithSuper(owner, name, desc, Type.Method);
    }

    public static String mapFieldWithSuper(String owner, String name, String desc) {
        return mapWithSuper(owner, name, desc, Type.Field);
    }

    public static String getObfClass(String name) {
        return map(null, name, null, Type.Class);
    }
}
