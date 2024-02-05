package com.ldf.media.api.service.impl;

import cn.hutool.core.lang.Assert;
import com.aizuda.structure.MK_INI;
import com.ldf.media.api.model.param.*;
import com.ldf.media.api.model.result.MediaInfoResult;
import com.ldf.media.api.model.result.Track;
import com.ldf.media.api.service.IApiService;
import com.ldf.media.callback.MKSourceFindCallBack;
import com.ldf.media.constants.MediaServerConstants;
import com.ldf.media.context.MediaServerContext;
import com.aizuda.callback.IMKProxyPlayCloseCallBack;
import com.aizuda.structure.MK_MEDIA_SOURCE;
import com.aizuda.structure.MK_PROXY_PLAYER;
import com.aizuda.structure.MK_TRACK;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.ldf.media.context.MediaServerContext.ZLM_API;

/**
 * 接口服务
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
@Service
public class ApiServiceImpl implements IApiService {
    @Override
    public void addStreamProxy(StreamProxyParam param) {
        //查询流是是否存在
        MK_MEDIA_SOURCE mkMediaSource = ZLM_API.mk_media_source_find2(param.getEnableRtmp() == 1 ? "rtmp" : "rtsp", MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream(), 0);
        Assert.isNull(mkMediaSource, "当前流信息已被使用");
        //创建拉流代理
        MK_INI option = ZLM_API.mk_ini_create();
        ZLM_API.mk_ini_set_option_int(option,"hls_enabled",param.getEnableHls());
        ZLM_API.mk_ini_set_option_int(option,"mp4_enabled",param.getEnableMp4());
        ZLM_API.mk_ini_set_option_int(option,"enable_audio",param.getEnableAudio());
        ZLM_API.mk_ini_set_option_int(option,"enable_fmp4",param.getEnableFmp4());
        ZLM_API.mk_ini_set_option_int(option,"enable_ts",param.getEnableTs());
        ZLM_API.mk_ini_set_option_int(option,"enable_hls",param.getEnableHls());
        ZLM_API.mk_ini_set_option_int(option,"enable_rtsp",param.getEnableRtsp());
        ZLM_API.mk_ini_set_option_int(option,"enable_rtmp",param.getEnableRtmp());
        ZLM_API.mk_ini_set_option_int(option,"mp4_max_second",param.getMp4MaxSecond());
        //ZLM_API.mk_ini_set_option(option,"mp4_save_path","D:/record");
        //ZLM_API.mk_ini_set_option(option,"hls_save_path","D:/record");
        ZLM_API.mk_ini_set_option_int(option,"add_mute_audio",0);
        ZLM_API.mk_ini_set_option_int(option,"auto_close",1);
        //创建拉流代理
        MK_PROXY_PLAYER mk_proxy = ZLM_API.mk_proxy_player_create2(MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream(),option);
        //删除配置
        ZLM_API.mk_ini_release(option);
        //回调关闭时间
        IMKProxyPlayCloseCallBack imkProxyPlayCloseCallBack = (pUser, err, what, sys_err) -> {
            //这里Pointer是ZLM维护的不需要我们释放 遵循谁申请谁释放原则
            ZLM_API.mk_proxy_player_release(new MK_PROXY_PLAYER(pUser));
        };
        ZLM_API.mk_proxy_player_set_option(mk_proxy, "rtp_type", param.getRtpType().toString());
        //开始播放
        ZLM_API.mk_proxy_player_play(mk_proxy, param.getUrl());
        //添加代理关闭回调 并把代理客户端传过去释放
        ZLM_API.mk_proxy_player_set_on_close(mk_proxy, imkProxyPlayCloseCallBack, mk_proxy.getPointer());
    }

    @Override
    public Integer closeStream(CloseStreamParam param) {
        //查询流是是否存在
        MK_MEDIA_SOURCE mkMediaSource = ZLM_API.mk_media_source_find2(param.getSchema(), MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream(), 0);
        Assert.notNull(mkMediaSource, "当前流不在线");
        return ZLM_API.mk_media_source_close(mkMediaSource, param.getForce());
    }

    @Override
    public Integer closeStreams(CloseStreamsParam param) {
        AtomicReference<Integer> count = new AtomicReference<>(0);
        ZLM_API.mk_media_source_for_each(Pointer.NULL, new MKSourceFindCallBack((MK_MEDIA_SOURCE ctx) -> {
            int status = ZLM_API.mk_media_source_close(ctx, param.getForce());
            count.set(count.get() + status);
        }), param.getSchema(), MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream());
        return count.get();
    }

    @Override
    public List<MediaInfoResult> getMediaList(GetMediaListParam param) {
        List<MediaInfoResult> list = new ArrayList<>();
        ZLM_API.mk_media_source_for_each(Pointer.NULL, new MKSourceFindCallBack((MK_MEDIA_SOURCE ctx) -> {
            String app = ZLM_API.mk_media_source_get_app(ctx);
            String stream = ZLM_API.mk_media_source_get_stream(ctx);
            String schema = ZLM_API.mk_media_source_get_schema(ctx);
            int readerCount = ZLM_API.mk_media_source_get_reader_count(ctx);
            int totalReaderCount = ZLM_API.mk_media_source_get_total_reader_count(ctx);
            int trackSize = ZLM_API.mk_media_source_get_track_count(ctx);
            MediaInfoResult mediaInfoResult = new MediaInfoResult();
            mediaInfoResult.setApp(app);
            mediaInfoResult.setStream(stream);
            mediaInfoResult.setSchema(schema);
            mediaInfoResult.setReaderCount(readerCount);
            mediaInfoResult.setTotalReaderCount(totalReaderCount);
            List<Track> tracks = new ArrayList<>();
            for (int i = 0; i < trackSize; i++) {
                MK_TRACK mkTrack = ZLM_API.mk_media_source_get_track(ctx, i);
                Track track = new Track();
                int codec_id = ZLM_API.mk_track_codec_id(mkTrack);
                String codec_name = ZLM_API.mk_track_codec_name(mkTrack);
                int bit_rate = ZLM_API.mk_track_bit_rate(mkTrack);
                int is_video = ZLM_API.mk_track_is_video(mkTrack);
                track.setCodec_id(codec_id);
                track.setCodec_id_name(codec_name);
                track.setBit_rate(bit_rate);
                track.setIs_video(is_video);
                if (is_video == 1) {
                    int width = ZLM_API.mk_track_video_width(mkTrack);
                    track.setWidth(width);
                    int height = ZLM_API.mk_track_video_height(mkTrack);
                    track.setHeight(height);
                    int fps = ZLM_API.mk_track_video_fps(mkTrack);
                    track.setFps(fps);
                } else {
                    int sample_rate = ZLM_API.mk_track_audio_sample_rate(mkTrack);
                    int audio_channel = ZLM_API.mk_track_audio_channel(mkTrack);
                    int audio_sample_bit = ZLM_API.mk_track_audio_sample_bit(mkTrack);
                    track.setSample_rate(sample_rate);
                    track.setAudio_channel(audio_channel);
                    track.setAudio_sample_bit(audio_sample_bit);
                }
                tracks.add(track);
            }
            mediaInfoResult.setTracks(tracks);
            list.add(mediaInfoResult);
        }), param.getSchema(), MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream());
        return list;
    }

    @Override
    public Boolean startRecord(StartRecordParam param) {
        int ret = ZLM_API.mk_recorder_start(param.getType(), MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream(), param.getCustomized_path(), param.getMax_second());
        return ret == 1;
    }

    @Override
    public Boolean stopRecord(StopRecordParam param) {
        int ret = ZLM_API.mk_recorder_stop(param.getType(), MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream());
        return ret == 1;
    }

    @Override
    public Boolean isRecording(RecordStatusParam param) {
        int ret = ZLM_API.mk_recorder_is_recording(param.getType(), MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream());
        return ret == 1;
    }

}
