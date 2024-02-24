package cn.yapeteam.loader.mixin.operation.test;

public class target {
    public static void func() {
        float yy = 10;
        float yy2 = 11;
        float yy3 = yy2 - yy;
        System.out.println(yy3);
    }
}
