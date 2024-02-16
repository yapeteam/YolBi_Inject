package cn.yapeteam.yolbi.ui.listedclickui.component;

import cn.yapeteam.yolbi.utils.render.RenderUtil;
import cn.yapeteam.yolbi.utils.render.Stencil;
import lombok.Getter;

/**
 * @author TIMER_err
 */
@Getter
public class Limitation {
    public Limitation(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    private final float x, y, width, height;

    public void start() {
        start(() -> RenderUtil.drawRect(x, y, x + width, y + height, -1));
    }

    public void start(Runnable area) {
        Stencil.write(false);
        area.run();
        Stencil.erase(true);
    }

    public void end() {
        Stencil.dispose();
    }
}
