package com.ldf.media.api.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.aizuda.zlm4j.callback.IMKWebRtcGetAnwerSdpCallBack;
import com.ldf.media.config.MediaServerConfig;
import com.sun.jna.Pointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.ldf.media.context.MediaServerContext.ZLM_API;

/**
 * WebRTC信令模块
 *
 * @author lidaofu
 * @since 2024/4/19
 **/
@RestController
public class WebRTCController {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private MediaServerConfig config;
    @Value("${server.port}")
    private int port;
    //这里用map做个简单的Transaction管理容器
    private final static Map<String, String> SDP_MAP = new HashMap<>();

    /**
     * 推流sdp交互
     *
     * @param app
     * @param stream
     */
    @PostMapping("/index/api/whip")
    public void whip(String app, String stream) throws IOException {
        //这里可以做鉴权
        String authorization = request.getHeader("Authorization");
        ServletInputStream inputStream = request.getInputStream();
        byte[] bytes = inputStream.readAllBytes();
        String offerSdp = new String(bytes);
        //webrtc使用的是udp,默认监听8000,不需要设置端口号
        String rtcUrl= StrUtil.format("rtc://{}:{}/{}/{}",config.getRtc_host(),config.getRtc_port(),app,stream);
        IMKWebRtcGetAnwerSdpCallBack imkWebRtcGetAnwerSdpCallBack = (pointer, sdp, s1) -> {
            try {
                SDP_MAP.put("whip_"+stream, sdp);
                response.setContentType("application/sdp");
                //todo 如果是https请换为https
                String location=StrUtil.format("http://{}:{}/index/api/delete_webrtc?id={}&token={}",config.getRtc_host(),config.getRtc_port(),port,"whip_"+stream, RandomUtil.randomString(8));
                response.setHeader("Location",location);
                response.setStatus(201);
                response.getWriter().write(sdp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        ZLM_API.mk_webrtc_get_answer_sdp(null,imkWebRtcGetAnwerSdpCallBack,"push",offerSdp,rtcUrl);
    }

    /**
     * 拉流sdp交互
     *
     * @param app
     * @param stream
     */
    @PostMapping("/index/api/whep")
    public void whep(String app, String stream) throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        byte[] bytes = inputStream.readAllBytes();
        String offerSdp = new String(bytes);
        String rtcUrl= StrUtil.format("rtc://{}:{}/{}/{}",config.getRtc_host(),config.getRtc_port(),app,stream);
        IMKWebRtcGetAnwerSdpCallBack imkWebRtcGetAnwerSdpCallBack = (pointer, sdp, s1) -> {
            try {
                SDP_MAP.put("whep_"+stream, sdp);
                response.setContentType("application/sdp");
                //todo 如果是https请换为https
                String location=StrUtil.format("http://{}:{}/index/api/delete_webrtc?id={}&token={}",config.getRtc_host(),config.getRtc_port(),port,"whip_"+stream, RandomUtil.randomString(8));
                response.setHeader("Location",location);
                response.setStatus(201);
                response.getWriter().write(sdp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        ZLM_API.mk_webrtc_get_answer_sdp(null,imkWebRtcGetAnwerSdpCallBack,"play",offerSdp,rtcUrl);
    }

    /**
     * 删除webrtc
     * 要显式终止会话，WHEP 播放器必须对初始HTTP POST的Location头字段中返回的资源URL执行HTTP DELETE请求。收到HTTP DELETE请求后，WHEP资源将被删除，并在媒体服务器上释放资源
     * @param id
     * @param token
     * @throws IOException
     */
    @DeleteMapping("/index/api/delete_webrtc")
    public void deleteWebrtc(String id, String token) throws IOException {
        //todo 校验token后删除资源
        SDP_MAP.remove(id);
        response.setStatus(200);
        response.getWriter().write("");
    }
}
