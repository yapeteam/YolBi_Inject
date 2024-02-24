package cn.yapeteam.loader.mixin.operation.test;

import cn.yapeteam.loader.mixin.annotations.Inject;
import cn.yapeteam.loader.mixin.annotations.Local;
import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Target;

@Mixin(target.class)
public class source {
    @Inject(method = "func", desc = "()V", target = @Target(value = "INVOKEVIRTUAL", target = "java/io/PrintStream.println(F)V"))
    public static void func(@Local(source = "yy", index = 0) float yy, @Local(source = "yy2", index = 1) float yy2, @Local(source = "yy3", index = 2) float yy3) {
        int u = 100;
        yy = 15;
        yy2 = 13;
        yy3 = yy2 - yy;
    }
}
