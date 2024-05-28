package com.ldf.media.api.model.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * rtp服务
 *
 * @author lidaofu
 * @since 2023/3/30
 **/
@Data
@ApiModel(value = "RtpServerResult对象", description = "rtp服务")
public class RtpServerResult implements Serializable {

    private static final long serialVersionUID = 1;


    @NotNull(message = "接收端口，0则为随机端口")
    @ApiModelProperty(value = "接收端口",required = true)
    private Integer port;

    @NotBlank(message = "流id不为空")
    @ApiModelProperty(value = "流id",required = true)
    private String stream;


}
