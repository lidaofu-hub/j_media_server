package com.ldf.media.module.stack;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.ldf.media.api.model.param.VideoStackParam;
import com.ldf.media.api.model.param.VideoStackWindowParam;
import com.ldf.media.pool.MediaServerThreadPool;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVIOContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.ffmpeg.global.swscale;
import org.bytedeco.ffmpeg.swscale.SwsContext;
import org.bytedeco.javacpp.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.bytedeco.ffmpeg.global.avutil.AV_LOG_ERROR;

public class VideoStack {

    private static int FPS = 25;

    private Boolean isStop = false;

    private Long pts = 0L;

    private Long lastPushTime=0L;

    private Long pushWaitTime=0L;

    private VideoStackParam param;

    private String pushUrl;

    private AVFormatContext oFmtCtx = null;

    private AVCodecContext enCodecCtx = null;

    private AVStream avStream = null;

    private AVCodec avCodec = null;

    private AVFrame avFrame = null;

    private AVPacket avPacket = null;

    private SwsContext avSwsCtx = null;

    private PointerPointer<Pointer> dataPointer = null;

    private Pointer srcPointer = null;

    private SwsContext imgSwsCtx = null;

    private Integer srcDataSize = 0;

    private IntPointer linePointer = null;

    private AtomicBoolean readDataAtomic=null;

    private Set<Integer> EMPTY_WINDOW_SET = new LinkedHashSet<>();

    private List<VideoStackWindow> windowList = new ArrayList<>();

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public VideoStack(VideoStackParam param, String pushUrl) {
        this.param = param;
        this.pushUrl = pushUrl;
    }

    /**
     * 初始化
     */
    public void init() {
        //初始化全局参数
        initGlobalData();
        //填充颜色
        initFillColor();
        //填充图片
        initFillImage();
        //画分割线
        initGridLine();
        //初始化窗口
        initStackWindow();
        MediaServerThreadPool.execute(() -> {
            initEncodeAndPush();
        });
    }


    /**
     * 初始化全局数据
     */
    private void initGlobalData() {
        srcDataSize = avutil.av_image_get_buffer_size(avutil.AV_PIX_FMT_BGR24, param.getWidth(), param.getHeight(), 1);
        srcPointer = avutil.av_mallocz(srcDataSize);
        dataPointer = new PointerPointer<>(4);
        linePointer = new IntPointer(4);
        srcDataSize = avutil.av_image_fill_arrays(dataPointer, linePointer, new BytePointer(srcPointer), avutil.AV_PIX_FMT_BGR24, param.getWidth(), param.getHeight(), 1);
        lastPushTime=System.currentTimeMillis();
        pushWaitTime= (long) (1000/FPS);
        readDataAtomic=new AtomicBoolean(false);
    }


    /**
     * 填充颜色
     */
    private void initFillColor() {
        if (StrUtil.isBlank(param.getFillImgUrl())) {
            int[] bgr = convertRGBHex(param.getFillColor());
            BytePointer bytePointer = new BytePointer(srcPointer);
            // 4. 填充每一行
            for (int y = 0; y < param.getHeight(); y++) {
                // 计算当前行的起始位置
                long rowOffset = y * (long) linePointer.get(0);
                for (int x = 0; x < param.getWidth(); x++) {
                    long pixelOffset = rowOffset + x * 3L;
                    bytePointer.put(pixelOffset, (byte) bgr[0]);
                    bytePointer.put(pixelOffset + 1, (byte) bgr[1]);
                    bytePointer.put(pixelOffset + 2, (byte) bgr[2]);
                }
            }
        }

    }


