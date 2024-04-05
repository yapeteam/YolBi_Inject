package cn.yapeteam.loader;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.utils.ASMUtils;
import cn.yapeteam.loader.utils.ClassUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm_9_2.tree.ClassNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
     * friendly→obf
     **/
    @Getter
    private static ArrayList<Map> mappings = new ArrayList<>();
    /**
     * friendly→notch
     **/
    @Getter
    private static final ArrayList<Map> vanilla = new ArrayList<>();
    /**
     * friendly→searges
     **/
    @Getter
    private static final ArrayList<Map> searges = new ArrayList<>();

    @Getter
    public static Mode mode = null;

    public static void readMapping(String content, ArrayList<Map> dest) {
        dest.clear();
        for (String line : content.split("\n")) {
            String[] values = line.substring(4).split(" ");
            String[] obf, friendly;
            switch (line.substring(0, 2)) {
                case "CL":
                    dest.add(new Map(null, values[1], null, values[0], Type.Class));
                    break;
                case "FD":
                    if (values.length == 4) {
                        obf = ASMUtils.split(values[0], "/");
                        friendly = ASMUtils.split(values[2], "/");
                        dest.add(new Map(
                                values[2].replace("/" + friendly[friendly.length - 1], ""),
                                friendly[friendly.length - 1],
                                values[3],
                                obf[obf.length - 1],
                                Mapper.Type.Field
                        ));
                    } else if (values.length == 2) {
                        obf = ASMUtils.split(values[0], "/");
                        friendly = ASMUtils.split(values[1], "/");
                        dest.add(new Map(
                                values[1].replace("/" + friendly[friendly.length - 1], ""),
                                friendly[friendly.length - 1],
                                null,
                                obf[obf.length - 1],
                                Mapper.Type.Field
                        ));
                    }
                    break;
                case "MD":
                    obf = ASMUtils.split(values[0], "/");
                    friendly = ASMUtils.split(values[2], "/");
                    dest.add(
                            new Map(
                                    values[2].replace("/" + friendly[friendly.length - 1], ""),
                                    friendly[friendly.length - 1],
                                    values[3],
                                    obf[obf.length - 1],
                                    Type.Method
                            )
                    );
            }
        }
    }

    public static void readMappings() {
        String vanilla = new String(Objects.requireNonNull(ResourceManager.resources.get("mappings/vanilla.srg")), StandardCharsets.UTF_8);
        String forge = new String(Objects.requireNonNull(ResourceManager.resources.get("mappings/forge.srg")), StandardCharsets.UTF_8);
        readMapping(vanilla, getVanilla());
        readMapping(forge, getSearges());
    }

    @Getter
    private static final java.util.Map<String, String> cache = new HashMap<>();

    /**
     * @param owner Class except
     * @param name  Name
     * @param desc  Class except
     * @param type  Class, Field or Method
     * @return ObfName
     */
    public static String map(@Nullable String owner, String name, @Nullable String desc, Type type) {
        if (owner != null) owner = owner.replace('.', '/');
        String identifier = owner + "." + name + " " + desc;
        String value = cache.get(identifier);
        if (value != null) return value;
        String finalOwner = owner;
        val map = mappings.stream().filter(m ->
                m.type == type &&
                        (type == Type.Class || finalOwner == null || m.owner.equals(finalOwner.replace('.', '/'))) &&
                        (m.name.equals(name.replace('.', '/'))) &&
                        (type == Type.Class || desc == null || m.desc.equals(desc))
        ).findFirst().orElse(new Map(owner, name, "null", name, type));
        String result = applyMode(map);
        cache.put(identifier, result);
        return result;
    }

    public static Mode guessMappingMode() {
        Class<?> clazz = ClassUtils.getClass("net.minecraft.client.Minecraft");
        if (clazz == null) return Mode.Vanilla;
        byte[] bytes = JVMTIWrapper.instance.getClassBytes(clazz);
        ClassNode node = ASMUtils.node(bytes);
        if (node.methods.stream().anyMatch(m -> m.name.equals("runTick")))
            return Mode.None;
        return Mode.Searge;
    }

    public static void setMode(Mode mode) {
        Mapper.mode = mode;
        switch (mode) {
            case Vanilla:
                mappings = vanilla;
                break;
            case Searge:
                mappings = searges;
                break;
            case None:
                break;
        }
    }

    public static String applyMode(Map map) {
        switch (mode) {
            case Vanilla:
                return map.obf;
            case Searge:
                if (map.type == Type.Class)
                    return map.name;
                return map.obf;
            case None:
                return map.name;
        }
        return map.name;
    }

    public static String mapWithSuper(String owner, String name, String desc, Type type) {
        owner = owner.replace('.', '/');
        String identifier = owner + "." + name + " " + desc;
        String value = cache.get(identifier);
        if (value != null) return value;
        java.util.Map<String, Map> owners = new HashMap<>();
        mappings.stream().filter(m ->
                m.type == type && m.name.equals(name) && (desc == null || m.desc == null || desc.equals(m.desc))
        ).forEach(m -> owners.put(m.owner, m));
        String mappedOwner = map(null, owner, null, Type.Class);
        Class<?> theClass = ClassUtils.getClass(mappedOwner);
        while (theClass != Object.class) {
            if (theClass != null) {
                Class<?> finalTheClass = theClass;
                java.util.Map.Entry<String, Map> entry = owners.entrySet().stream()
                        .filter(m -> map(null, m.getKey(), null, Type.Class).equals(finalTheClass.getName().replace('.', '/')))
                        .findFirst().orElse(null);
                if (entry != null) {
                    cache.put(identifier, applyMode(entry.getValue()));
                    return applyMode(entry.getValue());
                }
                theClass = theClass.getSuperclass();
            } else {
                Logger.warn("Owner not found: {}", mappedOwner);
                break;
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

    public static String getFriendlyClass(String obf) {
        String value = cache.get(obf);
        if (value != null) return value;
        Map map = mappings.stream().filter(m -> m.type == Type.Class && m.obf.equals(obf.replace('.', '/'))).findFirst().orElse(null);
        if (map != null) {
            cache.put(obf, map.name);
            return map.name;
        }
        return obf;
    }
}
