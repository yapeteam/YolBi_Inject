package cn.yapeteam.loader;

import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Shadow;
import cn.yapeteam.loader.mixin.annotations.Super;
import cn.yapeteam.loader.mixin.utils.DescParser;
import cn.yapeteam.loader.utils.ASMUtils;
import lombok.AllArgsConstructor;
import org.objectweb.asm_9_2.Handle;
import org.objectweb.asm_9_2.Type;
import org.objectweb.asm_9_2.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ClassMapper {
    public static byte[] map(byte[] bytes) throws Throwable {
        ClassNode node = ASMUtils.node(bytes);
        System.out.println(node.name);
        node.superName = Mapper.getObfClass(node.superName);
        List<String> interfaces = new ArrayList<>();
        for (String anInterface : node.interfaces)
            interfaces.add(Mapper.getObfClass(anInterface));
        node.interfaces = interfaces;
        if (node.visibleAnnotations != null)
            for (AnnotationNode visibleAnnotation : node.visibleAnnotations) {
                if (visibleAnnotation.values == null) continue;
                List<Object> values = new ArrayList<>();
                for (int i = 0; i < visibleAnnotation.values.size(); i++) {
                    Object aValue = visibleAnnotation.values.get(i);
                    if (aValue instanceof Type) {
                        Type type = (Type) aValue;
                        String name = type.getClassName();
                        int count = 0;
                        if (name.contains("[]"))
                            while (name.contains("[]")) {
                                name = replaceFirst(name, "[]", "");
                                count++;
                            }
                        StringBuilder builder = new StringBuilder();
                        for (int j = 0; j < count; j++)
                            builder.append("[");
                        aValue = Type.getType(builder + "L" + name.replace(name, Mapper.getObfClass(name)).replace('.', '/') + ";");
                    }
                    values.add(aValue);
                }
                visibleAnnotation.values = values;
            }
        ArrayList<Name_Desc> methodShadows = new ArrayList<>();
        ArrayList<Name_Desc> fieldShadows = new ArrayList<>();
        String targetName = null;
        if (node.visibleAnnotations != null) {
            Type type = ASMUtils.getAnnotationValue(
                    node.visibleAnnotations.stream()
                            .filter(a -> a.desc.contains(ASMUtils.slash(Mixin.class.getName())))
                            .findFirst().orElse(null), "value"
            );
            if (type != null) targetName = type.getClassName();
            if (targetName != null) {
                for (MethodNode method : node.methods) {
                    if (Shadow.Helper.hasAnnotation(method))
                        methodShadows.add(new Name_Desc(method.name, method.desc));
                }
                for (FieldNode field : node.fields) {
                    if (Shadow.Helper.hasAnnotation(field))
                        fieldShadows.add(new Name_Desc(field.name, field.desc));
                }
                targetName = targetName.replace('.', '/');
            }
        }
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                    if (methodShadows.stream().anyMatch(m -> m.name.equals(methodInsnNode.name) && m.desc.equals(methodInsnNode.desc)))
                        methodInsnNode.owner = Mapper.getFriendlyClass(targetName);
                } else if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction;
                    if (fieldShadows.stream().anyMatch(m -> m.name.equals(fieldInsnNode.name) && m.desc.equals(fieldInsnNode.desc)))
                        fieldInsnNode.owner = Mapper.getFriendlyClass(targetName);
                }
            }
            if (Mapper.getMode() != Mapper.Mode.None)
                method(method, node, targetName);
        }
        for (FieldNode field : node.fields) {
            if (Mapper.getMode() != Mapper.Mode.None)
                field(field);
        }
        bytes = ASMUtils.rewriteClass(node);
        return bytes;
    }

    private static String parseDesc(String desc) {
        char[] chars = desc.toCharArray();
        ArrayList<String> types = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == 'L') {
                i++;
                while (chars[i] != ';') {
                    builder.append(chars[i]);
                    i++;
                }
                types.add(builder.toString());
                builder = new StringBuilder();
            }
        }
        for (String type : types) {
            builder.append(type).append(';');
        }
        String result = builder.toString();
        result = types.size() == 1 ? result.replace(";", "") : result;
        return result;
    }

    public static String replaceFirst(String string, CharSequence target, CharSequence replacement) {
        return Pattern.compile(target.toString(), Pattern.LITERAL).matcher(string).replaceFirst(Matcher.quoteReplacement(replacement.toString()));
    }


    private static boolean hasType(String type) {
        return Mapper.getVanilla().stream().anyMatch(m -> m.getType() == Mapper.Type.Class && m.getName().equals(type));
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

    @AllArgsConstructor
    public static class Name_Desc {
        public String name, desc;
    }

    public static void main(String[] args) throws Throwable {
        Mapper.setMode(Mapper.Mode.Vanilla);
        Mapper.readMappings();
        map(readStream(Files.newInputStream(Paths.get("AimAssist.class"))));
    }

    public static void method(MethodNode source, ClassNode parent, String targetName) throws Throwable {
        if (source.visibleAnnotations != null) {
            for (AnnotationNode visibleAnnotation : source.visibleAnnotations) {
                if (Super.Helper.isAnnotation(visibleAnnotation)) {
                    source.name = Mapper.mapWithSuper(parent.superName, source.name, null, Mapper.Type.Method);
                    break;
                }
            }
        }
        for (String name : parseDesc(source.desc).split(";"))
            source.desc = replaceFirst(source.desc, name, Mapper.getObfClass(name));
        for (AbstractInsnNode instruction : source.instructions) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                if (hasType(methodInsnNode.owner)) {
                    methodInsnNode.name = Mapper.mapMethodWithSuper(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
                    methodInsnNode.owner = Mapper.getObfClass(methodInsnNode.owner);
                }
                if (hasType(parseDesc(methodInsnNode.desc))) methodInsnNode.desc = desc(methodInsnNode.desc);
                for (String name : parseDesc(methodInsnNode.desc).split(";"))
                    methodInsnNode.desc = replaceFirst(methodInsnNode.desc, name, Mapper.getObfClass(name));
            } else if (instruction instanceof TypeInsnNode) {
                TypeInsnNode typeInsnNode = (TypeInsnNode) instruction;
                typeInsnNode.desc = Mapper.map(null, typeInsnNode.desc, null, Mapper.Type.Class);
            } else if (instruction instanceof FieldInsnNode) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction;
                if (hasType(fieldInsnNode.owner)) {
                    fieldInsnNode.name = Mapper.mapFieldWithSuper(fieldInsnNode.owner, fieldInsnNode.name, fieldInsnNode.desc);
                    fieldInsnNode.owner = Mapper.map(null, fieldInsnNode.owner, null, Mapper.Type.Class);
                }
                if (hasType(parseDesc(fieldInsnNode.desc)))
                    fieldInsnNode.desc = desc(fieldInsnNode.desc);
            } else if (instruction instanceof LdcInsnNode) {
                LdcInsnNode ldcInsnNode = (LdcInsnNode) instruction;
                if (ldcInsnNode.cst instanceof Type) {
                    Type type = (Type) ldcInsnNode.cst;
                    String name = type.getClassName();
                    int count = 0;
                    if (name.contains("[]"))
                        while (name.contains("[]")) {
                            name = replaceFirst(name, "[]", "");
                            count++;
                        }
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < count; i++)
                        builder.append("[");
                    ldcInsnNode.cst = Type.getType(builder + "L" + name.replace(name, Mapper.getObfClass(name)).replace('.', '/') + ";");
                }
            } else if (instruction instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode invokeDynamicInsnNode = (InvokeDynamicInsnNode) instruction;
                invokeDynamicInsnNode.desc = DescParser.mapDesc(invokeDynamicInsnNode.desc);
                for (int i = 0; i < invokeDynamicInsnNode.bsmArgs.length; i++) {
                    Object bsmArg = invokeDynamicInsnNode.bsmArgs[i];
                    if (bsmArg instanceof Handle) {
                        Handle handle = (Handle) bsmArg;
                        invokeDynamicInsnNode.bsmArgs[i] = new Handle(handle.getTag(), Mapper.getObfClass(handle.getOwner()), Mapper.map(handle.getOwner(), handle.getName(), handle.getDesc(), Mapper.Type.Method), DescParser.mapDesc(handle.getDesc()), handle.isInterface());
                    } else if (bsmArg instanceof Type) {
                        Type type = (Type) bsmArg;
                        String desc = type.toString();
                        invokeDynamicInsnNode.bsmArgs[i] = Type.getType(DescParser.mapDesc(desc));
                    }
                }
            }
        }
    }

    public static void field(FieldNode node) {
        for (String name : parseDesc(node.desc).split(";"))
            node.desc = replaceFirst(node.desc, name, Mapper.getObfClass(name));
    }

    private static String desc(String desc) {
        for (String name : parseDesc(desc).split(";"))
            desc = replaceFirst(desc, name, Mapper.getObfClass(name));
        return desc;
    }
}
