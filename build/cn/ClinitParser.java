package cn;

import org.objectweb.asm.*;

import java.io.InputStream;
import java.util.ArrayList;

public class ClinitParser {
    public static ArrayList<String> parse(InputStream in, ArrayList<String> list) {
        try {
            ClassReader classReader = new ClassReader(in);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

            ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9, classWriter) {
                @Override
                public void visit
                        (int version, int access, String name, String signature, String superName, String[] interfaces) {
                    String clazzname = superName.replace("/", ".");
                    if (!list.contains(clazzname))
                        list.add(clazzname);
                    for (String anInterface : interfaces) {
                        String InterfaceName = anInterface.replace("/", ".");
                        if (!list.contains(InterfaceName))
                            list.add(InterfaceName);
                    }
                    super.visit(version, access, name, signature, superName, interfaces);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if ("<clinit>".equals(name)) {
                        return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                            @Override
                            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                                if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) {
                                    String clazzname = descriptor.replace("[", "").substring(1).replace(";", "").replace("/", ".");
                                    if (!list.contains(clazzname) && !clazzname.isEmpty()) {
                                        list.add(clazzname);
                                    }
                                }
                                super.visitFieldInsn(opcode, owner, name, descriptor);
                            }
                        };
                    }
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
            };

            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        } catch (Exception ignored) {
        }
        return list;
    }
}
