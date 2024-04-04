package cn.yapeteam.loader.api.module.values.impl;

import cn.yapeteam.loader.api.module.values.Value;
import cn.yapeteam.loader.api.module.values.Visibility;
import lombok.Getter;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ColorValue extends Value<Color> {

    @Getter
    private float hue;
    @Getter
    private float saturation;
    @Getter
    private float brightness;
    @Getter
    private float alpha;
    private int color;

    public ColorValue(String name, int color) {
        super(name);
        this.name = name;
        this.color = color;
        this.value = intToColor(color);
        float[] hsb = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = value.getAlpha() / 255f;
    }

    public ColorValue(String name, Visibility visibility, int color) {
        super(name);
        this.name = name;
        setVisibility(visibility);
        this.color = color;
        this.value = intToColor(color);
        float[] hsb = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = value.getAlpha() / 255f;
    }

    public ColorValue(String name, String desc, Visibility visibility, int color) {
        super(name);
        this.name = name;
        this.desc = desc;
        setVisibility(visibility);
        this.color = color;
        this.value = intToColor(color);
        float[] hsb = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = value.getAlpha() / 255f;
    }

    public int getColor() {
        return value.getRGB();
    }

    @Override
    public void setValue(Color value) {
        super.setValue(value);
        float[] hsb = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = value.getAlpha() / 255f;
    }

    public static void drawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (float) (col1 >> 24 & 255) / 255.0f;
        float f1 = (float) (col1 >> 16 & 255) / 255.0f;
        float f2 = (float) (col1 >> 8 & 255) / 255.0f;
        float f3 = (float) (col1 & 255) / 255.0f;
        float f4 = (float) (col2 >> 24 & 255) / 255.0f;
        float f5 = (float) (col2 >> 16 & 255) / 255.0f;
        float f6 = (float) (col2 >> 8 & 255) / 255.0f;
        float f7 = (float) (col2 & 255) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glPushMatrix();
        GL11.glBegin(7);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glVertex2d(left, top);
        GL11.glVertex2d(left, bottom);
        GL11.glColor4f(f5, f6, f7, f4);
        GL11.glVertex2d(right, bottom);
        GL11.glVertex2d(right, top);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
    }

    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void color(int color) {
        float f = (color >> 24 & 0xFF) / 255.0F;
        float f1 = (color >> 16 & 0xFF) / 255.0F;
        float f2 = (color >> 8 & 0xFF) / 255.0F;
        float f3 = (color & 0xFF) / 255.0F;
        GL11.glColor4f(f1, f2, f3, f);
    }

    public static void color(Color color) {
        color(color.getRGB());
    }

    public static void drawGradientRect(float x, float y, float x1, float y1, int topColor, int bottomColor) {
        enableGL2D();
        GL11.glShadeModel(7425);
        GL11.glBegin(7);
        color(topColor);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        color(bottomColor);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
        GL11.glShadeModel(7424);
        disableGL2D();
    }

    public static void drawCircle(double x, double y, double radius, int c) {
        float alpha = (float) (c >> 24 & 255) / 255.0f;
        float red = (float) (c >> 16 & 255) / 255.0f;
        float green = (float) (c >> 8 & 255) / 255.0f;
        float blue = (float) (c & 255) / 255.0f;
        boolean blend = GL11.glIsEnabled(3042);
        boolean line = GL11.glIsEnabled(2848);
        boolean texture = GL11.glIsEnabled(3553);
        if (!blend) {
            GL11.glEnable(3042);
        }
        if (!line) {
            GL11.glEnable(2848);
        }
        if (texture) {
            GL11.glDisable(3553);
        }
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(9);
        int i = 0;
        while (i <= 360) {
            GL11.glVertex2d(
                    x + Math.sin((double) i * 3.141526 / 180.0) * radius,
                    y + Math.cos((double) i * 3.141526 / 180.0) * radius);
            ++i;
        }

        GL11.glEnd();
        if (texture) {
            GL11.glEnable(3553);
        }
        if (!line) {
            GL11.glDisable(2848);
        }
        if (!blend) {
            GL11.glDisable(3042);
        }
    }

    public void draw(float x, float y, float width, float height, float mouseX, float mouseY) {
        if (hue == -1) {
            float[] vals = Color.RGBtoHSB(color >> 16 & 255, color >> 8 & 255, color & 255, null);
            hue = vals[0];
            saturation = vals[1];
            brightness = vals[2];
            alpha = (color >> 24 & 255) / 255f;
        }
        // Saturation
        drawGradientSideways(x, y, x + width, y + height, -1, Color.HSBtoRGB(getHue(), 1, 1));
        // Brightness
        drawGradientRect(x, y, x + width, y + height, new Color(0, 0, 0, 255).getRGB(), new Color(255, 255, 255, 0).getRGB());

        // hue
        int i = 0;
        while (i < 5) {
            drawGradientRect(x + 42, y + height / 5 * (4 - i), x + 48, y + height / 5 * (4 - i + 1), Color.HSBtoRGB(1 - 0.2f * (i), 1, 1), Color.HSBtoRGB(1 - 0.2f * (i + 1), 1, 1));
            i++;
        }

        // Alpha
        //drawRect(x + 50, y, x + 56, y + height, new Color(82, 82, 82, 255).getRGB());
        drawGradientRect(x + 50, y, x + 56, y + height, reAlpha(getColor(), 1), reAlpha(getColor(), 0));

        double bY = height - getBrightness() * height;
        double sX = getSaturation() * width;
        drawCircle(x + sX, y + bY, 2.4, new Color(0, 0, 0, 255).getRGB());
        drawCircle(x + sX, y + bY, 2, -1);


        double hueY = getHue() * height;
        //drawRect(x + 42, y + hueY, x + 48, y + hueY + 1, -1);

        double alphaY = getAlpha() * height;
        //drawRect(x + 50, y + alphaY, x + 56, y + alphaY + 1, -1);

        if (Mouse.isButtonDown(0)) {
            if (isHovered(x + 42, y, x + 48, y + height, ((int) mouseX), ((int) mouseY))) {
                hue = (mouseY - y) / height;
            }
            if (isHovered(x, y, x + width, y + height, ((int) mouseX), ((int) mouseY))) {
                brightness = 1 - (mouseY - y) / height;
            }
            if (isHovered(x, y, x + width, y + height, ((int) mouseX), ((int) mouseY))) {
                saturation = (mouseX - x) / width;
            }
            if (isHovered(x + 50, y, x + 56, y + height, ((int) mouseX), ((int) mouseY))) {
                alpha = ((mouseY - y) / height);
            }
        }
        this.color = intToColor(reAlpha(Color.HSBtoRGB(hue, saturation, brightness), alpha)).getRGB();
        this.value = intToColor(reAlpha(Color.HSBtoRGB(hue, saturation, brightness), alpha));
        color(new Color(255, 255, 255, 255));
    }

    public static boolean isHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

    public static Color intToColor(int color) {
        Color c1 = new Color(color);
        return new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), color >> 24 & 255);
    }

    public static int reAlpha(int color, float alpha) {
        Color c = new Color(color);
        float r = 0.003921569f * (float) c.getRed();
        float g = 0.003921569f * (float) c.getGreen();
        float b = 0.003921569f * (float) c.getBlue();
        return new Color(r, g, b, alpha).getRGB();
    }
}
