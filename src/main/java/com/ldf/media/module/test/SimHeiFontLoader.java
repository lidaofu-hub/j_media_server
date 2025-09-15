package com.ldf.media.module.test;

import java.awt.*;
import java.io.InputStream;

public class SimHeiFontLoader {

    /**
     * 从src/main/resources加载SimHei.ttf字体
     *
     * @param fontSize 字体大小
     * @return 加载成功的Font对象，失败返回默认字体
     */
    public static Font loadSimHeiFont(float fontSize) {
        // 字体文件在resources中的路径
        String fontPath = "/SimHei.ttf"; // 若放在子目录如fonts下，则改为"/fonts/SimHei.ttf"

        try (InputStream is = SimHeiFontLoader.class.getResourceAsStream(fontPath)) {
            if (is == null) {
                throw new RuntimeException("未找到字体文件: " + fontPath);
            }

            // 从输入流创建基础字体
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);

            // 注册字体到系统（可选，注册后可通过字体名全局使用）
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(baseFont);

            // 派生指定大小的字体并返回
            return baseFont.deriveFont(fontSize);

        } catch (Exception e) {
            System.err.println("加载SimHei字体失败，将使用默认字体: " + e.getMessage());
            // 返回默认字体作为降级方案
            return new Font("SansSerif", Font.PLAIN, (int) fontSize);
        }
    }
}
