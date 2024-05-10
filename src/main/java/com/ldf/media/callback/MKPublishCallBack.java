package com.ldf.media.callback;

import com.aizuda.zlm4j.structure.MK_INI;
import com.ldf.media.context.MediaServerContext;
import com.aizuda.zlm4j.callback.IMKPublishCallBack;
import com.aizuda.zlm4j.structure.MK_MEDIA_INFO;
import com.aizuda.zlm4j.structure.MK_PUBLISH_AUTH_INVOKER;
import com.aizuda.zlm4j.structure.MK_SOCK_INFO;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;

import static com.ldf.media.context.MediaServerContext.ZLM_API;

/**
 * 推流回调
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
public class MKPublishCallBack implements IMKPublishCallBack {

    public MKPublishCallBack() {
        //回调使用同一个线程
        Native.setCallbackThreadInitializer(this, new CallbackThreadInitializer(true, false, "MediaPublishThread"));
    }

    /**
     * 收到rtsp/rtmp推流事件广播，通过该事件控制推流鉴权
     *
     * @param url_info 推流url相关信息
     * @param invoker  执行invoker返回鉴权结果
     * @param sender   该tcp客户端相关信息
     * @see mk_publish_auth_invoker_do
     */
    @Override
    public void invoke(MK_MEDIA_INFO url_info, MK_PUBLISH_AUTH_INVOKER invoker, MK_SOCK_INFO sender) {
        //这里拿到访问路径后(例如rtmp://xxxx/xxx/xxx?token=xxxx其中?后面就是拿到的参数)的参数
        // err_msg返回 空字符串表示鉴权成功 否则鉴权失败提示
        //String param = ZLM_API.mk_media_info_get_params(url_info);
        //MediaServerContext.ZLM_API.mk_publish_auth_invoker_do(invoker,"",1,0);
        MK_INI option = ZLM_API.mk_ini_create();
        ZLM_API.mk_ini_set_option_int(option, "enable_mp4", 0);
        ZLM_API.mk_ini_set_option_int(option, "enable_audio", 0);
        ZLM_API.mk_ini_set_option_int(option, "enable_fmp4",0);
        ZLM_API.mk_ini_set_option_int(option, "enable_ts", 0);
        ZLM_API.mk_ini_set_option_int(option, "enable_hls",1);
        ZLM_API.mk_ini_set_option_int(option, "enable_rtsp", 1);
        ZLM_API.mk_ini_set_option_int(option, "enable_rtmp", 1);
        //ZLM_API.mk_ini_set_option_int(option, "auto_close", 0);
        ZLM_API.mk_ini_set_option_int(option, "mp4_max_second", 360);
        ZLM_API.mk_ini_set_option_int(option, "segNum", 0);
        //流名称替换
        //ZLM_API.mk_ini_set_option(option, "stream_replace", "test1");
        ZLM_API.mk_publish_auth_invoker_do2(invoker, "", option);
        ZLM_API.mk_ini_release(option);
    }
}
