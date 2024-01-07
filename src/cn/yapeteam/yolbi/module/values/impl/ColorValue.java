package cn.yapeteam.yolbi.module.values.impl;

import cn.yapeteam.yolbi.module.values.Value;
import cn.yapeteam.yolbi.module.values.Visibility;
import cn.yapeteam.yolbi.utils.render.RenderUtil;
import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

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

    public void draw(float x, float y, float width, float height, float mouseX, float mouseY) {
        if (hue == -1) {
            float[] vals = Color.RGBtoHSB(color >> 16 & 255, color >> 8 & 255, color & 255, null);
            hue = vals[0];
            saturation = vals[1];
            brightness = vals[2];
            alpha = (color >> 24 & 255) / 255f;
        }
        // Saturation
        RenderUtil.drawGradientSideways(x, y, x + width, y + height, -1, Color.HSBtoRGB(getHue(), 1, 1));
        // Brightness
        RenderUtil.drawGradientRect(x, y, x + width, y + height, new Color(0, 0, 0, 255).getRGB(), new Color(255, 255, 255, 0).getRGB());

        // hue
        int i = 0;
        while (i < 5) {
            RenderUtil.drawGradientRect(x + 42, y + height / 5 * (4 - i), x + 48, y + height / 5 * (4 - i + 1), Color.HSBtoRGB(1 - 0.2f * (i), 1, 1), Color.HSBtoRGB(1 - 0.2f * (i + 1), 1, 1));
            i++;
        }

        // Alpha
        RenderUtil.drawRect(x + 50, y, x + 56, y + height, new Color(82, 82, 82, 255).getRGB());
        RenderUtil.drawGradientRect(x + 50, y, x + 56, y + height, reAlpha(getColor(), 1), reAlpha(getColor(), 0));

        double bY = height - getBrightness() * height;
        double sX = getSaturation() * width;
        RenderUtil.drawCircle(x + sX, y + bY, 2.4, new Color(0, 0, 0, 255).getRGB());
        RenderUtil.drawCircle(x + sX, y + bY, 2, -1);


        double hueY = getHue() * height;
        RenderUtil.drawRect(x + 42, y + hueY, x + 48, y + hueY + 1, -1);

        double alphaY = getAlpha() * height;
        RenderUtil.drawRect(x + 50, y + alphaY, x + 56, y + alphaY + 1, -1);

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
        GlStateManager.color(1, 1, 1, 1);
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
