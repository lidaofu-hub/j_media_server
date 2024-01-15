package com.ldf.media.callback;

import com.ldf.media.context.MediaServerContext;
import com.ldf.media.sdk.callback.IMKPublishCallBack;
import com.ldf.media.sdk.structure.MK_MEDIA_INFO;
import com.ldf.media.sdk.structure.MK_PUBLISH_AUTH_INVOKER;
import com.ldf.media.sdk.structure.MK_SOCK_INFO;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;

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
        MediaServerContext.ZLM_API.mk_publish_auth_invoker_do(invoker,"",0,0);
    }
}
