package com.ldf.media.api.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.aizuda.zlm4j.callback.IMKWebRtcGetAnwerSdpCallBack;
import com.ldf.media.config.MediaServerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.ldf.media.context.MediaServerContext.ZLM_API;

/**
 * WebRTC信令模块
 *
 * @author lidaofu
 * @since 2024/4/19
 **/
@Api(tags = "【API】webrtc Api")
@RestController
@Slf4j
@CrossOrigin("*")
public class WebRTCController {
    @Autowired
    private MediaServerConfig config;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Value("${server.port}")
    private int port;

    /**
     * webrtc sdp协议交互
     *
     * @param app    应用名称
     * @param stream 流标识
     * @param type   sdp协议动作类型：play、push、echo
     * @param pcSdp  前端请求的sdp协议
     * @return 服务端响应的sdp协议
     * @throws IOException
     */
    @ApiOperation(value = "【webrtc】sdp协议交换")
    @PostMapping("/index/api/webrtc")
    public DeferredResult<ResponseEntity<String>> webrtc(@ApiParam("应用名称") String app,
                                                         @ApiParam("流标识") String stream,
                                                         @ApiParam("sdp协议动作类型：play、push、echo") String type,
                                                         @RequestBody String pcSdp) throws IOException {
        DeferredResult<ResponseEntity<String>> out = new DeferredResult<>();
        //webrtc使用的是udp,默认监听8000,不需要设置端口号
        String rtcUrl = StrUtil.format("rtc://{}:{}/{}/{}", config.getRtc_host(), config.getRtc_port(), app, stream);
        IMKWebRtcGetAnwerSdpCallBack imkWebRtcGetAnwerSdpCallBack = createWebrtcAnswerSdpCallback(out);
        ZLM_API.mk_webrtc_get_answer_sdp(null, imkWebRtcGetAnwerSdpCallBack, type, pcSdp, rtcUrl);
        return out;
    }

    /**
     * 构建服务端SDP协议回调对象
     *
     * @param out 异步接受对象
     * @return
     */
    private static IMKWebRtcGetAnwerSdpCallBack createWebrtcAnswerSdpCallback(DeferredResult<ResponseEntity<String>> out) {
        return (pointer, sevSdp, error) -> {
            JSONObject result = new JSONObject();
            if (StrUtil.isNotBlank(error)) {
                log.error("zkMediaKit 交互 webrtc 协议失败！");
                result.putOnce("code", -1).putOnce("sdp", null);
            } else {
                log.info("zkMediaKit 交互 webrtc 协议成功！");
                result.putOnce("code", 0).putOnce("sdp", sevSdp);
            }
            ResponseEntity<String> response = new ResponseEntity<>(result.toString(), HttpStatus.OK);
            out.setResult(response);
        };
    }


    /**
     * 推流sdp交互
     *
     * @param app
     * @param stream
     */
    @PostMapping("/index/api/whip")
    public void whip(String app, String stream, @RequestBody String offerSdp) {
        //这里可以做鉴权
        String authorization = request.getHeader("Authorization");
        //webrtc使用的是udp,默认监听8000,不需要设置端口号
        String rtcUrl = StrUtil.format("rtc://{}:{}/{}/{}", config.getRtc_host(), config.getRtc_port(), app, stream);
        IMKWebRtcGetAnwerSdpCallBack imkWebRtcGetAnwerSdpCallBack = (pointer, sdp, s1) -> {
            try {
                response.setContentType("application/sdp");
                //todo 如果是https请换为https
                String location = StrUtil.format("http://{}:{}/index/api/delete_webrtc?id={}&token={}", config.getRtc_host(), port, "whip_" + stream, RandomUtil.randomString(8));
                response.setHeader("Location", location);
                response.setStatus(201);
                response.getWriter().write(sdp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        ZLM_API.mk_webrtc_get_answer_sdp(null, imkWebRtcGetAnwerSdpCallBack, "push", offerSdp, rtcUrl);
    }

    /**
     * 拉流sdp交互
     *
     * @param app
     * @param stream
     */
    @PostMapping("/index/api/whep")
    public void whep(String app, String stream, @RequestBody String offerSdp) {
        String rtcUrl = StrUtil.format("rtc://{}:{}/{}/{}", config.getRtc_host(), config.getRtc_port(), app, stream);
        IMKWebRtcGetAnwerSdpCallBack imkWebRtcGetAnwerSdpCallBack = (pointer, sdp, s1) -> {
            try {
                response.setContentType("application/sdp");
                //todo 如果是https请换为https
                String location = StrUtil.format("http://{}:{}/index/api/delete_webrtc?id={}&token={}", config.getRtc_host(), port, "whip_" + stream, RandomUtil.randomString(8));
                response.setHeader("Location", location);
                response.setStatus(201);
                response.getWriter().write(sdp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        ZLM_API.mk_webrtc_get_answer_sdp(null, imkWebRtcGetAnwerSdpCallBack, "play", offerSdp, rtcUrl);
    }

    /**
     * 删除webrtc
     * 要显式终止会话，WHEP 播放器必须对初始HTTP POST的Location头字段中返回的资源URL执行HTTP DELETE请求。收到HTTP DELETE请求后，WHEP资源将被删除，并在媒体服务器上释放资源
     *
     * @param id
     * @param token
     * @throws IOException
     */
    @DeleteMapping("/index/api/delete_webrtc")
    public void deleteWebrtc(String id, String token) throws IOException {
        //todo 校验token后删除资源
        response.setStatus(200);
        response.getWriter().write("");
    }

}
