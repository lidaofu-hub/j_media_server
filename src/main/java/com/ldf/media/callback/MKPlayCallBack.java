package com.ldf.media.callback;

import com.aizuda.zlm4j.callback.IMKPlayCallBack;
import com.aizuda.zlm4j.structure.MK_AUTH_INVOKER;
import com.aizuda.zlm4j.structure.MK_MEDIA_INFO;
import com.aizuda.zlm4j.structure.MK_SOCK_INFO;
import com.ldf.media.context.MediaServerContext;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import org.springframework.stereotype.Component;

/**
 * 播放rtsp/rtmp/http-flv/hls事件广播，通过该事件控制播放鉴权
 *
 * @author lidaofu
 * @since 2023/11/23
 **/
@Component
public class MKPlayCallBack implements IMKPlayCallBack {
    public MKPlayCallBack() {
        //回调使用同一个线程
        Native.setCallbackThreadInitializer(this, new CallbackThreadInitializer(true, false, "MediaPlayThread"));
    }

    /**
     * 播放rtsp/rtmp/http-flv/hls事件广播，通过该事件控制播放鉴权
     *
     * @param url_info 播放url相关信息
     * @param invoker  执行invoker返回鉴权结果
     * @param sender   播放客户端相关信息
     * @see mk_auth_invoker_do
     */
    public void invoke(MK_MEDIA_INFO url_info, MK_AUTH_INVOKER invoker, MK_SOCK_INFO sender) {
        //这里拿到访问路径后(例如http://xxxx/xxx/xxx.live.flv?token=xxxx其中?后面就是拿到的参数)的参数
        // err_msg返回 空字符串表示鉴权成功 否则鉴权失败提示
        //String param = ZLM_API.mk_media_info_get_params(url_info);
        MediaServerContext.ZLM_API.mk_auth_invoker_do(invoker, "");
    }
}
