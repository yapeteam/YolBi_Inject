package cn.yapeteam.loader.mixin;

import cn.yapeteam.loader.SocketSender;
import cn.yapeteam.loader.mixin.operation.Operation;
import cn.yapeteam.loader.mixin.operation.impl.InjectOperation;
import cn.yapeteam.loader.mixin.operation.impl.OverwriteOperation;
import cn.yapeteam.loader.utils.ASMUtils;
import cn.yapeteam.loader.utils.ClassUtils;
import lombok.Getter;
import org.objectweb.asm_9_2.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Transformer {
    private final ClassProvider provider;
    private final ArrayList<Mixin> mixins;
    private final ArrayList<Operation> operations;

    public Transformer(ClassProvider classProvider) {
        this.provider = classProvider;
        this.mixins = new ArrayList<>();
        this.operations = new ArrayList<>();
        operations.add(new InjectOperation());
        operations.add(new OverwriteOperation());
    }

    public void addMixin(Class<?> theClass) throws Throwable {
        byte[] bytes = ClassUtils.getClassBytes(theClass.getName());
        ClassNode source = ASMUtils.node(bytes);
        mixins.add(new Mixin(source, theClass, provider));
    }

    public Map<String, byte[]> transform() {
        Map<String, byte[]> classMap = new HashMap<>();
        for (Mixin mixin : mixins) {
            for (Operation operation : operations)
                operation.dispose(mixin);
            String name = mixin.getTarget().name.replace('/', '.');
            byte[] class_bytes = ASMUtils.rewriteClass(mixin.getTarget());
            classMap.put(name, class_bytes);
        }
        SocketSender.send("E2");
        return classMap;
    }
}
