package cn.yapeteam.yolbi.module;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.values.Value;
import com.mojang.realmsclient.gui.ChatFormatting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

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

    public final String getDisplayName(ChatFormatting formatting) {
        String tag = getSuffix();

        if (tag == null || tag.isEmpty()) {
            return name;
        }

        return name + formatting + " " + tag;
    }
}
