package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 流查询参数
 *
 * @author lidaofu
 * @since 2023/3/30
 **/
@Data
@ApiModel(value = "MediaQueryParam对象", description = "流查询参数")
public class MediaQueryParam implements Serializable {

    private static final long serialVersionUID = 1;


    @NotBlank(message = "app不为空")
    @ApiModelProperty(value = "app",required = true)
    private String app;

    @NotBlank(message = "流id不为空")
    @ApiModelProperty(value = "流id",required = true)
    private String stream_id;

    @NotBlank(message = "流的协议不为空")
    @ApiModelProperty(value = "流的协议",required = true)
    private String schema;

}
