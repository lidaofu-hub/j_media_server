package com.ldf.media.context;

import com.ldf.media.callback.*;
import com.ldf.media.config.MediaServerConfig;
import com.aizuda.core.ZLMApi;
import com.aizuda.structure.MK_EVENTS;
import com.aizuda.structure.MK_INI;
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
    public static ZLMApi ZLM_API = null;
    private static MK_EVENTS MK_EVENTS = null;

    @PostConstruct
    public void initMediaServer() {
        ZLM_API = Native.load("mk_api", ZLMApi.class);
        log.info("【MediaServer】初始化MediaServer程序成功");
        this.initServerConf();
    }


    public void initServerConf() {
        //初始化环境配置
        MK_INI mkIni = ZLM_API.mk_ini_default();
        ZLM_API.mk_ini_set_option(mkIni, "general.mediaServerId", "JMediaServer");
        ZLM_API.mk_ini_set_option(mkIni, "http.notFound", "<h1 style=\"text-align:center;\">Media Server V1.0 By LiDaoFu</h1>");
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.auto_close", config.getAuto_close());
        ZLM_API.mk_ini_set_option_int(mkIni, "general.streamNoneReaderDelayMS", config.getStreamNoneReaderDelayMS());
        ZLM_API.mk_ini_set_option_int(mkIni, "general.maxStreamWaitMS", config.getMaxStreamWaitMS());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.enable_ts", config.getEnable_ts());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.enable_hls", config.getEnable_hls());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.enable_fmp4", config.getEnable_fmp4());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.enable_rtsp", config.getEnable_rtsp());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.enable_rtmp", config.getEnable_rtmp());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.enable_mp4", config.getEnable_mp4());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.enable_hls_fmp4", config.getEnable_hls_fmp4());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.enable_audio", config.getEnable_audio());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.mp4_as_player", config.getMp4_as_player());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.mp4_max_second", config.getMp4_max_second());
        ZLM_API.mk_ini_set_option(mkIni, "http.rootPath", config.getRootPath());
        ZLM_API.mk_ini_set_option(mkIni, "protocol.mp4_save_path", config.getMp4_save_path());
        ZLM_API.mk_ini_set_option(mkIni, "protocol.hls_save_path", config.getHls_save_path());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.hls_demand", config.getHls_demand());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.rtsp_demand", config.getRtsp_demand());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.rtmp_demand", config.getRtmp_demand());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.ts_demand", config.getTs_demand());
        ZLM_API.mk_ini_set_option_int(mkIni, "protocol.fmp4_demand", config.getFmp4_demand());
        //全局回调
        MK_EVENTS = new MK_EVENTS();
        MK_EVENTS.on_mk_media_changed = new MKStreamChangeCallBack();
        MK_EVENTS.on_mk_media_no_reader = new MKNoReaderCallBack();
        MK_EVENTS.on_mk_media_publish = new MKPublishCallBack();
        MK_EVENTS.on_mk_media_not_found = new MKNoFoundCallBack();
        MK_EVENTS.on_mk_flow_report = new MKHttpFlowReportCallBack();
        MK_EVENTS.on_mk_media_play = new MKPlayCallBack();
 /*       MK_EVENTS.on_mk_http_access = new MKHttpAccessCallBack();
        MK_EVENTS.on_mk_http_request = new MKHttpRequestCallBack();
        MK_EVENTS.on_mk_http_access = new MKHttpAccessCallBack();
        MK_EVENTS.on_mk_http_before_access = new MKHttpBeforeAccessCallBack();*/
        MK_EVENTS.on_mk_record_mp4 = new MKRecordMp4CallBack();
        MK_EVENTS.on_mk_log = new MKLogCallBack();
        //添加全局回调
        ZLM_API.mk_events_listen(MK_EVENTS);
        //初始化zmk服务器
        ZLM_API.mk_env_init1(config.getThread_num(), config.getLog_level(), config.getLog_mask(), config.getLog_path(), config.getLog_file_days(), 0, null, 0, null, null);
        //创建http服务器 0:失败,非0:端口号
        short http_server_port = ZLM_API.mk_http_server_start(config.getHttp_port().shortValue(), 0);
        log.info("【MediaServer】HTTP流媒体服务启动：{}", http_server_port == 0 ? "失败" : "成功，端口：" + http_server_port);
        //创建rtsp服务器 0:失败,非0:端口号
        short rtsp_server_port = ZLM_API.mk_rtsp_server_start(config.getRtsp_port().shortValue(), 0);
        log.info("【MediaServer】RTSP流媒体服务启动：{}", rtsp_server_port == 0 ? "失败" : "成功，端口：" + rtsp_server_port);
        //创建rtmp服务器 0:失败,非0:端口号
        short rtmp_server_port = ZLM_API.mk_rtmp_server_start(config.getRtmp_port().shortValue(), 0);
        log.info("【MediaServer】RTMP流媒体服务启动：{}", rtmp_server_port == 0 ? "失败" : "成功，端口：" + rtmp_server_port);
    }

    @PreDestroy
    public void release() {
        ZLM_API.mk_stop_all_server();
        log.info("【MediaServer】关闭所有流媒体服务");
    }
}
