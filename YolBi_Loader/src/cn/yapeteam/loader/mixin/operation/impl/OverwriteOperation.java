package cn.yapeteam.loader.mixin.operation.impl;

import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.ResourceManager;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.mixin.Mixin;
import cn.yapeteam.loader.mixin.Transformer;
import cn.yapeteam.loader.mixin.annotations.Overwrite;
import cn.yapeteam.loader.mixin.operation.Operation;
import cn.yapeteam.loader.mixin.operation.test.source;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.stream.Collectors;

public class OverwriteOperation implements Operation {
    static class CustomLoader extends ClassLoader {
        public Class<?> load(byte[] bytes) {
            return defineClass(null, bytes, 0, bytes.length);
        }
    }

    public static void main(String[] args) throws Throwable {
        Mapper.setMode(Mapper.Mode.None);
        Transformer transformer = new Transformer((name) -> ResourceManager.readStream(InjectOperation.class.getResourceAsStream("/" + name.getName().replace('.', '/') + ".class")));
        transformer.addMixin(source.class);
        byte[] bytes = transformer.transform().get("cn.yapeteam.loader.mixin.operation.test.target");
        new CustomLoader().load(bytes).getMethod("func", int.class).invoke(null, 666);
    }

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();
        List<MethodNode> replacements = source.methods.stream()
                .filter(Overwrite.Helper::hasAnnotation)
                .collect(Collectors.toList());
        for (MethodNode replacement : replacements) {
            Overwrite info = Overwrite.Helper.getAnnotation(replacement);
            if (info == null) continue;
            MethodNode targetMethod = Operation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) {
                Logger.error("No method found: {} in {}", Mapper.mapWithSuper(mixin.getTargetName(), info.method(), info.desc(), Mapper.Type.Method) + info.desc(), target.name);
                return;
            }
            targetMethod.instructions = replacement.instructions;
        }
    }
}
