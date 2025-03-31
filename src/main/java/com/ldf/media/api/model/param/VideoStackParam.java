package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "VideoStackParam对象", description = "拼接屏幕参数")
public class VideoStackParam implements Serializable {

    private static final long serialVersionUID = 1;

    @NotBlank(message = "拼接屏任务id不为空")
    @ApiModelProperty(value = "拼接屏任务id(流id)", required = true)
    private String id;

    @ApiModelProperty(value = "拼接屏任务流app")
    private String app = "live";

    @NotNull(message = "拼接屏行数不为空")
    @ApiModelProperty(value = "拼接屏行数", required = true)
    private Integer row;

    @NotNull(message = "拼接屏列行数不为空")
    @ApiModelProperty(value = "拼接屏列行数", required = true)
    private Integer col;

    @NotNull(message = "拼接屏宽度不为空")
    @ApiModelProperty(value = "拼接屏宽度", required = true)
    private Integer width;

    @NotNull(message = "拼接屏高度不为空")
    @ApiModelProperty(value = "拼接屏高度", required = true)
    private Integer height;

    @ApiModelProperty(value = "图片链接，为空则填灰色")
    private String fillImgUrl;

    @ApiModelProperty(value = "默认填充颜色")
    private String fillColor = "BFBFBF";

    @ApiModelProperty(value = "是否存在分割线")
    private Boolean gridLineEnable = false;

    @ApiModelProperty(value = "分割线颜色")
    private String gridLineColor = "000000";

    @ApiModelProperty(value = "分割线宽度")
    private Integer gridLineWidth = 1;

//    @ApiModelProperty(value = "解码器名称(因驱动问题暂不实现)",notes = "不填则使用cpu解码 确保驱动存在例如 h264_qsv、 h264_cuvid、hevc_qsv、 hevc_cuvid、hevc_amf ")
//    private String deCodecName;

    @ApiModelProperty(value = "拼接屏内容")
    private List<VideoStackWindowParam> windowList;

}