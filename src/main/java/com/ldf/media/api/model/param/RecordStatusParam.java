package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 录像状态
 *
 * @author lidaofu
 * @since 2023/3/30
 **/
@Data
@ApiModel(value = "RecordStatusParam对象", description = "录像状态")
public class RecordStatusParam implements Serializable {

    private static final long serialVersionUID = 1;

    @NotBlank(message = "app不为空")
    @ApiModelProperty(value = "app",required = true)
    private String app;

    @NotBlank(message = "流id不为空")
    @ApiModelProperty(value = "流id",required = true)
    private String stream_id;

    @NotNull(message = "录像类型不为空")
    @ApiModelProperty(value = "0为hls，1为mp4,2:hls-fmp4,3:http-fmp4,4:http-ts 当0时需要开启配置分片持久化",required = true)
    private Integer type;

}
