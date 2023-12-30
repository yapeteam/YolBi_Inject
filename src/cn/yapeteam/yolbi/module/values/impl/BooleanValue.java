package cn.yapeteam.yolbi.module.values.impl;

import cn.yapeteam.yolbi.module.values.Value;
import cn.yapeteam.yolbi.module.values.Visibility;

public class BooleanValue extends Value<Boolean> {
    public BooleanValue(String name, boolean value) {
        super(name);
        this.value = value;
    }

    public BooleanValue(String name, Visibility visibility, boolean value) {
        this(name, value);
        setVisibility(visibility);
    }

    public BooleanValue(String name, String desc, boolean value) {
        this(name, value);
        this.desc = desc;
    }

    public BooleanValue(String name, String desc, Visibility visibility, boolean value) {
        this(name, desc, value);
        setVisibility(visibility);
    }
}