    /**
     * 填充图片
     */
    private void initFillImage() {
        Integer windowSize = param.getCol() * param.getRow();
        for (int i = 1; i <= windowSize; i++) {
            EMPTY_WINDOW_SET.add(i);
        }
        List<VideoStackWindowParam> urlList = param.getWindowList();
        if (CollectionUtil.isNotEmpty(urlList)) {
            for (VideoStackWindowParam videoStackUrlParam : urlList) {
                for (Integer i : videoStackUrlParam.getSpan()) {
                    EMPTY_WINDOW_SET.remove(i);
                }
            }
        }
        int w = param.getWidth() / param.getRow();
        int h = param.getHeight() / param.getCol();
        if (StrUtil.isNotBlank(param.getFillImgUrl())) {
            ImageData imageData = getImageBGRData(param.getFillImgUrl());
            if (imageData != null) {
                imgSwsCtx = swscale.sws_getContext(imageData.width, imageData.height, avutil.AV_PIX_FMT_BGR24, w, h, avutil.AV_PIX_FMT_BGR24, swscale.SWS_BICUBIC, null, null, (DoublePointer) null);
                if (imgSwsCtx != null) {
                    PointerPointer<Pointer> srcDataPointerPointer = new PointerPointer<>(4);
                    IntPointer srcLineSize = new IntPointer(4);
                    BytePointer rgbImgPointer = new BytePointer(imageData.bgrData);
                    avutil.av_image_fill_arrays(srcDataPointerPointer, srcLineSize, rgbImgPointer, avutil.AV_PIX_FMT_BGR24, imageData.width, imageData.height, 1);
                    int destDataSize = avutil.av_image_get_buffer_size(avutil.AV_PIX_FMT_BGR24, w, h, 1);
                    Pointer destPointer = avutil.av_mallocz(destDataSize);
                    PointerPointer<Pointer> destDataPointerPointer = new PointerPointer<>(4);
                    IntPointer destLineSize = new IntPointer(4);
                    avutil.av_image_fill_arrays(destDataPointerPointer, destLineSize, new BytePointer(destPointer), avutil.AV_PIX_FMT_BGR24, w, h, 1);
                    swscale.sws_scale(imgSwsCtx, srcDataPointerPointer, srcLineSize, 0, imageData.height, destDataPointerPointer, destLineSize);
                    int diffW = linePointer.get(0);
                    int destW = destLineSize.get(0);
                    for (Integer index : EMPTY_WINDOW_SET) {
                        int ySrcPos = ((index - 1) / param.getRow()) * diffW * h + ((index - 1) % param.getRow()) * destW;
                        int yImgPos = 0;
                        for (int i = 0; i < h; i++) {
                            Pointer.memcpy(srcPointer.getPointer(ySrcPos), destPointer.getPointer(yImgPos), w * 3L);
                            yImgPos = yImgPos + destW;
                            ySrcPos = ySrcPos + diffW;
                        }
                    }
                    swscale.sws_freeContext(imgSwsCtx);
                    avutil.av_free(destPointer);
                    imgSwsCtx = null;
                }
            }

        }
    }

    /**
     * 添加分割线
     */
    private void initGridLine() {
        if (param.getGridLineEnable()) {
            int[] rgb = convertRGBHex(param.getGridLineColor());
            addGridLines(dataPointer, linePointer, param.getWidth(), param.getHeight(), param.getRow(), param.getCol(), param.getGridLineWidth(), rgb[0], rgb[1], rgb[2]);
        }
    }

    /**
     * 初始化窗口
     */
    private void initStackWindow() {
        List<VideoStackWindowParam> urlList = param.getWindowList();
        if (CollectionUtil.isNotEmpty(urlList)) {
            for (VideoStackWindowParam videoStackWindowParam : urlList) {
                int[] whp = calculateBlockDimensions(videoStackWindowParam.getSpan(), param.getWidth(), param.getHeight(), param.getRow(), param.getCol());
                VideoStackWindow videoStackWindow = null;
                if (StrUtil.isNotBlank(videoStackWindowParam.getUrl())) {
                    videoStackWindow = new VideoStackWindow(videoStackWindowParam.getUrl(), true, whp[0], whp[1], whp[2] * 3, dataPointer, linePointer,readDataAtomic,param.getDeCodecName());
                } else {
                    videoStackWindow = new VideoStackWindow(StrUtil.isNotBlank(videoStackWindowParam.getImgUrl()) ? videoStackWindowParam.getUrl() : param.getFillImgUrl(), false, whp[0], whp[1], whp[2] * 3, dataPointer, linePointer,readDataAtomic,param.getDeCodecName());
                }
                videoStackWindow.init();
                windowList.add(videoStackWindow);
            }
        }
    }

