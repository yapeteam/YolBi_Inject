package cn.yapeteam.loader.mixin.operation.test;

import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.ResourceManager;
import cn.yapeteam.loader.mixin.Transformer;
import cn.yapeteam.loader.mixin.operation.impl.InjectOperation;
import cn.yapeteam.loader.utils.ASMUtils;
import org.objectweb.asm_9_2.Label;
import org.objectweb.asm_9_2.MethodVisitor;
import org.objectweb.asm_9_2.Opcodes;
import org.objectweb.asm_9_2.tree.ClassNode;

import java.io.File;
import java.nio.file.Files;

public class test {
    static class CustomLoader extends ClassLoader {
        public Class<?> load(byte[] bytes) {
            return defineClass(null, bytes, 0, bytes.length);
        }
    }

    public static void main(String[] args) throws Throwable {
        byte[] buffer = Files.readAllBytes(new File("net.minecraft.client.renderer.entity.RendererLivingEntity").toPath());//ResourceManager.readStream(InjectOperation.class.getResourceAsStream("/net/minecraft/client/renderer/entity/RendererLivingEntity.class"));
        new CustomLoader().load(buffer);
        if (true) return;
        ClassNode node = ASMUtils.node(buffer);
        node.methods.stream().filter(methodNode -> methodNode.name.equals("doRender")).forEach(m -> m.accept(new MethodVisitor(Opcodes.ASM9) {
            @Override
            public void visitLocalVariable(String varName, String descriptor, String signature, Label start, Label end, int index) {
                System.out.println(varName + ":" + index);
                super.visitLocalVariable(varName, descriptor, signature, start, end, index);
            }
        }));
        if (true) return;
        Mapper.setMode(Mapper.Mode.None);
        Transformer transformer = new Transformer((name) -> ResourceManager.readStream(InjectOperation.class.getResourceAsStream("/" + name.getName().replace('.', '/') + ".class")));
        transformer.addMixin(source.class);
        byte[] bytes = transformer.transform().get("cn.yapeteam.loader.mixin.operation.test.target");
        Files.write(new File("target.class").toPath(), bytes);
        new CustomLoader().load(bytes).getMethod("func").invoke(null);
    }

}
