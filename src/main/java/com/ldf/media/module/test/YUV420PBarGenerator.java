package com.ldf.media.module.test;

import java.util.Random;

public class YUV420PBarGenerator {
    private static final int ALIGNMENT = 16; // 16字节对齐，可根据需要调整
    private static int frameCounter = 0; // 帧计数器用于实现滚动效果

    public static YUV420PFrame generateBarFrame(int width, int height) {
        // 计算对齐后的行跨度
        int yStride = align(width, ALIGNMENT);
        int uvStride = align(width / 2, ALIGNMENT);

        // 计算各平面大小
        int ySize = yStride * height;
        int uvSize = uvStride * height / 2;

        // 创建数据数组
        byte[] yData = new byte[ySize];
        byte[] uData = new byte[uvSize];
        byte[] vData = new byte[uvSize];
        int[] linesize = new int[]{yStride, uvStride, uvStride};

        // 生成条状图案
        generateBars(yData, uData, vData, linesize, width, height);

        // 增加帧计数器以实现滚动效果
        frameCounter = (frameCounter + 1) % width;

        return new YUV420PFrame(yData, uData, vData, linesize, width, height);
    }

    // 生成条状图案
    private static void generateBars(byte[] yData, byte[] uData, byte[] vData,
                                     int[] linesize, int width, int height) {
        int yStride = linesize[0];
        int uvStride = linesize[1];

        // 定义几种不同颜色的条状图案
        BarPattern[] patterns = {
                new BarPattern(100, 50, 50),   // 灰色
                new BarPattern(180, 100, 100), // 亮黄色
                new BarPattern(80, 90, 200),   // 蓝色
                new BarPattern(150, 40, 100),  // 紫色
                new BarPattern(60, 200, 60)    // 绿色
        };

        Random random = new Random();

        // 填充Y平面
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 计算滚动位置
                int scrollX = (x + frameCounter) % width;

                // 根据位置选择图案
                int patternIndex = (scrollX / (width / patterns.length)) % patterns.length;
                BarPattern pattern = patterns[patternIndex];

                // 设置Y值
                yData[y * yStride + x] = (byte) pattern.y;
            }
        }

        // 填充U和V平面
        for (int y = 0; y < height / 2; y++) {
            for (int x = 0; x < width / 2; x++) {
                // 计算滚动位置
                int scrollX = (x * 2 + frameCounter) % width;

                // 根据位置选择图案
                int patternIndex = (scrollX / (width / patterns.length)) % patterns.length;
                BarPattern pattern = patterns[patternIndex];

                // 设置U和V值
                uData[y * uvStride + x] = (byte) pattern.u;
                vData[y * uvStride + x] = (byte) pattern.v;
            }
        }
    }

    // 字节对齐计算
    private static int align(int value, int alignment) {
        return (value + alignment - 1) & ~(alignment - 1);
    }

    // 条状图案定义
    private static class BarPattern {
        int y;
        int u;
        int v;

        BarPattern(int y, int u, int v) {
            this.y = y;
            this.u = u;
            this.v = v;
        }
    }

    // 帧数据容器类
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

        // 打印帧信息（用于调试）
        public void printInfo() {
            System.out.println("Frame size: " + width + "x" + height);
            System.out.println("Y plane: " + yData.length + " bytes, stride: " + linesize[0]);
            System.out.println("U plane: " + uData.length + " bytes, stride: " + linesize[1]);
            System.out.println("V plane: " + vData.length + " bytes, stride: " + linesize[2]);
        }
    }
}
