package cn.yapeteam.yolbi.module.impl.visual;

import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.ModuleInfo;
import cn.yapeteam.loader.api.module.values.impl.BooleanValue;
import cn.yapeteam.loader.api.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.ui.listedclickui.ImplScreen;
import cn.yapeteam.yolbi.utils.reflect.ReflectUtil;
import lombok.Getter;
import org.lwjgl.input.Keyboard;

@Getter
@ModuleInfo(name = "ClickUI", category = ModuleCategory.VISUAL, key = Keyboard.KEY_RSHIFT)
public class ClickUI extends Module {
    private final BooleanValue pauseGame = new BooleanValue("PauseGame", true);
    private final BooleanValue blur = new BooleanValue("Blur background", () -> !mc.gameSettings.ofFastRender, true);
    private final BooleanValue rainbow = new BooleanValue("RainBow", false);
    private final NumberValue<Integer> blurRadius = new NumberValue<>("blurRadius", blur::getValue, 3, 0, 50, 1);

    public ClickUI() {
        if (ReflectUtil.hasOptifine)
            blur.setCallback((oldV, newV) -> !mc.gameSettings.ofFastRender && newV);
        else blur.setVisibility(() -> true);
        addValues(pauseGame, blur, rainbow, blurRadius);
    }

    @Getter
    private final ImplScreen screen = new ImplScreen();

    @Override
    protected void onEnable() {
        setEnabled(false);
        if (ReflectUtil.hasOptifine && mc.gameSettings.ofFastRender)
            blur.setValue(false);
        mc.displayGuiScreen(screen);
    }
}
