package cn.yapeteam.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

public class Bootstrap {
    public static void agentmain(String args, Instrumentation instrumentation) throws Throwable {
        URLClassLoader loader = null;
        for (Object o : Thread.getAllStackTraces().keySet().toArray()) {
            Thread thread = (Thread) o;
            if (thread.getName().equals("Client thread")) {
                loader = (URLClassLoader) thread.getContextClassLoader();
                Thread.currentThread().setContextClassLoader(loader);
                break;
            }
        }

        String yolbi_dir = new File(System.getProperty("user.home"), ".yolbi").getAbsolutePath();
        for (File file : Objects.requireNonNull(new File(yolbi_dir).listFiles()))
            if (file.getName().endsWith(".jar") && !file.getName().equals("injection.jar")) {
                System.out.println(file.getAbsolutePath());
                loadJar(loader, file);
            }
        Class.forName("cn.yapeteam.loader.InstrumentationWrapper", true, loader).getConstructor(Instrumentation.class).newInstance(instrumentation);
        Class.forName("cn.yapeteam.loader.Loader", true, loader).getMethod("preload", String.class).invoke(null, yolbi_dir);
        loadJar(loader, new File(yolbi_dir, "injection.jar"));
        Class.forName("cn.yapeteam.yolbi.Loader", true, loader).getMethod("start").invoke(null);
    }

    private static void loadJar(URLClassLoader urlClassLoader, File jar) throws Throwable {
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(urlClassLoader, jar.toURI().toURL());
    }
}
