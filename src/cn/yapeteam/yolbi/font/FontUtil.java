package cn.yapeteam.yolbi.font;

import cn.yapeteam.loader.logger.Logger;

import java.awt.*;
import java.io.InputStream;

public class FontUtil {
    public static Font getFontFromTTF(InputStream is, float fontSize, int fontType) {
        Font output = null;
        try {
            output = Font.createFont(fontType, is);
            output = output.deriveFont(fontSize);
        } catch (Exception e) {
            Logger.exception(e);
        }
        return output;
    }
}
