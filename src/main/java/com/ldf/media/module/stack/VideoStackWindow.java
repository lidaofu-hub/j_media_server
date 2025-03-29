package com.ldf.media.module.stack;

import cn.hutool.core.util.StrUtil;
import com.ldf.media.pool.MediaServerThreadPool;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.ffmpeg.global.swscale;
import org.bytedeco.ffmpeg.swscale.SwsContext;
import org.bytedeco.javacpp.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.ldf.media.module.stack.VideoStack.getImageBGRData;
import static org.bytedeco.ffmpeg.global.avutil.AVERROR_EOF;
import static org.bytedeco.ffmpeg.global.avutil.AV_LOG_ERROR;
import static org.bytedeco.ffmpeg.presets.avutil.AVERROR_EAGAIN;

public class VideoStackWindow {
    private String url;

    private Boolean isVideo;

    private Integer width;

    private Integer height;

    private Integer yPos;

    private Integer vIndex;

    private PointerPointer<Pointer> dataPointer;

    private IntPointer linePointer;

    private AVFormatContext iFmtCtx = null;

    private AVStream avStream = null;

    private AVFrame avFrame = null;

    private AVFrame rgbFrame = null;

    private AVPacket avPacket = null;

    private AVCodecContext deCodecCtx = null;

    private SwsContext avSwsCtx = null;

    private SwsContext imgSwsCtx = null;

    private Boolean isStop = false;

    private AtomicBoolean readDataAtomic;

    private String deCodecName;


    public VideoStackWindow(String url, Boolean isVideo, Integer width, Integer height, Integer yPos, PointerPointer<Pointer> dataPointer, IntPointer linePointer, AtomicBoolean readDataAtomic, String deCodecName) {
        this.url = url;
        this.isVideo = isVideo;
        this.width = width;
        this.height = height;
        this.yPos = yPos;
        this.dataPointer = dataPointer;
        this.linePointer = linePointer;
        this.readDataAtomic = readDataAtomic;
        this.deCodecName = deCodecName;
    }


    public void init() {
        if (StrUtil.isNotBlank(url)) {
            if (isVideo) {
                MediaServerThreadPool.execute(() -> {
                    initVideo();
                });
            } else {
                initImg();
            }
        }
    }


    /**
     * 填充视频
     */
    private void initVideo() {
        int ret = 0;
        iFmtCtx = new AVFormatContext(null);
        boolean isRtsp = url.startsWith("rtsp");
        if (isRtsp) {
            AVDictionary rtspOptions = new AVDictionary(null);
            avutil.av_dict_set(rtspOptions, "rtsp_transport", "tcp", 0);
            ret = avformat.avformat_open_input(iFmtCtx, url, null, rtspOptions);
            avutil.av_dict_free(rtspOptions);
        } else {
            ret = avformat.avformat_open_input(iFmtCtx, url, null, null);
        }
        if (ret < 0) {
            avutil.av_log(iFmtCtx, AV_LOG_ERROR, "avformat_open_input error \n");
            free();
            return;
        }
        ret = avformat.avformat_find_stream_info(iFmtCtx, (PointerPointer) null);
        if (ret < 0) {
            avutil.av_log(iFmtCtx, AV_LOG_ERROR, "avformat_find_stream_info error \n");
            free();
            return;
        }
        vIndex = avformat.av_find_best_stream(iFmtCtx, avutil.AVMEDIA_TYPE_VIDEO, -1, -1, (PointerPointer) null, 0);
        if (vIndex < 0) {
            avutil.av_log(iFmtCtx, AV_LOG_ERROR, "av_find_best_stream error \n");
            free();
            return;
        }
        avStream = iFmtCtx.streams(vIndex);
        AVCodec deCodec = null;
        if (StrUtil.isNotBlank(deCodecName)) {
            deCodec = avcodec.avcodec_find_decoder_by_name(deCodecName);
            if (deCodec != null && deCodec.id() != avStream.codecpar().codec_id()) {
                deCodec = null;
            }
        }
        if (deCodec == null) {
            deCodec = avcodec.avcodec_find_decoder(avStream.codecpar().codec_id());
        }
        deCodecCtx = avcodec.avcodec_alloc_context3(deCodec);
        if (deCodecCtx == null) {
            avutil.av_log(iFmtCtx, AV_LOG_ERROR, "avcodec_alloc_context3 error \n");
            free();
            return;
        }
        ret = avcodec.avcodec_parameters_to_context(deCodecCtx, avStream.codecpar());
        if (vIndex < 0) {
            avutil.av_log(deCodecCtx, AV_LOG_ERROR, "avcodec_parameters_to_context error \n");
            free();
            return;
        }
        ret = avcodec.avcodec_open2(deCodecCtx, deCodec, (PointerPointer) null);
        if (ret < 0) {
            avutil.av_log(deCodecCtx, AV_LOG_ERROR, "avcodec_open2 error \n");
            free();
            return;
        }
        avPacket = avcodec.av_packet_alloc();
        avFrame = avutil.av_frame_alloc();
        avFrame.width(deCodecCtx.width());
        avFrame.height(deCodecCtx.height());
        avFrame.format(deCodecCtx.pix_fmt());
        avutil.av_frame_get_buffer(avFrame, 1);
        rgbFrame = avutil.av_frame_alloc();
        rgbFrame.width(width);
        rgbFrame.height(height);
        rgbFrame.format(avutil.AV_PIX_FMT_BGR24);
        avutil.av_frame_get_buffer(rgbFrame, 1);
        avSwsCtx = swscale.sws_getContext(avFrame.width(), avFrame.height(), avFrame.format(), width, height, avutil.AV_PIX_FMT_BGR24, swscale.SWS_BICUBIC, null, null, (DoublePointer) null);
        if (avSwsCtx == null) {
            avutil.av_log(deCodecCtx, AV_LOG_ERROR, "sws_getContext error \n");
            free();
            return;
        }
        while (!isStop && (ret = avformat.av_read_frame(iFmtCtx, avPacket)) == 0) {
            if (avPacket.stream_index() == vIndex) {
                ret = avcodec.avcodec_send_packet(deCodecCtx, avPacket);
                if (ret < 0) {
                    avutil.av_log(deCodecCtx, AV_LOG_ERROR, "avcodec_send_packet error \n");
                    free();
                    return;
                }
                while (true) {
                    if (avutil.av_frame_is_writable(avFrame) != 1) {
                        avutil.av_frame_make_writable(avFrame);
                    }
                    ret = avcodec.avcodec_receive_frame(deCodecCtx, avFrame);
                    if (ret == AVERROR_EAGAIN() || ret == AVERROR_EOF()) {
                        break;
                    }
                    if (ret < 0) {
                        avutil.av_log(deCodecCtx, AV_LOG_ERROR, "avcodec_receive_frame error \n");
                        free();
                        return;
                    }
                    if (avutil.av_frame_is_writable(rgbFrame) != 1) {
                        avutil.av_frame_make_writable(rgbFrame);
                    }
                    swscale.sws_scale(avSwsCtx, avFrame.data(), avFrame.linesize(), 0, avFrame.height(), rgbFrame.data(), rgbFrame.linesize());
                    int yRgbPos = 0;
                    int destPos = yPos;
                    int diffW = linePointer.get(0);
                    int destW = rgbFrame.linesize().get(0);
                    while (!readDataAtomic.get()) {
                        Thread.yield();
                    }
                    for (int i = 0; i < rgbFrame.height(); i++) {
                        Pointer.memcpy(dataPointer.get(0).getPointer(destPos), rgbFrame.data().get(0).getPointer(yRgbPos), rgbFrame.width() * 3L);
                        yRgbPos = yRgbPos + destW;
                        destPos = destPos + diffW;
                    }
                }
            }
            avcodec.av_packet_unref(avPacket);
        }

        free();
    }

