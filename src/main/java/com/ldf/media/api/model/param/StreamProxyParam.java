package com.ldf.media.api.model.param;

import cn.hutool.core.annotation.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 拉流代理参数
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
@Data
@ApiModel(value = "StreamProxyParam对象", description = "拉流代理参数")
public class StreamProxyParam {


    @NotBlank(message = "app不为空")
    @ApiModelProperty(value = "app",required = true)
    private String app;

    @NotBlank(message = "流id不为空")
    @ApiModelProperty(value = "流id",required = true)
    private String stream_id;

    @NotBlank(message = "代理流地址不为空")
    @ApiModelProperty(value = "代理流地址",required = true)
    private String url;

    @NotNull(message = "拉流方式不为空")
    @ApiModelProperty(value = "rtsp拉流时，拉流方式，0：tcp，1：udp，2：组播",required = true)
    private Integer rtpType;

    @NotNull(message = "拉流重试次数不为空")
    @ApiModelProperty(value = "拉流重试次数,不传此参数或传值<=0时，则无限重试",required = true)
    private Integer retryCount;

    @NotNull(message = "拉流超时时间不为空")
    @ApiModelProperty(value = "拉流超时时间，单位秒，float类型",required = true)
    private Integer timeoutSec;

    @NotNull(message = "开启hls转码不为空")
    @ApiModelProperty(value = "开启hls转码",required = true)
    private Integer enableHls;

    @NotNull(message = "开启rtsp/webrtc转码不为空")
    @ApiModelProperty(value = "开启rtsp/webrtc转码",required = true)
    private Integer enableRtsp;


    @NotNull(message = "开启rtmp/flv转码不为空")
    @ApiModelProperty(value = "开启rtmp/flv转码",required = true)
    private Integer enableRtmp;

    @NotNull(message = "开启ts/ws转码不为空")
    @ApiModelProperty(value = "开启ts/ws转码",required = true)
    private Integer enableTs;

    @NotNull(message = "转协议是否开启音频不为空")
    @ApiModelProperty(value = "转协议是否开启音频",required = true)
    private Integer enableAudio;

    @NotNull(message = "开启转fmp4不为空")
    @ApiModelProperty(value = "开启转fmp4",required = true)
    private Integer enableFmp4;

    @NotNull(message = "开启mp4录制不为空")
    @ApiModelProperty(value = "开启mp4录制",required = true)
    private Integer enableMp4;

    @NotNull(message = "mp4录制切片大小不为空")
    @ApiModelProperty(value = "mp4录制切片大小",required = true)
    private Integer mp4MaxSecond;
}
