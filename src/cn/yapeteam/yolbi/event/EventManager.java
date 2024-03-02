package cn.yapeteam.yolbi.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    private final ArrayList<Object> listeningObjects = new ArrayList<>();
    private final ArrayList<Class<?>> listeningClasses = new ArrayList<>();
    private final CopyOnWriteArrayList<ListeningMethod> listeningMethods = new CopyOnWriteArrayList<>();

    public void register(Object o) {
        if (!listeningObjects.contains(o))
            listeningObjects.add(o);

        updateListeningMethods();
    }

    public void unregister(Object o) {
        listeningObjects.remove(o);

        updateListeningMethods();
    }

    public void register(Class<?> c) {
        if (!listeningClasses.contains(c)) {
            listeningClasses.add(c);
        }

        updateListeningMethods();
    }

    public void unregister(Class<?> c) {
        listeningClasses.remove(c);

        updateListeningMethods();
    }

    private void updateListeningMethods() {
        listeningMethods.clear();
        listeningObjects.forEach(o -> {
            Class<?> clazz = o.getClass();
            while (clazz != Object.class) {
                Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(Listener.class) && m.getParameterCount() == 1)
                        .forEach(m -> listeningMethods.add(new ListeningMethod(m, o)));
                clazz = clazz.getSuperclass();
            }
        });
        listeningClasses.forEach(c -> Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Listener.class) && m.getParameters().length == 1).forEach(m -> listeningMethods.add(new ListeningMethod(m, null))));
        listeningMethods.sort(Comparator.comparingInt(m -> m.method.getAnnotation(Listener.class).value().getLevel()));
    }

    public <E extends Event> E post(Event e) {
        listeningMethods.forEach(m -> Arrays.stream(m.method.getParameters()).filter(p -> p.getType().equals(e.getClass())).forEach(p -> {
            try {
                m.method.invoke(m.instance, e);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }));
        return (E) e;
    }

    private static class ListeningMethod {
        private final Method method;
        private final Object instance;

        private ListeningMethod(Method method, Object instance) {
            method.setAccessible(true);
            this.method = method;
            this.instance = instance;
        }
    }
}