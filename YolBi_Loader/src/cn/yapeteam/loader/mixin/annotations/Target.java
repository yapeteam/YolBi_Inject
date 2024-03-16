package cn.yapeteam.loader.mixin.annotations;

import cn.yapeteam.loader.utils.ASMUtils;
import org.objectweb.asm_9_2.tree.AnnotationNode;
import org.objectweb.asm_9_2.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.PARAMETER)
public @interface Target {
    enum Shift {
        AFTER, BEFORE
    }

    String value();

    String target() default "";

    Shift shift() default Shift.BEFORE;

    class Helper {
        public static Target fromNode(AnnotationNode annotation) {
            return new Target() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return Target.class;
                }

                @Override
                public String value() {
                    return ASMUtils.getAnnotationValue(annotation, "value");
                }

                @Override
                public String target() {
                    Object value = ASMUtils.getAnnotationValue(annotation, "target");
                    if (value == null) return "";
                    return (String) value;
                }

                @Override
                public Shift shift() {
                    Object value = ASMUtils.getAnnotationValue(annotation, "shift");
                    if (value == null) return Shift.BEFORE;
                    return Shift.valueOf(((String[]) value)[1]);
                }
            };
        }

        public static boolean isAnnotation(AnnotationNode node) {
            return node.desc.contains(ASMUtils.slash(Target.class.getName()));
        }

        public static boolean hasAnnotation(MethodNode node) {
            return node.visibleAnnotations.stream().anyMatch(Helper::isAnnotation);
        }

        public static Target getAnnotation(MethodNode node) {
            if (!hasAnnotation(node)) return null;
            return fromNode(node.visibleAnnotations.stream().filter(Helper::isAnnotation).findFirst().orElse(null));
        }
    }
}
