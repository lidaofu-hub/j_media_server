package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 关闭流请求参数
 *
 * @author lidaofu
 * @since 2023/3/30
 **/
@Data
@ApiModel(value = "CloseStreamsParam对象", description = "关闭流请求参数")
public class CloseStreamsParam implements Serializable {

    private static final long serialVersionUID = 1;

    @ApiModelProperty(value = "app",required = true)
    private String app;

    @ApiModelProperty(value = "流id",required = true)
    private String stream;

    @ApiModelProperty(value = "是否强制关闭",required = true)
    private Integer force=1;

    @ApiModelProperty(value = "流的协议",required = true)
    private String schema;


}
