package cn.yapeteam.yolbi.module;

import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.ModuleInfo;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventKey;
import cn.yapeteam.yolbi.notification.Notification;
import cn.yapeteam.yolbi.notification.NotificationType;
import cn.yapeteam.yolbi.utils.animation.Easing;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Getter
@SuppressWarnings({"unchecked", "unused"})
public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void load() {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(new File(YolBi.YOLBI_DIR, "injection.jar").toPath()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String name = entry.getName().replace('/', '.');
                    name = name.substring(0, name.length() - 6);
                    if (name.startsWith("cn.yapeteam.yolbi.module.impl."))
                        try {
                            Class<?> aClass = Class.forName(name);
                            if (aClass.getSuperclass() == Module.class && aClass.getAnnotation(ModuleInfo.class) != null && aClass.getAnnotation(Deprecated.class) == null)
                                registerModule(aClass);
                        } catch (Throwable e) {
                            Logger.exception(e);
                        }
                }
            }
        } catch (IOException e) {
            Logger.exception(e);
        }
        modules.sort((m1, m2) -> -Integer.compare(m2.getName().charAt(0), m1.getName().charAt(0)));
    }

    @Listener
    private void onKey(EventKey e) {
        modules.stream().filter(m -> m.getKey() == e.getKey()).collect(Collectors.toList()).forEach(module -> {
            module.toggle();
            YolBi.instance.getNotificationManager().post(new Notification(
                    "Module: " + module.getName() + " toggled",
                    Easing.EASE_OUT_BACK, Easing.EASE_IN_OUT_CUBIC,
                    1500, module.isEnabled() ? NotificationType.SUCCESS : NotificationType.FAILED
                    )
            );
        });
    }

    private void registerModule(@NotNull Class<?> aClass) {
        if (aClass.getAnnotation(Deprecated.class) == null && aClass.getAnnotation(ModuleInfo.class) != null && modules.stream().noneMatch(module -> module.getClass() == aClass)) {
            try {
                Module module = (Module) aClass.newInstance();
                ModuleInfo info = aClass.getAnnotation(ModuleInfo.class);
                if (module.getName() == null)
                    module.setName(info.name());
                if (module.getCategory() == null)
                    module.setCategory(info.category());
                if (module.getKey() == 0)
                    module.setKey(info.key());
                modules.add(module);
                YolBi.instance.getConfigManager().registerConfig(module.getConfig());
            } catch (Throwable e) {
                Logger.error("Failed to load Module: {}", aClass.getSimpleName());
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