    /**
     * 初始化编码和推流
     */
    private void initEncodeAndPush() {
        int ret = 0;
        avCodec = avcodec.avcodec_find_encoder(avcodec.AV_CODEC_ID_H264);
        enCodecCtx = avcodec.avcodec_alloc_context3(avCodec);
        enCodecCtx.width(param.getWidth());
        enCodecCtx.height(param.getHeight());
        enCodecCtx.profile(avcodec.AV_PROFILE_H264_BASELINE);
        enCodecCtx.time_base(avutil.av_make_q(1, FPS));
        enCodecCtx.framerate(avutil.av_make_q(FPS, 1));
        enCodecCtx.pix_fmt(avutil.AV_PIX_FMT_YUV420P);
        enCodecCtx.max_b_frames(0);
        enCodecCtx.has_b_frames(0);
        enCodecCtx.codec_id(avcodec.AV_CODEC_ID_H264);
        enCodecCtx.codec_type(avutil.AVMEDIA_TYPE_VIDEO);
        AVDictionary codecOptions = new AVDictionary(null);
        avutil.av_dict_set(codecOptions, "preset", "ultrafast", 0);
        ret = avcodec.avcodec_open2(enCodecCtx, avCodec, codecOptions);
        avutil.av_dict_free(codecOptions);
        if (ret < 0) {
            avutil.av_log(enCodecCtx, avutil.AV_LOG_ERROR, "avcodec_open2 error");
            free();
            return;
        }
        oFmtCtx = new AVFormatContext(null);
        ret = avformat.avformat_alloc_output_context2(oFmtCtx, null, "flv", pushUrl);
        if (ret < 0) {
            avutil.av_log(enCodecCtx, avutil.AV_LOG_ERROR, "avformat_alloc_output_context2 error");
            free();
            return;
        }
        avStream = avformat.avformat_new_stream(oFmtCtx, null);
        ret = avcodec.avcodec_parameters_from_context(avStream.codecpar(), enCodecCtx);
        if (ret < 0) {
            avutil.av_log(enCodecCtx, avutil.AV_LOG_ERROR, "avcodec_parameters_from_context error");
            free();
            return;
        }
        avStream.codecpar().codec_tag(0);
        avPacket = avcodec.av_packet_alloc();
        avFrame = avutil.av_frame_alloc();
        avFrame.format(enCodecCtx.pix_fmt());
        avFrame.width(enCodecCtx.width());
        avFrame.height(enCodecCtx.height());
        avutil.av_frame_get_buffer(avFrame, 1);
        avSwsCtx = swscale.sws_getContext(param.getWidth(), param.getHeight(), avutil.AV_PIX_FMT_BGR24, avFrame.width(), avFrame.height(), avutil.AV_PIX_FMT_YUV420P, swscale.SWS_BICUBIC, null, null, (DoublePointer) null);
        if (avSwsCtx == null) {
            avutil.av_log(enCodecCtx, avutil.AV_LOG_ERROR, "sws_getContext error");
            free();
            return;
        }
        AVIOContext pb = new AVIOContext((Pointer) null);
        ret = avformat.avio_open(pb, pushUrl, avformat.AVIO_FLAG_WRITE);
        if (ret < 0) {
            avutil.av_log(oFmtCtx, AV_LOG_ERROR, "avio_open error \n");
            free();
            return;
        }
        oFmtCtx.pb(pb);
        ret = avformat.avformat_write_header(oFmtCtx, (PointerPointer) null);
        if (ret < 0) {
            avutil.av_log(oFmtCtx, AV_LOG_ERROR, "avformat_write_header error \n");
            free();
            return;
        }
        while (!isStop) {
            if (avutil.av_frame_is_writable(avFrame) != 1) {
                avutil.av_frame_make_writable(avFrame);
            }
            readDataAtomic.compareAndSet(false,true);
            swscale.sws_scale(avSwsCtx, dataPointer, linePointer, 0, param.getHeight(), avFrame.data(), avFrame.linesize());
            readDataAtomic.compareAndSet(true,false);
            avFrame.pts(pts);
            pts++;
            ret = avcodec.avcodec_send_frame(enCodecCtx, avFrame);
            if (ret < 0) {
                avutil.av_log(enCodecCtx, avutil.AV_LOG_ERROR, "avcodec_send_frame error");
                avformat.av_write_trailer(oFmtCtx);
                free();
                return;
            }
            while (true) {
                ret = avcodec.avcodec_receive_packet(enCodecCtx, avPacket);
                if (ret == avutil.AVERROR_EAGAIN() || ret == avutil.AVERROR_EOF()) {
                    break;
                }
                if (ret < 0) {
                    avutil.av_log(enCodecCtx, avutil.AV_LOG_ERROR, "avcodec_receive_packet error");
                    avformat.av_write_trailer(oFmtCtx);
                    free();
                    return;
                }
                avPacket.pts(avutil.av_rescale_q(avFrame.pts(), enCodecCtx.time_base(), avStream.time_base()));
                avPacket.dts(avPacket.pts());
                avPacket.duration(pushWaitTime);
                long elapsed = System.currentTimeMillis() - lastPushTime;
                long remaining = pushWaitTime - elapsed;
                if (remaining > 0) {
                    while (System.currentTimeMillis() - lastPushTime < pushWaitTime) {
                        Thread.yield();
                    }
                    avformat.av_interleaved_write_frame(oFmtCtx, avPacket);
                } else {
                    avformat.av_interleaved_write_frame(oFmtCtx, avPacket);
                }
                lastPushTime=System.currentTimeMillis();
                avcodec.av_packet_unref(avPacket);
            }

        }
        avformat.av_write_trailer(oFmtCtx);
        free();
    }

