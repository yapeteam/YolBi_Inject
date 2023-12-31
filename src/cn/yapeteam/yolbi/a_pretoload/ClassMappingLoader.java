package cn.yapeteam.yolbi.a_pretoload;

import cn.yapeteam.yolbi.a_pretoload.logger.Logger;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Super;
import cn.yapeteam.yolbi.a_pretoload.mixin.utils.DescParser;
import cn.yapeteam.yolbi.a_pretoload.utils.ASMUtils;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ClassMappingLoader {
    public static void main(String[] args) throws Throwable {
        Mapper.setMode(Mapper.Mode.Vanilla);
        ResourceManager.add("joined.srg", Files.readAllBytes(new File("resources/joined.srg").toPath()));
        ResourceManager.add("fields.csv", Files.readAllBytes(new File("resources/fields.csv").toPath()));
        ResourceManager.add("methods.csv", Files.readAllBytes(new File("resources/methods.csv").toPath()));
        Mapper.readMappings();
        loadClass(Files.readAllBytes(new File("cn.yapeteam.yolbi.module.impl.KillAura").toPath()));
    }

    public static void loadClass(byte[] bytes) throws Throwable {
        ClassNode node = ASMUtils.node(bytes);
        node.superName = Mapper.getObfClass(node.superName);
        List<String> interfaces = new ArrayList<>();
        for (String anInterface : node.interfaces)
            interfaces.add(Mapper.getObfClass(anInterface));
        node.interfaces = interfaces;
        for (MethodNode method : node.methods)
            method(method, node);
        for (FieldNode field : node.fields)
            field(field);
        bytes = ASMUtils.rewriteClass(node);
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            Method method = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(loader, bytes, 0, bytes.length);
        } catch (Throwable e) {
            Logger.exception(e);
        }
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
        return Mapper.getMappings().stream().anyMatch(m -> m.getType() == Mapper.Type.Class && m.getName().equals(type));
    }

    public static void method(MethodNode source, ClassNode parent) throws Throwable {
        if (source.visibleAnnotations != null) {
            for (AnnotationNode visibleAnnotation : source.visibleAnnotations) {
                if (visibleAnnotation.desc.substring(1, visibleAnnotation.desc.length() - 1).equals(ASMUtils.slash(Super.class.getName()))) {
                    source.name = Mapper.map(parent.superName, source.name, null, Mapper.Type.Method);
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
                    ldcInsnNode.cst = Type.getType("L" + type.getClassName().replace(name, Mapper.getObfClass(name)).replace('.', '/') + ";");
                }
            } else if (instruction instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode invokeDynamicInsnNode = (InvokeDynamicInsnNode) instruction;
                for (int i = 0; i < invokeDynamicInsnNode.bsmArgs.length; i++) {
                    Object bsmArg = invokeDynamicInsnNode.bsmArgs[i];
                    if (bsmArg instanceof Handle) {
                        Handle handle = (Handle) bsmArg;
                        invokeDynamicInsnNode.bsmArgs[i] = new Handle(handle.getTag(), handle.getOwner(), handle.getName(), DescParser.mapDesc(handle.getDesc()), handle.isInterface());
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
