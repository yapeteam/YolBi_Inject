package cn.yapeteam.yolbi.module;

import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.values.Value;
import cn.yapeteam.loader.api.module.values.impl.*;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.config.Config;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

@Getter
@Setter
public abstract class Module {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    private String name = null;
    private ModuleCategory category = null;
    private int key = 0;

    private boolean enabled = false;

    private boolean listening = false;

    private final ArrayList<Value<?>> values = new ArrayList<>();

    protected void onEnable() {
        //invoke on enabled
    }

    protected void onDisable() {
        //invoke on disabled
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;

            if (enabled) {
                onEnable();
                startListening();
            } else {
                onDisable();
                stopListening();
            }
        }
    }

    public final void toggle() {
        setEnabled(!this.enabled);
    }

    protected final void startListening() {
        if (!listening) {
            YolBi.instance.getEventManager().register(this);
            listening = true;
        }
    }

    protected final void stopListening() {
        if (listening) {
            YolBi.instance.getEventManager().unregister(this);
            listening = false;
        }
    }

    public void addValues(Value<?>... values) {
        this.values.addAll(Arrays.asList(values));
    }

    public Value<?> getValueByName(String name) {
        return values.stream().filter(v -> v.getName().equals(name)).findFirst().orElse(null);
    }

    public String getSuffix() {
        return null;
    }

    public Config getConfig() {
        return new Config(name) {
            @Override
            public void save(JsonObject content) {
                content.addProperty("enabled", enabled);
                content.addProperty("key", key);
                for (Value<?> value : values) {
                    if (value.getValue() != null)
                        if (value instanceof BooleanValue)
                            content.addProperty(value.getName(), ((BooleanValue) value).getValue());
                        else if (value instanceof ColorValue)
                            content.addProperty(value.getName(), ((ColorValue) value).getColor());
                        else if (value instanceof ModeValue)
                            content.addProperty(value.getName(), ((ModeValue<?>) value).getValue().toString());
                        else if (value instanceof NumberValue)
                            content.addProperty(value.getName(), ((NumberValue<?>) value).getValue());
                        else if (value instanceof TextValue)
                            content.addProperty(value.getName(), ((TextValue) value).getValue());
                }
            }

            @Override
            public void load(JsonObject content) {
                JsonElement enabled = content.get("enabled");
                setEnabled(enabled != null && enabled.getAsBoolean());
                JsonElement key = content.get("key");
                if (key != null) setKey(key.getAsInt());
                for (Value<?> value : values) {
                    JsonElement val = content.get(value.getName());
                    if (val == null) continue;
                    if (value instanceof BooleanValue)
                        ((BooleanValue) value).setValue(val.getAsBoolean());
                    else if (value instanceof ColorValue)
                        ((ColorValue) value).setValue((new Color(val.getAsInt())));
                    else if (value instanceof ModeValue)
                        ((ModeValue<?>) value).setMode(val.getAsString());
                    else if (value instanceof NumberValue)
                        ((NumberValue<?>) value).setValue(val.getAsNumber(), false);
                    else if (value instanceof TextValue)
                        ((TextValue) value).setValue(val.getAsString());
                }
            }
        };
    }
}
