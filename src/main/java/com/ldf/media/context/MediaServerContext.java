package com.ldf.media.context;

import com.aizuda.zlm4j.callback.*;
import com.aizuda.zlm4j.core.ZLMApi;
import com.aizuda.zlm4j.structure.MK_EVENTS;
import com.aizuda.zlm4j.structure.MK_INI;
import com.ldf.media.config.MediaServerConfig;
import com.sun.jna.Native;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 流媒体上下文
 *
 * @author lidaofu
 * @since 2023/11/28
 **/
@Slf4j
@Component
public class MediaServerContext {
    @Autowired
    private MediaServerConfig config;
    @Autowired
    private IMKStreamChangeCallBack iMKStreamChangeCallBack;
    @Autowired
    private IMKNoReaderCallBack iMKNoReaderCallBack;
    @Autowired
    private IMKPublishCallBack iMKPublishCallBack;
    @Autowired
    private IMKNoFoundCallBack imKNoFoundCallBack;
    @Autowired
    private IMKFlowReportCallBack iMKFlowReportCallBack;
    @Autowired
    private IMKPlayCallBack iMKPlayCallBack;
    @Autowired
    private IMKRecordMp4CallBack iMKRecordMp4CallBack;
    @Autowired
    private IMKRecordTsCallBack iMKRecordTsCallBack;
    @Autowired
    private IMKLogCallBack iMKLogCallBack;
    public static ZLMApi ZLM_API = null;
    public static MK_EVENTS MK_EVENT = null;
    public static MK_INI MK_INI = null;

    @PostConstruct
    public void initMediaServer() {
        ZLM_API = Native.load("mk_api", ZLMApi.class);
        this.initMediaServerConf();
        this.initEvents();
        this.startMediaServer();
        log.info("【MediaServer】初始化MediaServer程序成功");
    }


    /**
     * 初始化服务器配置
     */
    public void initMediaServerConf() {
        //初始化环境配置
        MK_INI = ZLM_API.mk_ini_default();
        ZLM_API.mk_ini_set_option(MK_INI, "general.mediaServerId", "JMediaServer");
        ZLM_API.mk_ini_set_option(MK_INI, "http.notFound", "<h1 style=\"text-align:center;\">Media Server V1.0 By LiDaoFu</h1>");
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.auto_close", config.getAuto_close());
        ZLM_API.mk_ini_set_option_int(MK_INI, "general.streamNoneReaderDelayMS", config.getStreamNoneReaderDelayMS());
        ZLM_API.mk_ini_set_option_int(MK_INI, "general.maxStreamWaitMS", config.getMaxStreamWaitMS());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.enable_ts", config.getEnable_ts());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.enable_hls", config.getEnable_hls());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.enable_fmp4", config.getEnable_fmp4());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.enable_rtsp", config.getEnable_rtsp());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.enable_rtmp", config.getEnable_rtmp());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.enable_mp4", config.getEnable_mp4());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.enable_hls_fmp4", config.getEnable_hls_fmp4());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.enable_audio", config.getEnable_audio());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.mp4_as_player", config.getMp4_as_player());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.mp4_max_second", config.getMp4_max_second());
        ZLM_API.mk_ini_set_option(MK_INI, "http.rootPath", config.getRootPath());
        ZLM_API.mk_ini_set_option(MK_INI, "protocol.mp4_save_path", config.getMp4_save_path());
        ZLM_API.mk_ini_set_option(MK_INI, "protocol.hls_save_path", config.getHls_save_path());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.hls_demand", config.getHls_demand());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.rtsp_demand", config.getRtsp_demand());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.rtmp_demand", config.getRtmp_demand());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.ts_demand", config.getTs_demand());
        ZLM_API.mk_ini_set_option_int(MK_INI, "protocol.fmp4_demand", config.getFmp4_demand());
        ZLM_API.mk_ini_set_option_int(MK_INI, "hls.broadcastRecordTs", config.getBroadcastRecordTs());
        ZLM_API.mk_ini_set_option_int(MK_INI, "hls.segNum", config.getSegNum());
        ZLM_API.mk_ini_set_option_int(MK_INI, "hls.segDur", config.getSegDur());
        ZLM_API.mk_ini_set_option(MK_INI, "rtc.externIP", config.getRtc_host());
        //初始化zmk服务器
        ZLM_API.mk_env_init1(config.getThread_num(), config.getLog_level(), config.getLog_mask(), config.getLog_path(), config.getLog_file_days(), 0, null, 0, null, null);
    }

    /**
     * 初始化事件回调
     */
    public void initEvents() {
        //全局回调
        MK_EVENT = new MK_EVENTS();
        MK_EVENT.on_mk_media_changed = iMKStreamChangeCallBack;
        MK_EVENT.on_mk_media_no_reader = iMKNoReaderCallBack;
        MK_EVENT.on_mk_media_publish = iMKPublishCallBack;
        MK_EVENT.on_mk_media_not_found = imKNoFoundCallBack;
        MK_EVENT.on_mk_flow_report = iMKFlowReportCallBack;
        MK_EVENT.on_mk_media_play = iMKPlayCallBack;
 /*       MK_EVENT.on_mk_http_access = new MKHttpAccessCallBack();
        MK_EVENT.on_mk_http_request = new MKHttpRequestCallBack();
        MK_EVENT.on_mk_http_access = new MKHttpAccessCallBack();
        MK_EVENT.on_mk_http_before_access = new MKHttpBeforeAccessCallBack();*/
        MK_EVENT.on_mk_record_mp4 = iMKRecordMp4CallBack;
        MK_EVENT.on_mk_record_ts = iMKRecordTsCallBack;
        MK_EVENT.on_mk_log = iMKLogCallBack;
        //添加全局回调
        ZLM_API.mk_events_listen(MK_EVENT);
    }

    /**
     * 启动流媒体服务器
     */
    public boolean startMediaServer() {
        boolean result = false;
        //创建http服务器 0:失败,非0:端口号
        short http_server_port = ZLM_API.mk_http_server_start(config.getHttp_port().shortValue(), 0);
        result=http_server_port == 0;
        log.info("【MediaServer】HTTP流媒体服务启动：{}", result ? "失败" : "成功，端口：" + http_server_port);
        //创建rtsp服务器 0:失败,非0:端口号
        short rtsp_server_port = ZLM_API.mk_rtsp_server_start(config.getRtsp_port().shortValue(), 0);
        result=rtsp_server_port == 0;
        log.info("【MediaServer】RTSP流媒体服务启动：{}", result ? "失败" : "成功，端口：" + rtsp_server_port);
        //创建rtmp服务器 0:失败,非0:端口号
        short rtmp_server_port = ZLM_API.mk_rtmp_server_start(config.getRtmp_port().shortValue(), 0);
        result=rtmp_server_port == 0;
        log.info("【MediaServer】RTMP流媒体服务启动：{}",result ? "失败" : "成功，端口：" + rtmp_server_port);
        //创建rtc服务器 0:失败,非0:端口号
        short rtc_server_port = ZLM_API.mk_rtc_server_start(config.getRtc_port().shortValue());
        result=rtc_server_port == 0;
        log.info("【MediaServer】RTC流媒体服务启动：{}", result ? "失败" : "成功，端口：" + rtc_server_port);
        return !result;
    }

    /**
     * 关闭流媒体服务器
     */
    public void stopMediaServer() {
        ZLM_API.mk_stop_all_server();
        log.info("【MediaServer】关闭所有流媒体服务");
    }

    /**
     * 释放资源
     */
    @PreDestroy
    public void release() {
        this.stopMediaServer();
    }


}
