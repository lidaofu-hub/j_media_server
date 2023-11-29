package com.ldf.media.callback;

import com.ldf.media.sdk.callback.IMKPublishCallBack;
import com.ldf.media.sdk.structure.MK_MEDIA_INFO;
import com.ldf.media.sdk.structure.MK_PUBLISH_AUTH_INVOKER;
import com.ldf.media.sdk.structure.MK_SOCK_INFO;

/**
 * 推流回调
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
public class MKPublishCallBack implements IMKPublishCallBack {

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

    }
}
