package com.ldf.media.api.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "VideoStackWindowParam对象", description = "拼接屏幕地址参数")
public class VideoStackWindowParam implements Serializable {

    private static final long serialVersionUID = 1;

    @ApiModelProperty(value = "拼接视频地址")
    private String videoUrl;

    @ApiModelProperty(value = "拼接图片地址,和上面二选一")
    private String imgUrl;

    @ApiModelProperty(value = "默认填充颜色")
    private String fillColor = "BFBFBF";

    @ApiModelProperty(value = "所占的格子")
    private List<Integer> span;
}