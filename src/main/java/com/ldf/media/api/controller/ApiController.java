package com.ldf.media.api.controller;

import com.ldf.media.api.model.param.*;
import com.ldf.media.api.model.result.MediaInfoResult;
import com.ldf.media.api.model.result.Result;
import com.ldf.media.api.model.result.RtpServerResult;
import com.ldf.media.api.model.result.Statistic;
import com.ldf.media.api.service.IApiService;
import com.ldf.media.api.service.ISnapService;
import com.ldf.media.api.service.ITranscodeService;
import com.ldf.media.api.service.IVideoStackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private final ISnapService iSnapService;
    private final ITranscodeService iTranscodeService;
    private final IVideoStackService iVideoStackService;

    @ApiOperation(value = "【拉流代理】添加rtmp/rtsp拉流代理", notes = "此接口不会返回具体流地址，请按照流地址生成规则结合自己网络信息来拼接具体地址")
    @PostMapping(value = "/addStreamProxy")
    public Result<String> addStreamProxy(@Validated @RequestBody StreamProxyParam param) {
        String error = iApiService.addStreamProxy(param);
        return new Result<>(error);
    }

    @ApiOperation(value = "【拉流代理】关闭拉流代理", notes = "流注册成功后，也可以使用close_streams接口替代")
    @PostMapping(value = "/delStreamProxy" )
    public Result<Boolean> delStreamProxy(String key) {
        Boolean flag = iApiService.delStreamProxy(key);
        return new Result<>(flag);
    }

    @ApiOperation(value = "【推流代理】添加rtmp/rtsp推流代理")
    @PostMapping(value = "/addStreamPusherProxy")
    public Result<String> addStreamPusherProxy(@Validated @RequestBody StreamPushProxyParam param) {
        String error = iApiService.addStreamPusherProxy(param);
        return new Result<>(error);
    }


    @ApiOperation(value = "【推流代理】删除rtmp/rtsp推流代理")
    @PostMapping(value = "/delStreamPusherProxy")
    public Result<Boolean> delStreamPusherProxy(String key) {
        Boolean flag = iApiService.delStreamPusherProxy(key);
        return new Result<>(flag);
    }


    @ApiOperation(value = "【流操作】关闭流")
    @PostMapping(value = "/close_stream")
    public Result<Integer> closeStream(@Validated @RequestBody CloseStreamParam param) {
        Integer status = iApiService.closeStream(param);
        return new Result<>(status);
    }

    @ApiOperation(value = "【流操作】关闭流(批量关)")
    @PostMapping(value = "/close_streams")
    public Result<Integer> closeStreams(@Validated @RequestBody CloseStreamsParam param) {
        Integer status = iApiService.closeStreams(param);
        return new Result<>(status);
    }

    @ApiOperation(value = "【流操作】获取流列表")
    @GetMapping(value = "/getMediaList")
    public Result<List<MediaInfoResult>> getMediaList(GetMediaListParam param) {
        List<MediaInfoResult> list = iApiService.getMediaList(param);
        return new Result<>(list);
    }

    @ApiOperation(value = "【流操作】获取流信息")
    @GetMapping(value = "/getMediaInfo")
    public Result<MediaInfoResult> getMediaInfo(@Validated MediaQueryParam param) {
        MediaInfoResult info = iApiService.getMediaInfo(param);
        return new Result<>(info);
    }


    @ApiOperation(value = "【流操作】流是否在线")
    @GetMapping(value = "/isMediaOnline")
    public Result<Boolean> isMediaOnline(@Validated MediaQueryParam param) {
        Boolean online = iApiService.isMediaOnline(param);
        return new Result<>(online);
    }


    @ApiOperation(value = "【录像】开始录像")
    @PostMapping(value = "/startRecord")
    public Result<Boolean> startRecord(@Validated @RequestBody StartRecordParam param) {
        Boolean flag = iApiService.startRecord(param);
        return new Result<>(flag);
    }


    @ApiOperation(value = "【录像】停止录像")
    @PostMapping(value = "/stopRecord" )
    public Result<Boolean> stopRecord(@Validated @RequestBody StopRecordParam param) {
        Boolean flag = iApiService.stopRecord(param);
        return new Result<>(flag);
    }

    @ApiOperation(value = "【录像】是否录像")
    @GetMapping(value = "/isRecording" )
    public Result<Boolean> isRecording(@Validated RecordStatusParam param) {
        Boolean flag = iApiService.isRecording(param);
        return new Result<>(flag);
    }

    @ApiOperation(value = "【系统】获取内存资源信息")
    @GetMapping(value = "/getStatistic")
    public Result<Statistic> getStatistic() {
        Statistic statistic = iApiService.getStatistic();
        return new Result<>(statistic);
    }


    @ApiOperation(value = "【系统】获取服务器配置")
    @GetMapping(value = "/getServerConfig")
    public Result<String> getServerConfig() {
        String confStr = iApiService.getServerConfig();
        return new Result<>(confStr);
    }

    @ApiOperation(value = "【系统】重启流媒体服务")
    @PostMapping(value = "/restartServer")
    public Result<Boolean> restartServer() {
        Boolean status = iApiService.restartServer();
        return new Result<>(status);
    }

    @ApiOperation(value = "【系统】设置服务器配置")
    @PostMapping(value = "/setServerConfig")
    public Result<Integer> setServerConfig(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Integer size = iApiService.setServerConfig(parameterMap);
        return new Result<>(size);
    }


    @ApiOperation(value = "【RTP服务】开启rtp服务")
    @PostMapping(value = "/openRtpServer")
    public Result<Integer> openRtpServer(@Validated @RequestBody OpenRtpServerParam param) {
        Integer port = iApiService.openRtpServer(param);
        return new Result<>(port);
    }

    @ApiOperation(value = "【RTP服务】关闭rtp服务")
    @ApiImplicitParam(name = "stream", value = "流id", required = true)
    @PostMapping(value = "/closeRtpServer")
    public Result<Integer> closeRtpServer(@NotBlank(message = "流id不为空") @RequestParam(value = "stream") String stream) {
        Integer status = iApiService.closeRtpServer(stream);
        return new Result<>(status);
    }

    @ApiOperation(value = "【RTP服务】获取所有RTP服务器")
    @GetMapping(value = "/listRtpServer")
    public Result<List<RtpServerResult>> listRtpServer() {
        List<RtpServerResult> results = iApiService.listRtpServer();
        return new Result<>(results);
    }

    @ApiOperation(value = "【截图】获取截图")
    @ApiImplicitParam(name = "url", value = "截图流地址", required = true)
    @GetMapping(value = "/getSnap")
    public void getSnap(String url, HttpServletResponse response) {
        iSnapService.getSnap(url, response);

    }

    @ApiOperation(value = "【转码】拉流代理转码(beta)", notes = "默认H265转H264 支持分辨率调整 暂时只支持视频转码，音频因为各种封装格式对编码格式、音频参数等转换规则复杂暂时不支持")
    @PostMapping(value = "/transcode")
    public Result<String> transcode(@Validated @RequestBody TranscodeParam param) {
        iTranscodeService.transcode(param);
        return new Result<>();
    }

    @ApiOperation(value = "【拼接屏】开启拼接屏(beta)")
    @PostMapping(value = "/stack/start" )
    public Result<String> startStack(@RequestBody @Validated VideoStackParam param) {
      iVideoStackService.startStack(param);
        return new Result<>();
    }

    @ApiOperation(value = "【拼接屏】重新设置拼接屏(beta)")
    @PostMapping(value = "/stack/reset" )
    public Result<String> resetStack(@RequestBody @Validated VideoStackParam param) {
        iVideoStackService.resetStack(param);
        return new Result<>();
    }

    @ApiOperation(value = "【拼接屏】关闭拼接屏(beta)")
    @ApiImplicitParam(name = "id", value = "拼接屏任务id", required = true)
    @PostMapping(value = "/stack/stop")
    public Result<String> stopStack(@NotBlank(message = "拼接屏任务id不为空") @RequestParam(value = "id")String id) {
         iVideoStackService.stopStack(id);
        return new Result<>();
    }
}
