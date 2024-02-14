package cn.yapeteam.yolbi.font;

import java.awt.*;

public interface AbstractFontRenderer {
    double getStringWidth(String text);

    double getStringHeight(String text);

    double getStringHeight();

    void drawStringWithShadow(String text, double x, double y, int color);

    void drawStringWithShadow(String text, double x, double y, Color color);

    void drawCenteredString(String text, double x, double y, int color);

    double drawCenteredStringWithShadow(String text, double x, double y, int color);

    void drawCenteredString(String text, double x, double y, Color color);

    double drawString(String text, double x, double y, int color, boolean shadow);

    void drawString(String text, double x, double y, Color color);

    double drawString(String text, double x, double y, int color);

    double getHeight();
}
