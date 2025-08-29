package com.ldf.media.api.model.param;

import cn.hutool.core.annotation.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

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
    private String stream;

    @NotBlank(message = "代理流地址不为空")
    @ApiModelProperty(value = "代理流地址",required = true)
    private String url;

    @ApiModelProperty(value = "rtsp拉流时，拉流方式，0：tcp，1：udp，2：组播")
    private Integer rtpType=0;

    @ApiModelProperty(value = "拉流重试次数,不传此参数或传值<=0时，则无限重试")
    private Integer retryCount=3;

    @ApiModelProperty(value = "拉流超时时间，单位秒型")
    private Integer timeoutSec;

    @ApiModelProperty(value = "开启hls转码")
    private Integer enableHls=1;

    @ApiModelProperty(value = "开启rtsp/webrtc转码")
    private Integer enableRtsp=1;

    @ApiModelProperty(value = "开启rtmp/flv转码")
    private Integer enableRtmp=1;

    @ApiModelProperty(value = "开启ts/ws转码")
    private Integer enableTs=0;

    @ApiModelProperty(value = "转协议是否开启音频")
    private Integer enableAudio=1;

    @ApiModelProperty(value = "开启转fmp4")
    private Integer enableFmp4=0;

    @ApiModelProperty(value = "开启mp4录制")
    private Integer enableMp4=0;

    @ApiModelProperty(value = "mp4录制切片大小")
    private Integer mp4MaxSecond=3600;

    @ApiModelProperty(value = "rtsp倍速")
    private BigDecimal rtspSpeed;

    @ApiModelProperty(value = "自动关流")
    private Integer autoClose = 1;
}
