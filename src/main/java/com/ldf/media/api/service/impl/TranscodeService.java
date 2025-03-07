package com.ldf.media.api.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ldf.media.api.model.param.TranscodeParam;
import com.ldf.media.api.service.ITranscodeService;
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
import org.bytedeco.ffmpeg.global.swscale;
import org.bytedeco.ffmpeg.swscale.SwsContext;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.bytedeco.ffmpeg.global.avutil.*;
import static org.bytedeco.ffmpeg.presets.avutil.AVERROR_EAGAIN;

@Slf4j
@Service
public class TranscodeService implements ITranscodeService {
    @Autowired
    private MediaServerConfig mediaServerConfig;

    @Override
    public void transcode(TranscodeParam param) {
        int ret = 0;
        int videoIndex = -1;
        int audioIndex = -1;
        AVFormatContext oFmtCtx = null;
        AVFormatContext iFmtCtx = null;
        AVCodecContext deCodecCtx = null;
        AVCodecContext enCodecCtx = null;
        SwsContext swsCtx = null;
        AVPacket srcPacket = null;
        AVFrame frame = null;
        AVFrame swsFrame = null;
        AVPacket packet = null;
        AVStream vStream = null;
        AVStream aStream = null;
        AVStream oVStream = null;
        AVStream oAStream = null;
        boolean needScale = false;
        String pushUrl = StrUtil.format("rtmp://127.0.0.1:{}/{}/{}", mediaServerConfig.getRtmp_port(), param.getApp(), param.getStream());
        iFmtCtx = new AVFormatContext(null);
        boolean isRtsp = param.getUrl().startsWith("rtsp");
        if (isRtsp) {
            AVDictionary rtspOptions = new AVDictionary(null);
            avutil.av_dict_set(rtspOptions, "rtsp_transport", "tcp", 0);
            ret = avformat.avformat_open_input(iFmtCtx, param.getUrl(), null, rtspOptions);
            avutil.av_dict_free(rtspOptions);
        } else {
            ret = avformat.avformat_open_input(iFmtCtx, param.getUrl(), null, null);
        }
        if (ret < 0) {
            avutil.av_log(iFmtCtx, AV_LOG_ERROR, "avformat_open_input error \n");
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
            return;
        }
        ret = avformat.avformat_find_stream_info(iFmtCtx, (PointerPointer) null);
        if (ret < 0) {
            avutil.av_log(iFmtCtx, AV_LOG_ERROR, "avformat_find_stream_info error \n");
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
            return;
        }
        oFmtCtx = new AVFormatContext(null);
        ret = avformat.avformat_alloc_output_context2(oFmtCtx, null, "flv", pushUrl);
        if (ret < 0) {
            return;
        }
        for (int i = 0; i < iFmtCtx.nb_streams(); i++) {
            AVStream stream = iFmtCtx.streams(i);
            if (stream.codecpar().codec_type() == AVMEDIA_TYPE_VIDEO) {
                oVStream = avformat.avformat_new_stream(oFmtCtx, null);
                vStream = stream;
                videoIndex = i;
            } else if (stream.codecpar().codec_type() == AVMEDIA_TYPE_AUDIO && param.getEnableAudio()) {
                oAStream = avformat.avformat_new_stream(oFmtCtx, null);
                avcodec.avcodec_parameters_copy(oAStream.codecpar(), stream.codecpar());
                aStream = stream;
                audioIndex = i;
            }
        }
        AVCodec deCodec = avcodec.avcodec_find_decoder(vStream.codecpar().codec_id());
        deCodecCtx = avcodec.avcodec_alloc_context3(deCodec);
        AVCodec enCodec = avcodec.avcodec_find_encoder_by_name("libx264");
        enCodecCtx = avcodec.avcodec_alloc_context3(enCodec);
        ret = avcodec.avcodec_parameters_to_context(deCodecCtx, vStream.codecpar());
        if (ret < 0) {
            avutil.av_log(deCodecCtx, AV_LOG_ERROR, "avcodec_parameters_to_context error \n");
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
            return;
        }
        ret = avcodec.avcodec_open2(deCodecCtx, deCodec, (PointerPointer) null);
        if (ret < 0) {
            avutil.av_log(deCodecCtx, AV_LOG_ERROR, "avcodec_open2 error \n");
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
            return;
        }
        if (param.getScaleWidth() != 0) {
            enCodecCtx.width(param.getScaleWidth());
            needScale = true;
        } else {
            enCodecCtx.width(deCodecCtx.width());
        }
        if (param.getScaleHeight() != 0) {
            enCodecCtx.height(param.getScaleHeight());
            needScale = true;
        } else {
            enCodecCtx.height(deCodecCtx.height());
        }
        enCodecCtx.gop_size(avutil.av_q2intfloat(vStream.avg_frame_rate()));
        enCodecCtx.has_b_frames(0);
        enCodecCtx.max_b_frames(0);
        enCodecCtx.profile(AVCodecContext.FF_PROFILE_H264_BASELINE);
        enCodecCtx.framerate(vStream.avg_frame_rate());
        enCodecCtx.time_base(avutil.av_make_q(vStream.avg_frame_rate().den(), vStream.avg_frame_rate().num()));
        enCodecCtx.pix_fmt(AV_PIX_FMT_YUV420P);
        avcodec.avcodec_parameters_from_context(oVStream.codecpar(), enCodecCtx);
        oVStream.codecpar().codec_tag(0);
        ret = avcodec.avcodec_open2(enCodecCtx, enCodec, (PointerPointer) null);
        if (ret < 0) {
            avutil.av_log(enCodecCtx, AV_LOG_ERROR, "avcodec_open2 error \n");
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
            return;
        }
        if (needScale) {
            swsCtx = swscale.sws_getContext(deCodecCtx.width(), deCodecCtx.height(), deCodecCtx.pix_fmt(), enCodecCtx.width(), enCodecCtx.height(), enCodecCtx.pix_fmt(), swscale.SWS_BICUBIC, null, null, (DoublePointer) null);
            if (swsCtx == null) {
                avutil.av_log(swsCtx, AV_LOG_ERROR, "sws_getContext error \n");
                free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
                return;
            }
        }
        frame = avutil.av_frame_alloc();
        frame.width(deCodecCtx.width());
        frame.height(deCodecCtx.height());
        frame.format(deCodecCtx.pix_fmt());
        ret = av_frame_get_buffer(frame, 0);
        if (ret < 0) {
            avutil.av_log(deCodecCtx, AV_LOG_ERROR, "av_frame_get_buffer error \n");
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
            return;
        }
        if (avutil.av_frame_is_writable(frame) != 1) {
            avutil.av_frame_make_writable(frame);
        }
        if (needScale) {
            swsFrame = avutil.av_frame_alloc();
            swsFrame.width(enCodecCtx.width());
            swsFrame.height(enCodecCtx.height());
            swsFrame.format(enCodecCtx.pix_fmt());
            ret = av_frame_get_buffer(swsFrame, 0);
            if (ret < 0) {
                avutil.av_log(swsCtx, AV_LOG_ERROR, "av_frame_get_buffer error \n");
                free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
                return;
            }
            if (avutil.av_frame_is_writable(swsFrame) != 1) {
                avutil.av_frame_make_writable(swsFrame);
            }
        }
        packet = avcodec.av_packet_alloc();
        packet.stream_index(oVStream.index());
        srcPacket = avcodec.av_packet_alloc();
        AVIOContext pb = new AVIOContext((Pointer) null);
        ret = avformat.avio_open(pb, pushUrl, avformat.AVIO_FLAG_WRITE);
        if (ret < 0) {
            avutil.av_log(oFmtCtx, AV_LOG_ERROR, "avio_open error \n");
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
            return;
        }
        oFmtCtx.pb(pb);
        ret = avformat.avformat_write_header(oFmtCtx, (PointerPointer) null);
        if (ret < 0) {
            avutil.av_log(oFmtCtx, AV_LOG_ERROR, "avformat_write_header error \n");
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
            return;
        }
        while ((ret = avformat.av_read_frame(iFmtCtx, srcPacket)) == 0) {
            if (srcPacket.stream_index() == videoIndex) {
                ret = avcodec.avcodec_send_packet(deCodecCtx, srcPacket);
                if (ret < 0) {
                    avutil.av_log(deCodecCtx, AV_LOG_ERROR, "avcodec_send_packet error \n");
                    free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
                    return;
                }
                while (true) {
                    ret = avcodec.avcodec_receive_frame(deCodecCtx, frame);
                    if (ret == AVERROR_EAGAIN() || ret == AVERROR_EOF()) {
                        break;
                    }
                    if (ret < 0) {
                        avutil.av_log(deCodecCtx, AV_LOG_ERROR, "avcodec_receive_frame error \n");
                        free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
                        return;
                    }
                    AVFrame tempFrame = null;
                    if (needScale) {
                        swscale.sws_scale(swsCtx, frame.data(), frame.linesize(), 0, frame.height(), swsFrame.data(), swsFrame.linesize());
                        swsFrame.pts(frame.pts());
                        swsFrame.pkt_dts(frame.pkt_dts());
                        swsFrame.duration(frame.duration());
                        tempFrame = swsFrame;
                    } else {
                        tempFrame = frame;
                    }
                    ret = avcodec.avcodec_send_frame(enCodecCtx, tempFrame);
                    if (ret < 0) {
                        avutil.av_log(enCodecCtx, AV_LOG_ERROR, "avcodec_send_frame error \n");
                        free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
                        return;
                    }
                    while (true) {
                        ret = avcodec.avcodec_receive_packet(enCodecCtx, packet);
                        if (ret == AVERROR_EAGAIN() || ret == AVERROR_EOF()) {
                            break;
                        }
                        if (ret < 0) {
                            avutil.av_log(enCodecCtx, AV_LOG_ERROR, "avcodec_receive_packet error \n");
                            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
                            return;
                        }
                        avcodec.av_packet_rescale_ts(packet, vStream.time_base(), oVStream.time_base());
                        packet.dts(packet.pts());
                        avformat.av_interleaved_write_frame(oFmtCtx, packet);
                        if (ret < 0) {
                            avutil.av_log(oFmtCtx, AV_LOG_ERROR, "av_interleaved_write_frame error \n");
                            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
                            return;
                        }
                        avcodec.av_packet_unref(packet);
                    }
                }
            } else if (srcPacket.stream_index() == audioIndex && param.getEnableAudio()) {
                avcodec.av_packet_rescale_ts(srcPacket, aStream.time_base(), oAStream.time_base());
                srcPacket.stream_index(oAStream.index());
                ret = avformat.av_interleaved_write_frame(oFmtCtx, srcPacket);
                if (ret < 0) {
                    avutil.av_log(oFmtCtx, AV_LOG_ERROR, "av_interleaved_write_frame error \n");
                    free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
                    return;
                }
            }
            avcodec.av_packet_unref(srcPacket);
        }
        if (ret < 0) {
            avutil.av_log(iFmtCtx, AV_LOG_ERROR, "av_read_frame error \n");
            free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
            return;
        }
        avformat.av_write_trailer(oFmtCtx);
        free(deCodecCtx, enCodecCtx, iFmtCtx, oFmtCtx, swsCtx, frame, swsFrame, srcPacket, packet);
    }


