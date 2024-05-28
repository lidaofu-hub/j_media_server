package com.ldf.media.api.controller;

import com.ldf.media.api.model.param.*;
import com.ldf.media.api.model.result.MediaInfoResult;
import com.ldf.media.api.model.result.Result;
import com.ldf.media.api.model.result.RtpServerResult;
import com.ldf.media.api.model.result.Statistic;
import com.ldf.media.api.service.IApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * Api接口
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
@Api(tags = "【API】流媒体Api")
@RequestMapping("/index/api")
@RestController
@Validated
@RequiredArgsConstructor
public class ApiController {
    private final IApiService iApiService;

    @ApiOperation(value = "添加rtmp/rtsp拉流代理", notes = "此接口不会返回具体流地址，请按照流地址生成规则结合自己网络信息来拼接具体地址")
    @RequestMapping(value = "/addStreamProxy", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<String> addStreamProxy(@Validated StreamProxyParam param) {
        iApiService.addStreamProxy(param);
        return new Result<>();
    }

    @ApiOperation(value = "关闭流")
    @RequestMapping(value = "/close_stream", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<Integer> closeStream(@Validated CloseStreamParam param) {
        Integer status = iApiService.closeStream(param);
        return new Result<>(status);
    }

    @ApiOperation(value = "关闭流(批量关)")
    @RequestMapping(value = "/close_streams", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<Integer> closeStreams(@Validated CloseStreamsParam param) {
        Integer status = iApiService.closeStreams(param);
        return new Result<>(status);
    }

    @ApiOperation(value = "获取流列表")
    @RequestMapping(value = "/getMediaList", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<List<MediaInfoResult>> getMediaList(GetMediaListParam param) {
        List<MediaInfoResult> list = iApiService.getMediaList(param);
        return new Result<>(list);
    }

    @ApiOperation(value = "获取流信息")
    @RequestMapping(value = "/getMediaInfo", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<MediaInfoResult> getMediaInfo(@Validated MediaQueryParam param) {
        MediaInfoResult info = iApiService.getMediaInfo(param);
        return new Result<>(info);
    }


    @ApiOperation(value = "流是否在线")
    @RequestMapping(value = "/isMediaOnline", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<Boolean> isMediaOnline(@Validated MediaQueryParam param) {
        Boolean online = iApiService.isMediaOnline(param);
        return new Result<>(online);
    }


    @ApiOperation(value = "开始录像")
    @RequestMapping(value = "/startRecord", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<Boolean> startRecord(@Validated StartRecordParam param) {
        Boolean flag = iApiService.startRecord(param);
        return new Result<>(flag);
    }


    @ApiOperation(value = "停止录像")
    @RequestMapping(value = "/stopRecord", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<Boolean> stopRecord(@Validated StopRecordParam param) {
        Boolean flag = iApiService.stopRecord(param);
        return new Result<>(flag);
    }

    @ApiOperation(value = "是否录像")
    @RequestMapping(value = "/isRecording", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<Boolean> isRecording(@Validated RecordStatusParam param) {
        Boolean flag = iApiService.isRecording(param);
        return new Result<>(flag);
    }

    @ApiOperation(value = "获取内存资源信息")
    @RequestMapping(value = "/getStatistic", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<Statistic> getStatistic() {
        Statistic statistic = iApiService.getStatistic();
        return new Result<>(statistic);
    }


    @ApiOperation(value = "获取服务器配置")
    @RequestMapping(value = "/getServerConfig", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<String> getServerConfig() {
        String confStr = iApiService.getServerConfig();
        return new Result<>(confStr);
    }

    @ApiOperation(value = "重启流媒体服务")
    @RequestMapping(value = "/restartServer", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<Boolean> restartServer() {
        Boolean status = iApiService.restartServer();
        return new Result<>(status);
    }

    @ApiOperation(value = "设置服务器配置")
    @RequestMapping(value = "/setServerConfig", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<Integer> setServerConfig(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Integer size = iApiService.setServerConfig(parameterMap);
        return new Result<>(size);
    }


    @ApiOperation(value = "开启rtp服务")
    @RequestMapping(value = "/openRtpServer", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<Integer> openRtpServer(@Validated OpenRtpServerParam param) {
        Integer port = iApiService.openRtpServer(param);
        return new Result<>(port);
    }

    @ApiOperation(value = "关闭rtp服务")
    @ApiImplicitParam(name = "stream_id", value = "流id", required = true)
    @RequestMapping(value = "/closeRtpServer", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<Integer> closeRtpServer(@NotBlank(message = "流id不为空")@RequestParam(value = "stream_id") String stream_id) {
        Integer status = iApiService.closeRtpServer(stream_id);
        return new Result<>(status);
    }

    @ApiOperation(value = "获取所有RTP服务器")
    @RequestMapping(value = "/listRtpServer", method = {RequestMethod.POST, RequestMethod.GET})
    public Result<List<RtpServerResult>> listRtpServer() {
        List<RtpServerResult> results = iApiService.listRtpServer();
        return new Result<>(results);
    }
}
