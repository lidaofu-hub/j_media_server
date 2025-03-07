package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
@Data
@ApiModel(value = "TranscodeParam对象", description = "转码参数")
public class TranscodeParam  implements Serializable {

    private static final long serialVersionUID = 1;

    @NotBlank(message = "url不为空")
    @ApiModelProperty(value = "url(rtmp协议只支持H264)",required = true)
    private String url;

    @NotBlank(message = "转码后推的app不为空")
    @ApiModelProperty(value = "转码后推的app",required = true)
    private String app;

    @ApiModelProperty(value = "是否开启音频",required = true)
    private Boolean enableAudio=true;

    @NotBlank(message = "转码后推的stream不为空")
    @ApiModelProperty(value = "转码后推的stream",required = true)
    private String stream;

    @ApiModelProperty(value = "修改分辨率宽",notes = "不需要则置为空")
    private Integer scaleWidth=0;

    @ApiModelProperty(value = "修改分辨率高",notes = "不需要则置为空")
    private Integer scaleHeight=0;
}
