package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel(value = "CloseTestVideoParam对象", description = "关闭测试流参数")
public class CloseTestVideoParam implements Serializable {

    private static final long serialVersionUID = 1;


    @NotBlank(message = "app不为空")
    @ApiModelProperty(value = "app", required = true)
    private String app;

    @NotBlank(message = "流id不为空")
    @ApiModelProperty(value = "流id", required = true)
    private String stream;

}
