package cn.yapeteam.loader.mixin.annotations;

import cn.yapeteam.loader.utils.ASMUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
public @interface Inject {
    String method();

    String desc();

    Target target();

    boolean hasReturn();

    class Helper {
        public static Inject fromNode(AnnotationNode annotation) {
            return new Inject() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return Inject.class;
                }

                @Override
                public String method() {
                    return ASMUtils.getAnnotationValue(annotation, "method");
                }

                @Override
                public String desc() {
                    return ASMUtils.getAnnotationValue(annotation, "desc");
                }

                @Override
                public boolean hasReturn() {
                    return Boolean.parseBoolean(Objects.requireNonNull(ASMUtils.getAnnotationValue(annotation, "hasReturn")).toString());
                }

                @Override
                public Target target() {
                    AnnotationNode annotationNode = ASMUtils.getAnnotationValue(annotation, "target");
                    if (annotationNode == null) return null;
                    return Target.Helper.fromNode(annotationNode);
                }
            };
        }

        public static boolean isAnnotation(@NotNull AnnotationNode node) {
            return node.desc.contains(ASMUtils.slash(Inject.class.getName()));
        }

        public static boolean hasAnnotation(@NotNull MethodNode node) {
            return node.visibleAnnotations != null && node.visibleAnnotations.stream().anyMatch(Helper::isAnnotation);
        }

        public static @Nullable Inject getAnnotation(MethodNode node) {
            if (!hasAnnotation(node)) return null;
            return fromNode(node.visibleAnnotations.stream().filter(Helper::isAnnotation).findFirst().orElse(null));
        }
    }
}
