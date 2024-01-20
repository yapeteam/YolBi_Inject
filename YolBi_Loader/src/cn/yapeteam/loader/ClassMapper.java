package cn.yapeteam.loader;

import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Shadow;
import cn.yapeteam.loader.mixin.annotations.Super;
import cn.yapeteam.loader.mixin.utils.DescParser;
import cn.yapeteam.loader.utils.ASMUtils;
import lombok.AllArgsConstructor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
        ArrayList<Name_Desc> methodShadows = new ArrayList<>();
        ArrayList<Name_Desc> fieldShadows = new ArrayList<>();
        String targetName = null;
        if (node.visibleAnnotations != null) {
            targetName = ASMUtils.getAnnotationValue(
                    node.visibleAnnotations.stream()
                            .filter(a -> a.desc.contains(ASMUtils.slash(Mixin.class.getName())))
                            .findFirst().orElse(null), "value"
            );
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
                        methodInsnNode.owner = targetName;
                } else if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction;
                    if (fieldShadows.stream().anyMatch(m -> m.name.equals(fieldInsnNode.name) && m.desc.equals(fieldInsnNode.desc)))
                        fieldInsnNode.owner = targetName;
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
        return Mapper.getMappings().stream().anyMatch(m -> m.getType() == Mapper.Type.Class && m.getName().equals(type));
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
