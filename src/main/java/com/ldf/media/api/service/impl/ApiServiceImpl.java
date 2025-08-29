package com.ldf.media.api.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import com.aizuda.zlm4j.callback.IMKGetStatisticCallBack;
import com.aizuda.zlm4j.callback.IMKProxyPlayerCallBack;
import com.aizuda.zlm4j.callback.IMKPushEventCallBack;
import com.aizuda.zlm4j.callback.IMKRtpServerDetachCallBack;
import com.aizuda.zlm4j.structure.*;
import com.ldf.media.api.model.param.*;
import com.ldf.media.api.model.result.*;
import com.ldf.media.api.service.IApiService;
import com.ldf.media.callback.MKSourceFindCallBack;
import com.ldf.media.config.MediaServerConfig;
import com.ldf.media.constants.MediaServerConstants;
import com.ldf.media.context.MediaServerContext;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.ldf.media.context.MediaServerContext.ZLM_API;

/**
 * 接口服务
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
@Slf4j
@Service
public class ApiServiceImpl implements IApiService {
    @Autowired
    private MediaServerContext context;
    @Autowired
    private MediaServerConfig config;

    /**
     * 拉流代理列表
     */
    private static final Map<String, MK_PROXY_PLAYER> PROXY_PLAYER_MAP = new HashMap<>();

    /**
     * 推流代理列表
     */
    private static final Map<String, MK_PUSHER> PUSH_PROXY_PLAYER_MAP = new HashMap<>();


    /**
     * 拉流代理关闭回调
     */
    private static final Map<String, IMKProxyPlayerCallBack> PROXY_PLAYER_CLOSE_MAP = new HashMap<>();

    /**
     * 推流代理关闭回调
     */
    private static final Map<String, IMKPushEventCallBack> PUSH_PROXY_PLAYER_CLOSE_MAP = new HashMap<>();

    /**
     * rtp服务列表
     */
    private static final Map<String, MK_RTP_SERVER> RTP_SERVER_MAP = new HashMap<>();


    /**
     * rtp服务断开回调
     */
    private static final Map<String, IMKRtpServerDetachCallBack> RTP_SERVER_DETACH_CALL_BACK_MAP = new HashMap<>();

    @Override
    public StreamUrlResult addStreamProxy(StreamProxyParam param) {
        String key = RandomUtil.randomString(10);
        //查询流是是否存在
        MK_MEDIA_SOURCE mkMediaSource = ZLM_API.mk_media_source_find2(param.getEnableRtmp() == 1 ? "rtmp" : "rtsp", MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream(), 0);
        Assert.isNull(mkMediaSource, "当前流信息已被使用");
        //创建拉流代理
        MK_INI option = ZLM_API.mk_ini_create();
        ZLM_API.mk_ini_set_option_int(option, "enable_mp4", param.getEnableMp4());
        ZLM_API.mk_ini_set_option_int(option, "enable_audio", param.getEnableAudio());
        ZLM_API.mk_ini_set_option_int(option, "enable_fmp4", param.getEnableFmp4());
        ZLM_API.mk_ini_set_option_int(option, "enable_ts", param.getEnableTs());
        ZLM_API.mk_ini_set_option_int(option, "enable_hls", param.getEnableHls());
        ZLM_API.mk_ini_set_option_int(option, "enable_rtsp", param.getEnableRtsp());
        ZLM_API.mk_ini_set_option_int(option, "enable_rtmp", param.getEnableRtmp());
        ZLM_API.mk_ini_set_option_int(option, "mp4_max_second", param.getMp4MaxSecond());
        //ZLM_API.mk_ini_set_option(option,"mp4_save_path","D:/record");
        //ZLM_API.mk_ini_set_option(option,"hls_save_path","D:/record");
        ZLM_API.mk_ini_set_option_int(option, "add_mute_audio", 0);
        ZLM_API.mk_ini_set_option_int(option, "auto_close", param.getAutoClose());

        //创建拉流代理
        MK_PROXY_PLAYER mk_proxy = ZLM_API.mk_proxy_player_create4(MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream(), option, param.getRetryCount());
        //设置超时时间
        if (param.getTimeoutSec() != null) {
            ZLM_API.mk_proxy_player_set_option(mk_proxy, "protocol_timeout_ms", String.valueOf(param.getTimeoutSec() * 1000));
        }
        if (param.getRtspSpeed() != null) {
            ZLM_API.mk_proxy_player_set_option(mk_proxy, "rtsp_speed", param.getRtspSpeed().setScale(2, RoundingMode.HALF_UP).toString());
        }
        //设置拉流方式
        if (param.getRtpType() != null) {
            ZLM_API.mk_proxy_player_set_option(mk_proxy, "rtp_type", param.getRtpType().toString());
        }
        //删除配置
        ZLM_API.mk_ini_release(option);
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
        //第一次代理结果获取
        IMKProxyPlayerCallBack imkProxyPlayerCallBack = (pUser, err, what, sys_err) -> {
            if (err != 0) {
                log.warn("【MediaServer】拉流代理失败：{}", what);
                queue.offer(what);
            } else {
                queue.offer(key);
                log.info("【MediaServer】拉流代理成功");
            }
        };
        ZLM_API.mk_proxy_player_set_on_play_result(mk_proxy, imkProxyPlayerCallBack, mk_proxy.getPointer(), null);
        //回调关闭事件
        IMKProxyPlayerCallBack imkProxyPlayCloseCallBack = (pUser, err, what, sys_err) -> {
            //这里Pointer是ZLM维护的不需要我们释放 遵循谁申请谁释放原则
            PROXY_PLAYER_CLOSE_MAP.remove(key);
            PROXY_PLAYER_MAP.remove(key);
            log.info("【MediaServer】拉流代理关闭");
            ZLM_API.mk_proxy_player_release(new MK_PROXY_PLAYER(pUser));
        };
        PROXY_PLAYER_CLOSE_MAP.put(key, imkProxyPlayCloseCallBack);
        PROXY_PLAYER_MAP.put(key, mk_proxy);
        //开始播放
        ZLM_API.mk_proxy_player_play(mk_proxy, param.getUrl());
        //添加代理关闭回调 并把代理客户端传过去释放
        ZLM_API.mk_proxy_player_set_on_close(mk_proxy, imkProxyPlayCloseCallBack, mk_proxy.getPointer());
        try {
            String error = queue.poll(5, TimeUnit.SECONDS);
            Assert.isTrue(key.equals(error), error);
            return new StreamUrlResult(config, param, key);
        } catch (InterruptedException e) {
        }
        return new StreamUrlResult(config, param, key);
    }

    @Override
    public Boolean delStreamProxy(String key) {
        MK_PROXY_PLAYER mkProxyPlayer = PROXY_PLAYER_MAP.get(key);
        if (mkProxyPlayer != null) {
            ZLM_API.mk_proxy_player_release(mkProxyPlayer);
            PROXY_PLAYER_MAP.remove(key);
            PROXY_PLAYER_CLOSE_MAP.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public String addStreamPusherProxy(StreamPushProxyParam param) {
        String key = RandomUtil.randomString(10);
        MK_MEDIA_SOURCE mkMediaSource = ZLM_API.mk_media_source_find2(param.getSchema(), MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream(), 0);
        if (mkMediaSource == null) {
            return "转推流不存在";
        }
        MK_PUSHER mkPusher = ZLM_API.mk_pusher_create_src(mkMediaSource);
        if (param.getUrl().startsWith("rtsp")) {
            ZLM_API.mk_pusher_set_option(mkPusher, "rtp_type", param.getRtpType().toString());
        }
        if (param.getTimeoutSec() != null) {
            ZLM_API.mk_pusher_set_option(mkPusher, "protocol_timeout_ms", String.valueOf(param.getTimeoutSec() * 1000));
        }
        PUSH_PROXY_PLAYER_MAP.put(key, mkPusher);
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
        //推流结果回调
        ZLM_API.mk_pusher_set_on_result(mkPusher, (user_data, err_code, err_msg) -> {
            if (err_code != 0) {
                queue.offer(err_msg);
                log.warn("【MediaServer】推流代理失败：{}", err_msg);
            } else {
                queue.offer(key);
                log.info("【MediaServer】推流代理成功");
            }
        }, mkPusher.getPointer());
        IMKPushEventCallBack imkPushEventCallBack = (user_data, err_code, err_msg) -> {
            //释放推流器
            ZLM_API.mk_pusher_release(mkPusher);
            PUSH_PROXY_PLAYER_MAP.remove(key);
            PUSH_PROXY_PLAYER_CLOSE_MAP.remove(key);
            log.info("【MediaServer】推流代理关闭");
        };
        PUSH_PROXY_PLAYER_CLOSE_MAP.put(key, imkPushEventCallBack);
        //推流关闭回调
        ZLM_API.mk_pusher_set_on_shutdown(mkPusher, imkPushEventCallBack, mkPusher.getPointer());
        //转推流地址 可以是rtmp或者rtsp
        ZLM_API.mk_pusher_publish(mkPusher, param.getUrl());
        try {
            String error = queue.poll(5, TimeUnit.SECONDS);
            return error;
        } catch (InterruptedException e) {
        }
        return key;
    }

    @Override
    public Boolean delStreamPusherProxy(String key) {
        MK_PUSHER mkPusher = PUSH_PROXY_PLAYER_MAP.get(key);
        if (mkPusher != null) {
            ZLM_API.mk_pusher_release(mkPusher);
            PUSH_PROXY_PLAYER_MAP.remove(key);
            PUSH_PROXY_PLAYER_CLOSE_MAP.remove(key);
            return true;
        }
        return false;
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
            int originType = ZLM_API.mk_media_source_get_origin_type(ctx);
            Pointer originTypePointer = ZLM_API.mk_media_source_get_origin_type_str(ctx);
            long aliveSecond = ZLM_API.mk_media_source_get_alive_second(ctx);
            int bytesSpeed = ZLM_API.mk_media_source_get_bytes_speed(ctx);
            Pointer originUrlPointer = ZLM_API.mk_media_source_get_origin_url(ctx);
            long createStamp = ZLM_API.mk_media_source_get_create_stamp(ctx);
            int isRecordHls = ZLM_API.mk_media_source_is_recording(ctx, 0);
            int isRecordMp4 = ZLM_API.mk_media_source_is_recording(ctx, 1);
            MediaInfoResult mediaInfoResult = new MediaInfoResult();
            mediaInfoResult.setApp(app);
            mediaInfoResult.setStream(stream);
            mediaInfoResult.setSchema(schema);
            mediaInfoResult.setReaderCount(readerCount);
            mediaInfoResult.setTotalReaderCount(totalReaderCount);
            mediaInfoResult.setOriginType(originType);
            mediaInfoResult.setOriginUrl(originUrlPointer.getString(0));
            mediaInfoResult.setCreateStamp(createStamp);
            mediaInfoResult.setIsRecordingHLS(isRecordHls == 1);
            mediaInfoResult.setIsRecordingMP4(isRecordMp4 == 1);
            mediaInfoResult.setOriginTypeStr(originTypePointer.getString(0));
            mediaInfoResult.setAliveSecond(aliveSecond);
            mediaInfoResult.setBytesSpeed(bytesSpeed);
            List<Track> tracks = new ArrayList<>();
            for (int i = 0; i < trackSize; i++) {
                MK_TRACK mkTrack = ZLM_API.mk_media_source_get_track(ctx, i);
                Track track = new Track();
                int codec_id = ZLM_API.mk_track_codec_id(mkTrack);
                String codec_name = ZLM_API.mk_track_codec_name(mkTrack);
                int bit_rate = ZLM_API.mk_track_bit_rate(mkTrack);
                int is_video = ZLM_API.mk_track_is_video(mkTrack);
                int is_ready = ZLM_API.mk_track_ready(mkTrack);
                long duration = ZLM_API.mk_track_duration(mkTrack);
                long frames = ZLM_API.mk_track_frames(mkTrack);
                float loss = ZLM_API.mk_media_source_get_track_loss(ctx, mkTrack);
                track.setCodec_id(codec_id);
                track.setCodec_id_name(codec_name);
                track.setBit_rate(bit_rate);
                track.setIs_video(is_video);
                track.setDuration(duration);
                track.setReady(is_ready == 1);
                track.setFrames(frames);
                track.setLoss(loss);
                if (is_video == 1) {
                    int width = ZLM_API.mk_track_video_width(mkTrack);
                    track.setWidth(width);
                    int height = ZLM_API.mk_track_video_height(mkTrack);
                    track.setHeight(height);
                    int fps = ZLM_API.mk_track_video_fps(mkTrack);
                    track.setFps(fps);
                    int gop_size = ZLM_API.mk_track_video_gop_size(mkTrack);
                    track.setGop_size(gop_size);
                    int gop_interval_ms = ZLM_API.mk_track_video_gop_interval_ms(mkTrack);
                    track.setGop_interval_ms(gop_interval_ms);
                    long key_frames = ZLM_API.mk_track_video_key_frames(mkTrack);
                    track.setKey_frames(key_frames);
                } else {
                    int sample_rate = ZLM_API.mk_track_audio_sample_rate(mkTrack);
                    int audio_channel = ZLM_API.mk_track_audio_channel(mkTrack);
                    int audio_sample_bit = ZLM_API.mk_track_audio_sample_bit(mkTrack);
                    track.setSample_rate(sample_rate);
                    track.setAudio_channel(audio_channel);
                    track.setAudio_sample_bit(audio_sample_bit);
                }
                tracks.add(track);
                ZLM_API.mk_track_unref(mkTrack);
            }
            ZLM_API.mk_free(originTypePointer);
            ZLM_API.mk_free(originUrlPointer);
            mediaInfoResult.setTracks(tracks);
            list.add(mediaInfoResult);
        }), param.getSchema(), MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream());
        return list;
    }


    @Override
    public Boolean isMediaOnline(MediaQueryParam param) {
        MK_MEDIA_SOURCE mkMediaSource = ZLM_API.mk_media_source_find2(param.getSchema(), MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream(), 0);
        return mkMediaSource != null;
    }

    @Override
    public MediaInfoResult getMediaInfo(MediaQueryParam param) {
        MK_MEDIA_SOURCE mkMediaSource = ZLM_API.mk_media_source_find2(param.getSchema(), MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream(), 0);
        if (mkMediaSource != null) {
            String app = ZLM_API.mk_media_source_get_app(mkMediaSource);
            String stream = ZLM_API.mk_media_source_get_stream(mkMediaSource);
            String schema = ZLM_API.mk_media_source_get_schema(mkMediaSource);
            int readerCount = ZLM_API.mk_media_source_get_reader_count(mkMediaSource);
            int totalReaderCount = ZLM_API.mk_media_source_get_total_reader_count(mkMediaSource);
            int trackSize = ZLM_API.mk_media_source_get_track_count(mkMediaSource);
            int originType = ZLM_API.mk_media_source_get_origin_type(mkMediaSource);
            Pointer originTypePointer = ZLM_API.mk_media_source_get_origin_type_str(mkMediaSource);
            long aliveSecond = ZLM_API.mk_media_source_get_alive_second(mkMediaSource);
            int bytesSpeed = ZLM_API.mk_media_source_get_bytes_speed(mkMediaSource);
            Pointer originUrlPointer = ZLM_API.mk_media_source_get_origin_url(mkMediaSource);
            long createStamp = ZLM_API.mk_media_source_get_create_stamp(mkMediaSource);
            int isRecordHls = ZLM_API.mk_media_source_is_recording(mkMediaSource, 0);
            int isRecordMp4 = ZLM_API.mk_media_source_is_recording(mkMediaSource, 1);
            MediaInfoResult mediaInfoResult = new MediaInfoResult();
            mediaInfoResult.setApp(app);
            mediaInfoResult.setStream(stream);
            mediaInfoResult.setSchema(schema);
            mediaInfoResult.setReaderCount(readerCount);
            mediaInfoResult.setTotalReaderCount(totalReaderCount);
            mediaInfoResult.setOriginType(originType);
            mediaInfoResult.setOriginUrl(originUrlPointer.getString(0));
            mediaInfoResult.setCreateStamp(createStamp);
            mediaInfoResult.setIsRecordingHLS(isRecordHls == 1);
            mediaInfoResult.setIsRecordingMP4(isRecordMp4 == 1);
            mediaInfoResult.setOriginTypeStr(originTypePointer.getString(0));
            mediaInfoResult.setAliveSecond(aliveSecond);
            mediaInfoResult.setBytesSpeed(bytesSpeed);
            List<Track> tracks = new ArrayList<>();
            for (int i = 0; i < trackSize; i++) {
                MK_TRACK mkTrack = ZLM_API.mk_media_source_get_track(mkMediaSource, i);
                Track track = new Track();
                int codec_id = ZLM_API.mk_track_codec_id(mkTrack);
                String codec_name = ZLM_API.mk_track_codec_name(mkTrack);
                int bit_rate = ZLM_API.mk_track_bit_rate(mkTrack);
                int is_video = ZLM_API.mk_track_is_video(mkTrack);
                int is_ready = ZLM_API.mk_track_ready(mkTrack);
                long duration = ZLM_API.mk_track_duration(mkTrack);
                long frames = ZLM_API.mk_track_frames(mkTrack);
                float loss = ZLM_API.mk_media_source_get_track_loss(mkMediaSource, mkTrack);
                track.setCodec_id(codec_id);
                track.setCodec_id_name(codec_name);
                track.setBit_rate(bit_rate);
                track.setIs_video(is_video);
                track.setDuration(duration);
                track.setReady(is_ready == 1);
                track.setFrames(frames);
                track.setLoss(loss);
                if (is_video == 1) {
                    int width = ZLM_API.mk_track_video_width(mkTrack);
                    track.setWidth(width);
                    int height = ZLM_API.mk_track_video_height(mkTrack);
                    track.setHeight(height);
                    int fps = ZLM_API.mk_track_video_fps(mkTrack);
                    track.setFps(fps);
                    int gop_size = ZLM_API.mk_track_video_gop_size(mkTrack);
                    track.setGop_size(gop_size);
                    int gop_interval_ms = ZLM_API.mk_track_video_gop_interval_ms(mkTrack);
                    track.setGop_interval_ms(gop_interval_ms);
                    long key_frames = ZLM_API.mk_track_video_key_frames(mkTrack);
                    track.setKey_frames(key_frames);
                } else {
                    int sample_rate = ZLM_API.mk_track_audio_sample_rate(mkTrack);
                    int audio_channel = ZLM_API.mk_track_audio_channel(mkTrack);
                    int audio_sample_bit = ZLM_API.mk_track_audio_sample_bit(mkTrack);
                    track.setSample_rate(sample_rate);
                    track.setAudio_channel(audio_channel);
                    track.setAudio_sample_bit(audio_sample_bit);
                }
                tracks.add(track);
                ZLM_API.mk_track_unref(mkTrack);
            }
            ZLM_API.mk_free(originTypePointer);
            ZLM_API.mk_free(originUrlPointer);
            mediaInfoResult.setTracks(tracks);
            return mediaInfoResult;
        }
        return null;
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

    @Override
    public Statistic getStatistic() {
        Statistic statistic = new Statistic();
        BlockingQueue<Boolean> queue = new ArrayBlockingQueue<>(1);
        IMKGetStatisticCallBack imkGetStatisticCallBack = new IMKGetStatisticCallBack() {
            @Override
            public void invoke(Pointer user_data, MK_INI ini) {
                String mediaSource = ZLM_API.mk_ini_get_option(ini, "object.MediaSource");
                String multiMediaSourceMuxer = ZLM_API.mk_ini_get_option(ini, "object.MultiMediaSourceMuxer");
                String tcpServer = ZLM_API.mk_ini_get_option(ini, "object.TcpServer");
                String tcpSession = ZLM_API.mk_ini_get_option(ini, "object.TcpSession");
                String udpServer = ZLM_API.mk_ini_get_option(ini, "object.UdpServer");
                String udpSession = ZLM_API.mk_ini_get_option(ini, "object.UdpSession");
                String tcpClient = ZLM_API.mk_ini_get_option(ini, "object.TcpClient");
                String socket = ZLM_API.mk_ini_get_option(ini, "object.Socket");
                String frameImp = ZLM_API.mk_ini_get_option(ini, "object.FrameImp");
                String frame = ZLM_API.mk_ini_get_option(ini, "object.Frame");
                String buffer = ZLM_API.mk_ini_get_option(ini, "object.Buffer");
                String bufferRaw = ZLM_API.mk_ini_get_option(ini, "object.BufferRaw");
                String bufferLikeString = ZLM_API.mk_ini_get_option(ini, "object.BufferLikeString");
                String bufferList = ZLM_API.mk_ini_get_option(ini, "object.BufferList");
                String rtpPacket = ZLM_API.mk_ini_get_option(ini, "object.RtpPacket");
                String rtmpPacket = ZLM_API.mk_ini_get_option(ini, "object.RtmpPacket");
                statistic.setMediaSource(Long.valueOf(mediaSource));
                statistic.setMultiMediaSourceMuxer(Long.valueOf(multiMediaSourceMuxer));
                statistic.setTcpServer(Long.valueOf(tcpServer));
                statistic.setTcpSession(Long.valueOf(tcpSession));
                statistic.setUdpServer(Long.valueOf(udpServer));
                statistic.setUdpSession(Long.valueOf(udpSession));
                statistic.setTcpClient(Long.valueOf(tcpClient));
                statistic.setSocket(Long.valueOf(socket));
                statistic.setFrameImp(Long.valueOf(frameImp));
                statistic.setFrame(Long.valueOf(frame));
                statistic.setBuffer(Long.valueOf(buffer));
                statistic.setBufferRaw(Long.valueOf(bufferRaw));
                statistic.setBufferLikeString(Long.valueOf(bufferLikeString));
                statistic.setBufferList(Long.valueOf(bufferList));
                statistic.setRtpPacket(Long.valueOf(rtpPacket));
                statistic.setRtmpPacket(Long.valueOf(rtmpPacket));
                queue.offer(true);
            }
        };
        ZLM_API.mk_get_statistic(imkGetStatisticCallBack, null, user_data -> {

        });
        try {
            queue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return statistic;
    }

    @Override
    public String getServerConfig() {
        Pointer pointer = ZLM_API.mk_ini_dump_string(MediaServerContext.MK_INI);
        String string = pointer.getString(0);
        ZLM_API.mk_free(pointer);
        return string;
    }

    @Override
    public Boolean restartServer() {
        context.stopMediaServer();
        return context.startMediaServer();
    }

    @Override
    public Integer setServerConfig(Map<String, String[]> parameterMap) {
        AtomicInteger count = new AtomicInteger();
        parameterMap.forEach((key, value) -> {
            if (value[0].matches("\\d+")) {
                ZLM_API.mk_ini_set_option_int(MediaServerContext.MK_INI, key, Integer.parseInt(value[0]));
            } else {
                ZLM_API.mk_ini_set_option(MediaServerContext.MK_INI, key, value[0]);
            }
            count.getAndIncrement();
        });
        return count.get();
    }

    @Override
    public Integer openRtpServer(OpenRtpServerParam param) {
        if (RTP_SERVER_MAP.containsKey(param.getStream())) {
            return -1;
        }
        short port = param.getPort().shortValue();
        MK_RTP_SERVER mkRtpServer = ZLM_API.mk_rtp_server_create(port, param.getTcp_mode(), param.getStream());
        if (mkRtpServer == null) {
            return -1;
        }
        short i = ZLM_API.mk_rtp_server_port(mkRtpServer);
        //监听rtp服务器断开事件
        IMKRtpServerDetachCallBack imkRtpServerDetachCallBack = user_data -> {
            RTP_SERVER_MAP.remove(param.getStream());
            RTP_SERVER_DETACH_CALL_BACK_MAP.remove(param.getStream());
        };
        ZLM_API.mk_rtp_server_set_on_detach(mkRtpServer, imkRtpServerDetachCallBack, null);
        RTP_SERVER_MAP.put(param.getStream(), mkRtpServer);
        RTP_SERVER_DETACH_CALL_BACK_MAP.put(param.getStream(), imkRtpServerDetachCallBack);
        return (int) i;
    }

    @Override
    public Integer closeRtpServer(String streamId) {
        MK_RTP_SERVER mkRtpServer = RTP_SERVER_MAP.get(streamId);
        if (mkRtpServer != null) {
            ZLM_API.mk_rtp_server_release(mkRtpServer);
            RTP_SERVER_MAP.remove(streamId);
            RTP_SERVER_DETACH_CALL_BACK_MAP.remove(streamId);
            return 1;
        }
        return 0;
    }

    @Override
    public List<RtpServerResult> listRtpServer() {
        List<RtpServerResult> rtpServerResults = new ArrayList<>();
        if (RTP_SERVER_MAP.size() > 0) {
            RTP_SERVER_MAP.forEach((key, value) -> {
                RtpServerResult rtpServerResult = new RtpServerResult();
                rtpServerResult.setPort((int) ZLM_API.mk_rtp_server_port(value));
                rtpServerResult.setStream(key);
                rtpServerResults.add(rtpServerResult);
            });
        }
        return rtpServerResults;
    }

}
