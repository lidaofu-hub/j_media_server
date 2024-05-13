package com.ldf.media.callback;

import com.aizuda.zlm4j.callback.IMKStreamChangeCallBack;
import com.aizuda.zlm4j.structure.MK_MEDIA_SOURCE;
import com.ldf.media.context.MediaServerContext;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 注册或反注册MediaSource事件广播
 *
 * @author lidaofu
 * @since 2023/11/23
 **/
@Component
@Slf4j
public class MKStreamChangeCallBack implements IMKStreamChangeCallBack {

    public MKStreamChangeCallBack() {
        Native.setCallbackThreadInitializer(this, new CallbackThreadInitializer(true, false, "MediaStreamChangeThread"));
    }

    /**
     * 注册或反注册MediaSource事件广播
     *
     * @param regist 注册为1，注销为0
     * @param sender 该MediaSource对象
     */
    public void invoke(int regist, MK_MEDIA_SOURCE sender) {
        //这里进行流状态处理
        String stream = MediaServerContext.ZLM_API.mk_media_source_get_stream(sender);
        String app = MediaServerContext.ZLM_API.mk_media_source_get_app(sender);
        String schema = MediaServerContext.ZLM_API.mk_media_source_get_schema(sender);
        //如果是regist是注销情况下无法获取流详细信息如观看人数等
        log.info("【MediaServer】APP:{} 流:{} 协议：{} {}", app, stream, schema, regist == 1 ? "注册" : "注销");
    }
}
