package cn.yapeteam.yolbi.module;

import cn.yapeteam.yolbi.a_pretoload.logger.Logger;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventKey;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        try {
            Field loadedClasses = ClassLoader.class.getDeclaredField("classes");
            loadedClasses.setAccessible(true);
            Vector<Class<?>> vector = (Vector<Class<?>>) loadedClasses.get(Module.class.getClassLoader());
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < vector.size(); i++) {
                Class<?> aClass = vector.get(i);
                if (aClass.getSuperclass() == Module.class && aClass.getAnnotation(ModuleInfo.class) != null && aClass.getAnnotation(Deprecated.class) == null)
                    registerModule((Class<? extends Module>) aClass);
            }
        } catch (Throwable e) {
            Logger.exception(e);
        }
        modules.sort((m1, m2) -> -Integer.compare(m2.getName().charAt(0), m1.getName().charAt(0)));
    }

    @Listener
    private void onKey(EventKey e) {
        modules.stream().filter(m -> m.getKey() == e.getKey()).collect(Collectors.toList()).forEach(Module::toggle);
    }

    private void registerModule(@NotNull Class<? extends Module> aClass) {
        if (aClass.getAnnotation(Deprecated.class) == null && aClass.getAnnotation(ModuleInfo.class) != null && modules.stream().noneMatch(module -> module.getClass() == aClass)) {
            try {
                Module module = aClass.getConstructor().newInstance();
                ModuleInfo info = aClass.getAnnotation(ModuleInfo.class);
                if (module.getName() == null)
                    module.setName(info.name());
                if (module.getCategory() == null)
                    module.setCategory(info.category());
                if (module.getKey() == 0)
                    module.setKey(info.key());
                modules.add(module);
            } catch (Throwable e) {
                System.err.println("Failed to load Module: " + aClass.getSimpleName());
                Logger.exception(e);
            }
        }
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        return (T) modules.stream().filter(m -> m.getClass().equals(clazz)).findFirst().orElse(null);
    }

    public <T extends Module> T getModuleByName(String name) {
        return (T) modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        return modules.stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());
    }
}