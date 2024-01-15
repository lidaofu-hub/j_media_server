package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 开始录像
 *
 * @author lidaofu
 * @since 2023/3/30
 **/
@Data
@ApiModel(value = "StartRecordParam对象", description = "开始录像参数")
public class StartRecordParam implements Serializable {

    private static final long serialVersionUID = 1;

    @NotBlank(message = "app不为空")
    @ApiModelProperty(value = "app",required = true)
    private String app;

    @NotBlank(message = "流id不为空")
    @ApiModelProperty(value = "流id",required = true)
    private String stream;

    @NotNull(message = "录像类型不为空")
    @ApiModelProperty(value = "0为hls，1为mp4,2:hls-fmp4,3:http-fmp4,4:http-ts 当0时需要开启配置分片持久化",required = true)
    private Integer type;

    @ApiModelProperty(value = "录像保存目录")
    private String customized_path;

    @ApiModelProperty(value = "mp4录像切片时间大小,单位秒，置0则采用配置项")
    private Long max_second=1L;
}
