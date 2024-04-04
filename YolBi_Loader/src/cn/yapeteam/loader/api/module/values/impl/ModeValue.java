package cn.yapeteam.loader.api.module.values.impl;

import cn.yapeteam.loader.api.module.values.Value;
import cn.yapeteam.loader.api.module.values.Visibility;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Setter
@Getter
public class ModeValue<T> extends Value<T> {
    private T[] modes;

    @SafeVarargs
    public ModeValue(String name, T current, T... modes) {
        super(name);
        this.value = current;
        this.modes = modes;
    }

    @SafeVarargs
    public ModeValue(String name, Visibility visibility, T current, T... modes) {
        super(name);
        this.value = current;
        this.modes = modes;
        setVisibility(visibility);
    }

    public boolean is(T mode) {
        return getValue().equals(mode);
    }

    public void setMode(String str) {
        value = getCallback() != null ? getCallback().run(value, getMode(str)) : getMode(str);
    }

    public T getMode(String name) {
        return Arrays.stream(modes).filter(m -> m.toString().equals(name)).findFirst().orElse(null);
    }

    public void increment() {
        int index = Arrays.asList(modes).indexOf(getValue());
        setValue(modes[index < modes.length - 1 ? index + 1 : 0]);
    }

    public void decrement() {
        int index = Arrays.asList(modes).indexOf(getValue());
        setValue(modes[index > 0 ? index - 1 : modes.length - 1]);
    }
}
