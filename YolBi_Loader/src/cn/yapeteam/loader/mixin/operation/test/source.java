package cn.yapeteam.loader.mixin.operation.test;

import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Overwrite;

@Mixin(target.class)
public class source {
    @Overwrite
    public static void func(int i) {
        int u = 0;
        for (int j = 0; j < 1000; j++) {
            u++;
        }
        System.out.println(i + 100 + u);
    }
}