    /**
     * 计算矩形大小
     *
     * @param blockNumbers
     * @param width
     * @param height
     * @param row
     * @param col
     * @return
     */
    public static int[] calculateBlockDimensions(List<Integer> blockNumbers, int width, int height, int row, int col) {
        int blockWidth = width / col;
        int blockHeight = height / row;

        int minRow = row;
        int maxRow = 0;
        int minCol = col;
        int maxCol = 0;

        for (int blockNumber : blockNumbers) {
            int index = blockNumber - 1;
            int currentRow = index / col;
            int currentCol = index % col;

            minRow = Math.min(minRow, currentRow);
            maxRow = Math.max(maxRow, currentRow);
            minCol = Math.min(minCol, currentCol);
            maxCol = Math.max(maxCol, currentCol);
        }

        int combinedWidth = (maxCol - minCol + 1) * blockWidth;
        int combinedHeight = (maxRow - minRow + 1) * blockHeight;
        int startIndex = (minRow * blockHeight) * width + (minCol * blockWidth);

        return new int[]{combinedWidth, combinedHeight, startIndex};
    }


    /**
     * 重新配置
     *
     * @param newParam
     */
    public void reset(VideoStackParam newParam) {
        for (VideoStackWindow videoStackWindow : windowList) {
            videoStackWindow.stop();
        }
        windowList.clear();
        param=newParam;
        //填充颜色
        initFillColor();
        //填充图片
        initFillImage();
        //画分割线
        initGridLine();
        //初始化窗口
        initStackWindow();
    }