    /**
     * 填充图像
     */
    private void initImg() {
        VideoStack.ImageData imageData = getImageBGRData(url);
        if (imageData != null) {
            imgSwsCtx = swscale.sws_getContext(imageData.width, imageData.height, avutil.AV_PIX_FMT_BGR24, width, height, avutil.AV_PIX_FMT_BGR24, swscale.SWS_BICUBIC, null, null, (DoublePointer) null);
            if (imgSwsCtx != null) {
                PointerPointer<Pointer> srcDataPointerPointer = new PointerPointer<>(4);
                IntPointer srcLineSize = new IntPointer(4);
                BytePointer rgbImgPointer = new BytePointer(imageData.bgrData);
                avutil.av_image_fill_arrays(srcDataPointerPointer, srcLineSize, rgbImgPointer, avutil.AV_PIX_FMT_BGR24, imageData.width, imageData.height, 1);
                int destDataSize = avutil.av_image_get_buffer_size(avutil.AV_PIX_FMT_BGR24, width, height, 1);
                Pointer destPointer = avutil.av_mallocz(destDataSize);
                PointerPointer<Pointer> destDataPointerPointer = new PointerPointer<>(4);
                IntPointer destLineSize = new IntPointer(4);
                avutil.av_image_fill_arrays(destDataPointerPointer, destLineSize, new BytePointer(destPointer), avutil.AV_PIX_FMT_BGR24, width, height, 1);
                swscale.sws_scale(imgSwsCtx, srcDataPointerPointer, srcLineSize, 0, imageData.height, destDataPointerPointer, destLineSize);
                int yImgPos = 0;
                int diffW = linePointer.get(0);
                int destW = destLineSize.get(0);
                int destPos = yPos;
                for (int i = 0; i < height; i++) {
                    Pointer.memcpy(dataPointer.get(0).getPointer(destPos), destPointer.getPointer(yImgPos), width * 3);
                    yImgPos = yImgPos + destW;
                    destPos = destPos + diffW;
                }
                swscale.sws_freeContext(imgSwsCtx);
                avutil.av_free(destPointer);
                imgSwsCtx = null;
            }
        }
    }


    /**
     * 停止
     */
    public void stop() {
        isStop = true;
    }


    /**
     * 释放资源
     */
    private void free() {
        if (iFmtCtx != null) {
            avformat.avformat_close_input(iFmtCtx);
            iFmtCtx = null;
        }
        if (deCodecCtx != null) {
            avcodec.avcodec_free_context(deCodecCtx);
            deCodecCtx = null;
        }
        if (avFrame != null) {
            avutil.av_frame_free(avFrame);
            avFrame = null;
        }
        if (rgbFrame != null) {
            avutil.av_frame_free(rgbFrame);
            rgbFrame = null;
        }
        if (avPacket != null) {
            avcodec.av_packet_free(avPacket);
            avPacket = null;
        }
        if (avSwsCtx != null) {
            swscale.sws_freeContext(avSwsCtx);
            avSwsCtx = null;
        }
    }

}
