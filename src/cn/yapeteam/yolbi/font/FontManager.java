package cn.yapeteam.yolbi.font;

import cn.yapeteam.yolbi.font.cfont.CFontRenderer;
import lombok.Getter;

import java.awt.*;

@Getter
public class FontManager {
    private final CFontRenderer JelloRegular18 = new CFontRenderer("JelloRegular.ttf", 18, Font.PLAIN, true, true);
    private final CFontRenderer JelloLight18 = new CFontRenderer("JelloLight.ttf", 18, Font.PLAIN, true, true);
    private final CFontRenderer JelloMedium18 = new CFontRenderer("JelloMedium.ttf", 18, Font.PLAIN, true, true);
}
