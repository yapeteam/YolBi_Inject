package cn.yapeteam.loader.mixin.operation.impl;

import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.ResourceManager;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.mixin.Mixin;
import cn.yapeteam.loader.mixin.Transformer;
import cn.yapeteam.loader.mixin.annotations.Overwrite;
import cn.yapeteam.loader.mixin.operation.Operation;
import cn.yapeteam.loader.mixin.operation.test.source;
import cn.yapeteam.loader.mixin.utils.DescParser;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static void processLocalValues(MethodNode source, MethodNode target) {
        int max_index = 0;
        for (AbstractInsnNode instruction : target.instructions) {
            if (instruction instanceof VarInsnNode && (Operation.isLoadOpe(instruction.getOpcode()) || Operation.isStoreOpe(instruction.getOpcode()))) {
                VarInsnNode varInsnNode = (VarInsnNode) instruction;
                max_index = Math.max(max_index, varInsnNode.var);
            }
        }

        Map<Integer, Integer> varMap = new HashMap<>();
        //Process local var store & load
        for (int i = 0; i < source.instructions.size(); i++) {
            AbstractInsnNode instruction = source.instructions.get(i);
            if (instruction instanceof VarInsnNode && Operation.isStoreOpe(instruction.getOpcode())) {
                VarInsnNode varInsnNode = (VarInsnNode) instruction;
                varMap.put(varInsnNode.var, varInsnNode.var += max_index);
            }
        }
        for (int i = 0; i < source.instructions.size(); i++) {
            AbstractInsnNode instruction = source.instructions.get(i);
            if (instruction instanceof VarInsnNode && Operation.isLoadOpe(instruction.getOpcode())) {
                VarInsnNode varInsnNode = (VarInsnNode) instruction;
                Integer index = varMap.get(varInsnNode.var);
                if (index != null)
                    varInsnNode.var = index;
            } else if (instruction instanceof IincInsnNode) {
                IincInsnNode iincInsnNode = (IincInsnNode) instruction;
                Integer index = varMap.get(iincInsnNode.var);
                if (index != null)
                    iincInsnNode.var = index;
            }
        }
    }

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();
        List<MethodNode> replacements = source.methods.stream()
                .filter(Overwrite.Helper::hasAnnotation)
                .collect(Collectors.toList());
        for (MethodNode replacement : replacements) {
            MethodNode targetMethod = Operation.findTargetMethod(target.methods, mixin.getTargetName(), replacement.name, DescParser.mapDesc(replacement.desc));
            if (targetMethod == null) {
                Logger.error("No method found: {} in {}", Mapper.mapWithSuper(mixin.getTargetName(), replacement.name, DescParser.mapDesc(replacement.desc), Mapper.Type.Method) + DescParser.mapDesc(replacement.desc), target.name);
                return;
            }
            processLocalValues(replacement, targetMethod);
            targetMethod.instructions = replacement.instructions;
        }
    }
}
