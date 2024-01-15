package com.ldf.media.api.controller;

import com.ldf.media.api.model.param.*;
import com.ldf.media.api.model.result.MediaInfoResult;
import com.ldf.media.api.model.result.Result;
import com.ldf.media.api.service.IApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        Integer status = iApiService.closeStream(param);
        return new Result<>(status);
    }

    @ApiOperation(value = "关闭流")
    @GetMapping("/close_streams")
    public Result<Integer> closeStreams(@Validated CloseStreamsParam param) {
        Integer status = iApiService.closeStreams(param);
        return new Result<>(status);
    }

    @ApiOperation(value = "获取流列表")
    @GetMapping("/getMediaList")
    public Result<List<MediaInfoResult>> getMediaList(@Validated GetMediaListParam param) {
        List<MediaInfoResult> list = iApiService.getMediaList(param);
        return new Result<>(list);
    }


    @ApiOperation(value = "开始录像")
    @PostMapping("/startRecord")
    public Result<Boolean> startRecord(@RequestBody @Validated StartRecordParam param) {
        Boolean flag = iApiService.startRecord(param);
        return new Result<>(flag);
    }


    @ApiOperation(value = "停止录像")
    @PostMapping("/stopRecord")
    public Result<Boolean> stopRecord(@RequestBody @Validated StopRecordParam param) {
        Boolean flag = iApiService.stopRecord(param);
        return new Result<>(flag);
    }

    @ApiOperation(value = "是否录像")
    @GetMapping("/isRecording")
    public Result<Boolean> isRecording(@Validated RecordStatusParam param) {
        Boolean flag = iApiService.isRecording(param);
        return new Result<>(flag);
    }


}
