package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 获取流列表
 *
 * @author lidaofu
 * @since 2023/3/30
 **/
@Data
@ApiModel(value = "GetMediaListParam对象", description = "获取流列表")
public class GetMediaListParam implements Serializable {

    private static final long serialVersionUID = 1;


    @ApiModelProperty(value = "app",required = true)
    private String app;

    @ApiModelProperty(value = "流id",required = true)
    private String stream;

    @ApiModelProperty(value = "流的协议",required = true)
    private String schema;

}
