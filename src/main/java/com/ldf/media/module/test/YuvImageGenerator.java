package com.ldf.media.module.test;

import cn.hutool.core.util.StrUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class YuvImageGenerator {

    // 用于包装YUV420P数据的类
    public static class YUV420PFrame {
        private final byte[] yData;
        private final byte[] uData;
        private final byte[] vData;
        private final int[] linesize;
        private final int width;
        private final int height;

        public YUV420PFrame(byte[] yData, byte[] uData, byte[] vData, int[] linesize, int width, int height) {
            this.yData = yData;
            this.uData = uData;
            this.vData = vData;
            this.linesize = linesize;
            this.width = width;
            this.height = height;
        }

        // getter方法
        public byte[] getYData() {
            return yData;
        }

        public byte[] getUData() {
            return uData;
        }

        public byte[] getVData() {
            return vData;
        }

        public int[] getLinesize() {
            return linesize;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    /**
     * 生成包含指定文字的YUV420P格式图像
     *
     * @param width           图像宽度
     * @param height          图像高度
     * @param backgroundColor 背景色
     * @param text            需要显示的文字
     * @param textSize        文字大小
     * @param textColor       文字颜色（新增参数）
     * @return 包装了YUV420P数据的YUV420PFrame对象
     */
    public static YUV420PFrame generateYuv420pImage(int width, int height, Color backgroundColor,
                                                    String text, int textSize, Color textColor) {
        // 创建BufferedImage绘制文字
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // 设置背景色
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);

        // 设置文字属性并绘制文字
        if (text != null && !text.isEmpty()) {
            // 使用指定的文字颜色
            g2d.setColor(textColor);
            g2d.setFont(SimHeiFontLoader.loadSimHeiFont(textSize));

            // 计算文字位置使其居中
            FontMetrics metrics = g2d.getFontMetrics();
            int x = (width - metrics.stringWidth(text)) / 2;
            int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();
            g2d.drawString(text, x, y - 64);
            g2d.drawString(StrUtil.format("当前视频分辨率为：{}x{}", width, height), x, y);
            long now = System.currentTimeMillis();
            g2d.drawString(StrUtil.format("当前系统CPU占用： {}%", SystemMonitor.getProcessCpuUsage(now)), x, y + 64);
            g2d.drawString(StrUtil.format("当前系统RAM占用： {}%", SystemMonitor.getMemoryUsage(now)), x, y + 128);
        }
        g2d.dispose();

        // 转换为YUV420P格式并分离Y、U、V分量
        byte[][] yuvPlanes = rgbToYuv420pPlanes(bufferedImage, width, height);
        byte[] yData = yuvPlanes[0];
        byte[] uData = yuvPlanes[1];
        byte[] vData = yuvPlanes[2];

        // 计算行大小
        int[] lineSizes = new int[3];
        lineSizes[0] = width;           // Y分量行大小
        lineSizes[1] = width / 2;       // U分量行大小
        lineSizes[2] = width / 2;       // V分量行大小

        return new YUV420PFrame(yData, uData, vData, lineSizes, width, height);
    }

    /**
     * 将RGB图像转换为YUV420P格式并分离为Y、U、V三个平面
     */
    private static byte[][] rgbToYuv420pPlanes(BufferedImage image, int width, int height) {
        int ySize = width * height;
        int uvSize = (width / 2) * (height / 2);

        // 初始化Y、U、V三个分量数组
        byte[] yData = new byte[ySize];
        byte[] uData = new byte[uvSize];
        byte[] vData = new byte[uvSize];

        // 提取RGB数据
        int[] rgb = new int[ySize];
        image.getRGB(0, 0, width, height, rgb, 0, width);

        // 转换为YUV并填充到相应数组
        int yIndex = 0;
        int uvIndex = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = rgb[i * width + j];

                // 提取RGB分量
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                // 计算Y分量
                yData[yIndex++] = (byte) (0.299 * r + 0.587 * g + 0.114 * b + 0.5);

                // 每2x2像素计算一次UV分量
                if (i % 2 == 0 && j % 2 == 0) {
                    // 计算U和V分量
                    uData[uvIndex] = (byte) (-0.14713 * r - 0.28886 * g + 0.436 * b + 128 + 0.5);
                    vData[uvIndex] = (byte) (0.615 * r - 0.51499 * g - 0.10001 * b + 128 + 0.5);
                    uvIndex++;
                }
            }
        }

        return new byte[][]{yData, uData, vData};
    }

    // 使用示例
    public static void main(String[] args) {
        int width = 640;
        int height = 480;
        Color bgColor = new Color(255, 255, 255); // 白色背景
        String text = "Hello, YUV420P!";
        int textSize = 24;
        Color textColor = new Color(255, 0, 0); // 红色文字（新增参数示例）

        YUV420PFrame frame = generateYuv420pImage(width, height, bgColor, text, textSize, textColor);

        System.out.println("图像宽度: " + frame.getWidth());
        System.out.println("图像高度: " + frame.getHeight());
        System.out.println("Y分量长度: " + frame.getYData().length);
        System.out.println("U分量长度: " + frame.getUData().length);
        System.out.println("V分量长度: " + frame.getVData().length);
        System.out.println("行大小: " + Arrays.toString(frame.getLinesize()));
    }
}
