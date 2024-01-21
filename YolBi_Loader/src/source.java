import cn.yapeteam.loader.mixin.annotations.Inject;
import cn.yapeteam.loader.mixin.annotations.Local;
import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Target;

import java.io.PrintStream;

@Mixin(target.class)
public class source {
    @Inject(method = "target", desc = "()V", hasReturn = false, target = @Target(value = "GETSTATIC", shift = Target.Shift.AFTER))
    public static void source(@Local(source = "ps", target = "ps") PrintStream ps) {
        PrintStream ps2 = System.out;
        Object[] array = Thread.getAllStackTraces().keySet().toArray();
        for (Object object : array) {
            ps2.println(((Thread) object).getName());
        }
        ps2.println(ps.getClass());
    }
}
