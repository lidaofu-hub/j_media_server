package com.ldf.media.api.controller;

import com.ldf.media.api.model.param.CloseStreamParam;
import com.ldf.media.api.model.param.CloseStreamsParam;
import com.ldf.media.api.model.param.GetMediaListParam;
import com.ldf.media.api.model.result.MediaInfoResult;
import com.ldf.media.api.model.result.Result;
import com.ldf.media.api.model.param.StreamProxyParam;
import com.ldf.media.api.service.IApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Api接口
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
@Api(tags = "【API】流媒体Api")
@RequestMapping("/index/api")
@RestController
@RequiredArgsConstructor
public class ApiController {
    private final IApiService iApiService;

    @ApiOperation(value = "添加rtmp/rtsp拉流代理")
    @GetMapping("/addStreamProxy")
    public Result<String> addStreamProxy(@Validated StreamProxyParam param) {
        iApiService.addStreamProxy(param);
        return new Result<>();
    }

    @ApiOperation(value = "关闭流")
    @GetMapping("/close_stream")
    public Result<Integer> closeStream(@Validated CloseStreamParam param) {
        Integer status= iApiService.closeStream(param);
        return new Result<>(status);
    }

    @ApiOperation(value = "关闭流")
    @GetMapping("/close_streams")
    public Result<Integer> closeStreams(@Validated CloseStreamsParam param) {
        Integer status= iApiService.closeStreams(param);
        return new Result<>(status);
    }

    @ApiOperation(value = "获取流列表")
    @GetMapping("/getMediaList")
    public Result<List<MediaInfoResult>> getMediaList(@Validated GetMediaListParam param) {
        List<MediaInfoResult> list= iApiService.getMediaList(param);
        return new Result<>(list);
    }
}