    /**
     * 释放资源
     *
     * @param deCodecCtx
     * @param enCodecCtx
     * @param iFmtCtx
     * @param oFmtCtx
     * @param frame
     * @param srcPacket
     * @param packet
     */
    private void free(AVCodecContext deCodecCtx, AVCodecContext enCodecCtx, AVFormatContext iFmtCtx, AVFormatContext oFmtCtx, SwsContext swsCtx, AVFrame frame, AVFrame swsFrame, AVPacket srcPacket, AVPacket packet) {
        if (deCodecCtx != null) {
            avcodec.avcodec_free_context(deCodecCtx);
            deCodecCtx = null;
        }
        if (enCodecCtx != null) {
            avcodec.avcodec_free_context(enCodecCtx);
            enCodecCtx = null;
        }
        if (iFmtCtx != null) {
            avformat.avformat_close_input(iFmtCtx);
            iFmtCtx = null;
        }
        if (oFmtCtx != null) {
            avformat.avformat_free_context(oFmtCtx);
            oFmtCtx = null;
        }
        if (swsCtx != null) {
            swscale.sws_freeContext(swsCtx);
            swsCtx = null;
        }
        if (frame != null) {
            avutil.av_frame_free(frame);
            frame = null;
        }
        if (swsFrame != null) {
            avutil.av_frame_free(swsFrame);
            swsFrame = null;
        }
        if (srcPacket != null) {
            avcodec.av_packet_free(srcPacket);
            srcPacket = null;
        }
        if (packet != null) {
            avcodec.av_packet_free(packet);
            packet = null;
        }
    }

}
