package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

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
@ApiModel(value = "CloseStreamParam对象", description = "关闭流请求参数")
public class CloseStreamParam  implements Serializable {

    private static final long serialVersionUID = 1;

    @NotBlank(message = "app不为空")
    @ApiModelProperty(value = "app",required = true)
    private String app;

    @NotBlank(message = "流id不为空")
    @ApiModelProperty(value = "流id",required = true)
    private String stream_id;

    @NotNull(message = "是否强制关闭不为空")
    @ApiModelProperty(value = "是否强制关闭",required = true)
    private Integer force;

    @NotBlank(message = "流的协议不为空")
    @ApiModelProperty(value = "流的协议",required = true)
    private String schema;


}
