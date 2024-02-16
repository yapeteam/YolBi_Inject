package cn.yapeteam.yolbi.font;

import cn.yapeteam.yolbi.font.cfont.CFontRenderer;
import lombok.Getter;

import java.awt.*;

@Getter
public class FontManager {
    private final AbstractFontRenderer JelloRegular18 = new CFontRenderer("JelloRegular.ttf", 18, Font.PLAIN, true, true);
    private final AbstractFontRenderer JelloLight18 = new CFontRenderer("JelloLight.ttf", 18, Font.PLAIN, true, true);
    private final AbstractFontRenderer JelloMedium18 = new CFontRenderer("JelloMedium.ttf", 18, Font.PLAIN, true, true);
    private final AbstractFontRenderer PingFang12 = new CFontRenderer("PingFang_Normal.ttf", 12, Font.PLAIN, true, true);
    private final AbstractFontRenderer PingFang14 = new CFontRenderer("PingFang_Normal.ttf", 14, Font.PLAIN, true, true);
    private final AbstractFontRenderer PingFang18 = new CFontRenderer("PingFang_Normal.ttf", 18, Font.PLAIN, true, true);
    private final AbstractFontRenderer PingFangBold18 = new CFontRenderer("PingFang_Bold.ttf", 18, Font.PLAIN, true, true);
    private final AbstractFontRenderer FLUXICON14 = new CFontRenderer("fluxicon.ttf", 18, Font.PLAIN, true, true);
    private final AbstractFontRenderer default18 = new CFontRenderer(new Font(null, Font.PLAIN, 18), true, true);
}
