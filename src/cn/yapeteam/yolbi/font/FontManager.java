package cn.yapeteam.yolbi.font;

import cn.yapeteam.yolbi.font.cfont.CFontRenderer;
import lombok.Getter;

import java.awt.*;

@Getter
public class FontManager {
    private final CFontRenderer JelloRegular18;
    private final CFontRenderer JelloLight18;
    private final CFontRenderer JelloMedium18;

    public FontManager() {
        JelloRegular18 = new CFontRenderer("JelloRegular.ttf", 18, Font.PLAIN, true, true);
        JelloLight18 = new CFontRenderer("JelloLight.ttf", 18, Font.PLAIN, true, true);
        JelloMedium18 = new CFontRenderer("JelloMedium.ttf", 18, Font.PLAIN, true, true);
    }
}
