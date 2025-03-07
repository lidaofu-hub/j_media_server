package com.ldf.media.api.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RandomUtil;
import com.ldf.media.api.service.ISnapService;
import com.ldf.media.config.MediaServerConfig;
import lombok.extern.slf4j.Slf4j;
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
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.bytedeco.ffmpeg.global.avutil.*;
import static org.bytedeco.ffmpeg.presets.avutil.AVERROR_EAGAIN;

@Slf4j
@Service
public class SnapServiceImpl implements ISnapService {
    @Autowired
    private MediaServerConfig mediaServerConfig;

    /**
     * 获取截图
     *
     * @param url
     * @param response
     */
    @Override
    public void getSnap(String url, HttpServletResponse response) {
        int ret = 0;
        int videoIndex = -1;
        AVFormatContext oFmtCtx = null;
        AVFormatContext iFmtCtx = null;
        AVCodecContext deCodecCtx = null;
        AVCodecContext enCodecCtx = null;
        AVPacket srcPacket = null;
        AVFrame frame = null;
        AVPacket packet = null;
        AVStream vStream = null;
        String imgPath = null;
        File snapPath = new File(mediaServerConfig.getRootPath() + "/snap");
        if (!snapPath.exists()) {
            snapPath.mkdirs();
        }
        imgPath = snapPath.getAbsolutePath() + "/" + RandomUtil.randomString(10) + ".jpg";
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
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        ret=avformat.avformat_find_stream_info(iFmtCtx,(PointerPointer) null);
        if (ret < 0) {
            avutil.av_log(iFmtCtx, AV_LOG_ERROR, "avformat_find_stream_info error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        videoIndex = avformat.av_find_best_stream(iFmtCtx, AVMEDIA_TYPE_VIDEO, -1, -1, (PointerPointer) null, 0);
        if (videoIndex < 0) {
            avutil.av_log(iFmtCtx, AV_LOG_ERROR, "av_find_best_stream error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        vStream = iFmtCtx.streams(videoIndex);
        srcPacket = avcodec.av_packet_alloc();
        while ((ret = avformat.av_read_frame(iFmtCtx, srcPacket)) == 0) {
            if (srcPacket.stream_index() == videoIndex) {
                if (srcPacket.flags() == avcodec.AV_PKT_FLAG_KEY) {
                    break;
                }
            }
            avcodec.av_packet_unref(srcPacket);
        }
        if (ret < 0) {
            avutil.av_log(iFmtCtx, AV_LOG_ERROR, "av_read_frame error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        AVCodec deCodec = avcodec.avcodec_find_decoder(vStream.codecpar().codec_id());
        deCodecCtx = avcodec.avcodec_alloc_context3(deCodec);
        AVCodec enCodec = avcodec.avcodec_find_encoder(avcodec.AV_CODEC_ID_MJPEG);
        enCodecCtx = avcodec.avcodec_alloc_context3(enCodec);
        ret = avcodec.avcodec_parameters_to_context(deCodecCtx, vStream.codecpar());
        if (ret < 0) {
            avutil.av_log(deCodecCtx, AV_LOG_ERROR, "avcodec_parameters_to_context error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        ret = avcodec.avcodec_open2(deCodecCtx, deCodec, (PointerPointer) null);
        if (ret < 0) {
            avutil.av_log(deCodecCtx, AV_LOG_ERROR, "avcodec_open2 error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        oFmtCtx = new AVFormatContext(null);
        ret = avformat.avformat_alloc_output_context2(oFmtCtx, null, "mjpeg", imgPath);
        if (ret < 0) {
            return;
        }
        AVStream oStream = avformat.avformat_new_stream(oFmtCtx, null);
        enCodecCtx.width(deCodecCtx.width());
        enCodecCtx.height(deCodecCtx.height());
        enCodecCtx.time_base(avutil.av_make_q(1, 1));
        enCodecCtx.pix_fmt(AV_PIX_FMT_YUVJ420P);
        avcodec.avcodec_parameters_from_context(oStream.codecpar(), enCodecCtx);
        ret = avcodec.avcodec_open2(enCodecCtx, enCodec, (PointerPointer) null);
        if (ret < 0) {
            avutil.av_log(enCodecCtx, AV_LOG_ERROR, "avcodec_open2 error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        frame = avutil.av_frame_alloc();
        frame.width(enCodecCtx.width());
        frame.height(enCodecCtx.height());
        frame.format(enCodecCtx.pix_fmt());
        ret = av_frame_get_buffer(frame, 0);
        if (ret < 0) {
            avutil.av_log(enCodecCtx, AV_LOG_ERROR, "av_frame_get_buffer error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        if (avutil.av_frame_is_writable(frame) != 1) {
            avutil.av_frame_make_writable(frame);
        }
        packet = avcodec.av_packet_alloc();
        packet.stream_index(oStream.index());
        avcodec.avcodec_send_packet(deCodecCtx, srcPacket);
        avcodec.avcodec_send_packet(deCodecCtx, null);
        ret = avcodec.avcodec_receive_frame(deCodecCtx, frame);
        if (ret < 0 && ret != AVERROR_EOF() && ret != AVERROR_EAGAIN()) {
            avutil.av_log(deCodecCtx, AV_LOG_ERROR, "avcodec_receive_frame error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        avcodec.avcodec_send_frame(enCodecCtx, frame);
        avcodec.avcodec_send_frame(enCodecCtx, null);
        ret = avcodec.avcodec_receive_packet(enCodecCtx, packet);
        if (ret < 0 && ret != AVERROR_EOF() && ret != AVERROR_EAGAIN()) {
            avutil.av_log(enCodecCtx, AV_LOG_ERROR, "avcodec_receive_packet error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        AVIOContext pb = new AVIOContext((Pointer) null);
        ret = avformat.avio_open(pb, imgPath, avformat.AVIO_FLAG_WRITE);
        if (ret < 0) {
            avutil.av_log(oFmtCtx, AV_LOG_ERROR, "avio_open error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        oFmtCtx.pb(pb);
        ret = avformat.avformat_write_header(oFmtCtx, (PointerPointer) null);
        if (ret < 0) {
            avutil.av_log(oFmtCtx, AV_LOG_ERROR, "avformat_write_header error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        ret = avformat.av_write_frame(oFmtCtx, packet);
        if (ret < 0) {
            avutil.av_log(oFmtCtx, AV_LOG_ERROR, "av_write_frame error \n");
            writeError(response);
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
            return;
        }
        avcodec.av_packet_unref(packet);
        avcodec.av_packet_unref(srcPacket);
        avformat.av_write_trailer(oFmtCtx);
        writeImg(imgPath, response);
        free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, frame, srcPacket, packet);
    }

    /**
     * 写图片
     * @param imgPath
     * @param response
     */
    private void writeImg(String imgPath, HttpServletResponse response) {
        File file = new File(imgPath);
        if (!file.exists()) {
            writeError(response);
        }
        Long fileLength = file.length();
        try (InputStream ins = new FileInputStream(file)) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "OPTIONS,GET, POST, PUT, DELETE");
            response.setHeader("Content-Disposition", "inline;filename=" + imgPath.substring(imgPath.lastIndexOf("/")+1, imgPath.length()));
            response.setHeader("Content-Length", String.valueOf(fileLength));
            response.setContentType("image/jpg");
            IoUtil.copy(ins, response.getOutputStream());
        } catch (Exception e) {
            log.error("【Response】写入图片失败，原因：{}", e.getMessage());
            writeError(response);
        }
    }

    /**
     * 写错误
     * @param response
     */
    private void writeError(HttpServletResponse response) {
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write("截图失败".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
        }
    }


    /**
     * 释放资源
     * @param deCodecCtx
     * @param enCodecCtx
     * @param iFmtCtx
     * @param oFmtCtx
     * @param frame
     * @param srcPacket
     * @param packet
     */
    private void free(AVCodecContext deCodecCtx, AVCodecContext enCodecCtx, AVFormatContext iFmtCtx, AVFormatContext oFmtCtx, AVFrame frame, AVPacket srcPacket, AVPacket packet) {
        if (deCodecCtx != null) {
            avcodec.avcodec_free_context(deCodecCtx);
        }
        if (enCodecCtx != null) {
            avcodec.avcodec_free_context(enCodecCtx);
        }
        if (iFmtCtx != null) {
            avformat.avformat_close_input(iFmtCtx);
        }
        if (oFmtCtx != null) {
            avformat.avformat_free_context(oFmtCtx);
        }
        if (frame != null) {
            avutil.av_frame_free(frame);
        }
        if (srcPacket != null) {
            avcodec.av_packet_free(srcPacket);
        }
        if (packet != null) {
            avcodec.av_packet_free(packet);
        }
    }


}
