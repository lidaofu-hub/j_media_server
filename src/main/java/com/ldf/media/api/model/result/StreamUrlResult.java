package com.ldf.media.api.model.result;

import cn.hutool.core.util.StrUtil;
import com.ldf.media.api.model.param.StreamProxyParam;
import com.ldf.media.api.model.param.TestVideoParam;
import com.ldf.media.config.MediaServerConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class StreamUrlResult implements Serializable {
    private static final long serialVersionUID = 1;

    @ApiModelProperty(value = "拉流代理key")
    private String key;

    @ApiModelProperty(value = "app")
    private String app;

    @ApiModelProperty(value = "流id")
    private String stream;

    @ApiModelProperty("hlsUrl播放地址")
    private String hlsUrl;

    @ApiModelProperty("rtspUrl播放地址")
    private String rtspUrl;

    @ApiModelProperty("rtmpUrl播放地址")
    private String rtmpUrl;

    @ApiModelProperty("wsflv播放地址")
    private String wsFlvUrl;

    @ApiModelProperty("httpflv播放地址")
    private String httpFlvUrl;

    @ApiModelProperty("httpFmp4Url播放地址")
    private String httpFmp4Url;

    @ApiModelProperty("wsFmp4Url播放地址")
    private String wsFmp4Url;

    @ApiModelProperty("httpTsUrl播放地址")
    private String httpTsUrl;

    @ApiModelProperty("wsTsUrl播放地址")
    private String wsTsUrl;

    public StreamUrlResult(MediaServerConfig config, StreamProxyParam param, String key) {
        this.app = param.getApp();
        this.key = key;
        this.stream = param.getStream();
        if (param.getEnableRtmp() == 1) {
            this.wsFlvUrl = StrUtil.format("ws://{}:{}/{}/{}.live.flv", config.getMedia_ip(), config.getHttp_port(), app, stream);
            this.httpFlvUrl = StrUtil.format("http://{}:{}/{}/{}.live.flv", config.getMedia_ip(), config.getHttp_port(), app, stream);
            if (config.getRtmp_port() == 1935) {
                this.rtmpUrl = StrUtil.format("rtmp://{}/{}/{}", config.getMedia_ip(), app, stream);
            } else {
                this.rtmpUrl = StrUtil.format("rtmp://{}:{}/{}/{}", config.getMedia_ip(), config.getRtmp_port(), app, stream);
            }
        }
        if (param.getEnableHls() == 1) {
            this.hlsUrl = StrUtil.format("http://{}:{}/{}/{}/hls.m3u8", config.getMedia_ip(), config.getHttp_port(), app, stream);
        }
        if (param.getEnableRtsp() == 1) {
            if (config.getRtsp_port() == 554) {
                this.rtspUrl = StrUtil.format("rtsp://{}/{}/{}", config.getMedia_ip(), app, stream);
            } else {
                this.rtspUrl = StrUtil.format("rtsp://{}:{}/{}/{}", config.getMedia_ip(), config.getRtsp_port(), app, stream);
            }
        }
        if (param.getEnableFmp4() == 1) {
            this.wsFmp4Url = StrUtil.format("ws://{}:{}/{}/{}.live.mp4", config.getMedia_ip(), config.getHttp_port(), app, stream);
            this.httpFmp4Url = StrUtil.format("http://{}:{}/{}/{}.live.mp4", config.getMedia_ip(), config.getHttp_port(), app, stream);
        }

        if (param.getEnableTs() == 1) {
            this.wsTsUrl = StrUtil.format("ws://{}:{}/{}/{}.live.ts", config.getMedia_ip(), config.getHttp_port(), app, stream);
            this.httpTsUrl = StrUtil.format("http://{}:{}/{}/{}.live.ts", config.getMedia_ip(), config.getHttp_port(), app, stream);
        }

    }

    public StreamUrlResult(MediaServerConfig config, TestVideoParam param) {
        this.app = param.getApp();
        this.stream = param.getStream();
        if (param.getEnableRtmp() == 1) {
            this.wsFlvUrl = StrUtil.format("ws://{}:{}/{}/{}.live.flv", config.getMedia_ip(), config.getHttp_port(), app, stream);
            this.httpFlvUrl = StrUtil.format("http://{}:{}/{}/{}.live.flv", config.getMedia_ip(), config.getHttp_port(), app, stream);
            if (config.getRtmp_port() == 1935) {
                this.rtmpUrl = StrUtil.format("rtmp://{}/{}/{}", config.getMedia_ip(), app, stream);
            } else {
                this.rtmpUrl = StrUtil.format("rtmp://{}:{}/{}/{}", config.getMedia_ip(), config.getRtmp_port(), app, stream);
            }
        }
        if (param.getEnableHls() == 1) {
            this.hlsUrl = StrUtil.format("http://{}:{}/{}/{}/hls.m3u8", config.getMedia_ip(), config.getHttp_port(), app, stream);
        }
        if (param.getEnableRtsp() == 1) {
            if (config.getRtsp_port() == 554) {
                this.rtspUrl = StrUtil.format("rtsp://{}/{}/{}", config.getMedia_ip(), app, stream);
            } else {
                this.rtspUrl = StrUtil.format("rtsp://{}:{}/{}/{}", config.getMedia_ip(), config.getRtsp_port(), app, stream);
            }
        }
        if (param.getEnableFmp4() == 1) {
            this.wsFmp4Url = StrUtil.format("ws://{}:{}/{}/{}.live.mp4", config.getMedia_ip(), config.getHttp_port(), app, stream);
            this.httpFmp4Url = StrUtil.format("http://{}:{}/{}/{}.live.mp4", config.getMedia_ip(), config.getHttp_port(), app, stream);
        }

        if (param.getEnableTs() == 1) {
            this.wsTsUrl = StrUtil.format("ws://{}:{}/{}/{}.live.ts", config.getMedia_ip(), config.getHttp_port(), app, stream);
            this.httpTsUrl = StrUtil.format("http://{}:{}/{}/{}.live.ts", config.getMedia_ip(), config.getHttp_port(), app, stream);
        }

    }

    public StreamUrlResult() {
    }
}
