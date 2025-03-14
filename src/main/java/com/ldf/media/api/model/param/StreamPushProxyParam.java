package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 推流代理参数
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
@Data
@ApiModel(value = "StreamPushProxyParam对象", description = "推流代理参数")
public class StreamPushProxyParam {


    @NotBlank(message = "app不为空")
    @ApiModelProperty(value = "app",required = true)
    private String app;

    @NotBlank(message = "流id不为空")
    @ApiModelProperty(value = "流id",required = true)
    private String stream;

    @NotBlank(message = "流的协议不为空")
    @ApiModelProperty(value = "流的协议",required = true)
    private String schema;

    @NotBlank(message = "推流代理流地址不为空")
    @ApiModelProperty(value = "推流代理流地址",required = true)
    private String url;

    @ApiModelProperty(value = "rtsp推流时，推流方式，0：tcp，1：udp，2：组播")
    private Integer rtpType=0;

    @ApiModelProperty(value = "推流代理超时时间，单位秒")
    private Integer timeoutSec;
}