    /**
     * 停止
     */
    public void stop() {
        for (VideoStackWindow videoStackWindow : windowList) {
            videoStackWindow.stop();
        }
        windowList.clear();
        this.isStop = true;
    }

    /**
     * 释放资源
     */
    private void free() {
        if (oFmtCtx != null) {
            avformat.avformat_free_context(oFmtCtx);
            oFmtCtx = null;
        }
        if (enCodecCtx != null) {
            avcodec.avcodec_free_context(enCodecCtx);
            enCodecCtx = null;
        }
        if (avFrame != null) {
            avutil.av_frame_free(avFrame);
            avFrame = null;
        }
        if (avPacket != null) {
            avcodec.av_packet_free(avPacket);
            avPacket = null;
        }
        if (srcPointer != null) {
            avutil.av_free(srcPointer);
            avPacket = null;
        }
    }

    /**
     * 读取图片bgr
     *
     * @param url
     * @return
     */
    public static ImageData getImageBGRData(String url) {
        // 1. 读取图片文件
        BufferedImage image = null;
        try {
            image = ImageIO.read(new URL(url));
            // 检查是否是 RGB/BGR 格式
            if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
                byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                return new ImageData(image.getWidth(), image.getHeight(), pixels);
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (image != null) {
                image.flush();
            }
        }
        return null;
    }


    /**
     * 将 RGB 十六进制字符串转换
     *
     * @param rgbHex RGB 十六进制字符串（如 "FF0000" 表示红色）
     * @return int[3] 数组，依次为 BGR
     */
    public static int[] convertRGBHex(String rgbHex) {
        // 1. 解析 RGB 十六进制字符串
        int r = Integer.parseInt(rgbHex.substring(0, 2), 16);
        int g = Integer.parseInt(rgbHex.substring(2, 4), 16);
        int b = Integer.parseInt(rgbHex.substring(4, 6), 16);

        return new int[]{b, g, r};
    }

    static class ImageData {
        public ImageData(int width, int height, byte[] bgrData) {
            this.width = width;
            this.height = height;
            this.bgrData = bgrData;
        }

        public int width;
        public int height;
        public byte[] bgrData;
    }


    /**
     * 为BGR24格式的Frame添加网格分割线
     *
     * @param frameData 要处理的PointerPointer
     * @param row       行分割数
     * @param col       列分割数
     * @param lineWidth 分割线宽度(像素)
     * @param b         蓝色分量(0-255)
     * @param g         绿色分量(0-255)
     * @param r         红色分量(0-255)
     */
    public static void addGridLines(PointerPointer<Pointer> frameData, IntPointer linePointer, int width, int height, int row, int col, int lineWidth,
                                    int b, int g, int r) {

        // 计算每个块的宽度和高度
        int blockWidth = width / col;
        int blockHeight = height / row;
        int stride = linePointer.get(0);
        // 获取帧数据指针
        BytePointer data = new BytePointer(frameData.get(0));
        // 绘制水平分割线
        for (int currentRow = 1; currentRow < row; currentRow++) {
            int yPos = currentRow * blockHeight;
            for (int w = 0; w < lineWidth; w++) {
                int currentY = Math.min(yPos + w, height - 1);
                for (int x = 0; x < width; x++) {
                    int pos = currentY * stride + x * 3;
                    data.put(pos, (byte) b);     // B
                    data.put(pos + 1, (byte) g); // G
                    data.put(pos + 2, (byte) r); // R
                }
            }
        }

        // 绘制垂直分割线
        for (int currentCol = 1; currentCol < col; currentCol++) {
            int xPos = currentCol * blockWidth;

            for (int w = 0; w < lineWidth; w++) {
                int currentX = Math.min(xPos + w, width - 1);
                for (int y = 0; y < height; y++) {
                    int pos = y * stride + currentX * 3;
                    data.put(pos, (byte) b);     // B
                    data.put(pos + 1, (byte) g); // G
                    data.put(pos + 2, (byte) r); // R
                }
            }
        }
    }
}
