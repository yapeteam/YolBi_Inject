package cn.yapeteam.yolbi.command.impl;

import cn.yapeteam.loader.api.module.values.Value;
import cn.yapeteam.loader.api.module.values.impl.*;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.command.AbstractCommand;
import cn.yapeteam.yolbi.module.Module;

import java.awt.*;

public class CommandValue extends AbstractCommand {
    public CommandValue() {
        super("value");
    }

    @Override
    public void process(String[] args) {
        if (args.length == 3) {
            Module module = YolBi.instance.getModuleManager().getModuleByName(args[0]);
            if (module != null) {
                Value<?> value = module.getValueByName(args[1]);
                if (value instanceof BooleanValue) {
                    ((BooleanValue) value).setValue(Boolean.parseBoolean(args[2]));
                } else if (value instanceof ColorValue) {
                    ((ColorValue) value).setValue(new Color(Integer.parseInt(args[2])));
                } else if (value instanceof ModeValue) {
                    ((ModeValue<?>) value).setMode(args[2]);
                } else if (value instanceof NumberValue) {
                    Object var = value.getValue();
                    if (var instanceof Integer) {
                        ((NumberValue<Integer>) value).setValue(Integer.parseInt(args[2]));
                    } else if (var instanceof Float) {
                        ((NumberValue<Float>) value).setValue(Float.parseFloat(args[2]));
                    } else if (var instanceof Double) {
                        ((NumberValue<Double>) value).setValue(Double.parseDouble(args[2]));
                    } else if (var instanceof Long) {
                        ((NumberValue<Long>) value).setValue(Long.parseLong(args[2]));
                    }
                } else if (value instanceof TextValue) {
                    ((TextValue) value).setValue(args[2]);
                }
            }
        }
    }
}
