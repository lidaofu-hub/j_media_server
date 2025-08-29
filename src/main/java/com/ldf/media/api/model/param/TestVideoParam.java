package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel(value = "TestVideoParam对象", description = "测试流参数")
public class TestVideoParam implements Serializable {

    private static final long serialVersionUID = 1;


    @NotBlank(message = "app不为空")
    @ApiModelProperty(value = "app", required = true)
    private String app;

    @NotBlank(message = "流id不为空")
    @ApiModelProperty(value = "流id", required = true)
    private String stream;

    @ApiModelProperty(value ="视频宽")
    private Integer width = 1920;

    @ApiModelProperty(value ="视频高")
    private Integer height = 1080;

    @ApiModelProperty(value = "帧率")
    private Integer fps =25;

    @ApiModelProperty(value = "比特率")
    private Integer bitRate=5000000;

    @ApiModelProperty(value = "自动关流")
    private Integer autoClose = 1;

    @ApiModelProperty(value = "开启hls转码")
    private Integer enableHls = 1;

    @ApiModelProperty(value = "开启rtsp/webrtc转码")
    private Integer enableRtsp = 1;

    @ApiModelProperty(value = "开启rtmp/flv转码")
    private Integer enableRtmp = 1;

    @ApiModelProperty(value = "开启ts/ws转码")
    private Integer enableTs = 0;

    @ApiModelProperty(value = "开启转fmp4")
    private Integer enableFmp4 = 0;

    @ApiModelProperty(value = "开启mp4录制")
    private Integer enableMp4 = 0;

    @ApiModelProperty(value = "mp4录制切片大小")
    private Integer mp4MaxSecond = 3600;

}
