package cn.yapeteam.loader.mixin.operation.impl;

import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.mixin.Mixin;
import cn.yapeteam.loader.mixin.annotations.Overwrite;
import cn.yapeteam.loader.mixin.operation.Operation;
import org.objectweb.asm_9_2.tree.ClassNode;
import org.objectweb.asm_9_2.tree.MethodNode;

import java.util.List;
import java.util.stream.Collectors;

public class OverwriteOperation implements Operation {
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
