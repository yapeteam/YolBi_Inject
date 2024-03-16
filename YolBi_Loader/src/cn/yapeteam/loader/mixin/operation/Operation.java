package cn.yapeteam.loader.mixin.operation;


import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.mixin.Mixin;
import cn.yapeteam.loader.mixin.utils.DescParser;
import org.objectweb.asm_9_2.Opcodes;
import org.objectweb.asm_9_2.tree.MethodNode;

import java.lang.reflect.Field;
import java.util.List;

public interface Operation {
    void dispose(Mixin mixin);

    static boolean isLoadOpe(int opcode) {
        for (Field field : Opcodes.class.getFields())
            if (field.getName().endsWith("LOAD"))
                try {
                    if ((int) field.get(null) == opcode)
                        return true;
                } catch (Throwable ignored) {
                }
        return false;
    }

    static boolean isStoreOpe(int opcode) {
        for (Field field : Opcodes.class.getFields())
            if (field.getName().endsWith("STORE"))
                try {
                    if ((int) field.get(null) == opcode)
                        return true;
                } catch (Throwable ignored) {
                }
        return false;
    }

    static MethodNode findTargetMethod(List<MethodNode> list, String owner, String name, String desc) {
        name = Mapper.mapWithSuper(owner, name, desc, Mapper.Type.Method);
        desc = DescParser.mapDesc(desc);
        String finalName = name;
        String finalDesc = desc;
        return list.stream().filter(m -> m.name.equals(finalName) && m.desc.equals(finalDesc)).findFirst().orElse(null);
    }
}
