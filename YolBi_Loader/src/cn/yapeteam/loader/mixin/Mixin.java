package cn.yapeteam.loader.mixin;

import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.utils.ASMUtils;
import lombok.Getter;
import org.objectweb.asm.tree.ClassNode;

@Getter
public class Mixin {
    private final ClassNode source;
    private final ClassNode target;
    private final String targetName;

    public Mixin(ClassNode source, ClassProvider provider) throws Throwable {
        this.source = source;
        targetName = ASMUtils.getAnnotationValue(
                source.visibleAnnotations.stream()
                        .filter(a -> a.desc.contains(ASMUtils.slash(cn.yapeteam.loader.mixin.annotations.Mixin.class.getName())))
                        .findFirst().orElse(null), "value"
        );
        if (targetName == null)
            throw new NullPointerException("Mixin value is null!");
        target = ASMUtils.node(provider.getClassBytes(Mapper.map(null, ASMUtils.slash(targetName), null, Mapper.Type.Class)));
    }
}
