package com.ldf.media.api.model.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * MediaInfoResult
 *
 * @author lidaofu
 * @since 2023/3/30
 **/
@Data
@ApiModel(value = "GetMediaListParam对象", description = "流信息")
public class MediaInfoResult implements Serializable {

    private static final long serialVersionUID = 1;

    @ApiModelProperty(value = "app")
    private String app;

    @ApiModelProperty(value = "流id")
    private String stream;

    @ApiModelProperty(value = "本协议观看人数")
    private Integer readerCount;

    @ApiModelProperty(value = "产生源类型，包括 unknown = 0,rtmp_push=1,rtsp_push=2,rtp_push=3,pull=4,ffmpeg_pull=5,mp4_vod=6,device_chn=7")
    private Integer originType;

    @ApiModelProperty(value = "产生源的url")
    private String originUrl;

    @ApiModelProperty(value = "产生源的url的类型")
    private String originTypeStr;

    @ApiModelProperty(value = "观看总数 包括hls/rtsp/rtmp/http-flv/ws-flv")
    private Integer totalReaderCount;

    @ApiModelProperty(value = "schema")
    private String schema;

    @ApiModelProperty(value = "存活时间，单位秒")
    private Long aliveSecond;

    @ApiModelProperty(value = "数据产生速度，单位byte/s")
    private Integer  bytesSpeed;

    @ApiModelProperty(value = "GMT unix系统时间戳，单位秒")
    private Long createStamp;

    @ApiModelProperty(value = "是否录制Hls")
    private Boolean isRecordingHLS;

    @ApiModelProperty(value = "是否录制mp4")
    private Boolean isRecordingMP4;

    @ApiModelProperty(value = "虚拟地址")
    private String vhost;

    private List<Track> tracks;


}
