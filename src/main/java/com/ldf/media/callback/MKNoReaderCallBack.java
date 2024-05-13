package com.ldf.media.callback;

import com.ldf.media.context.MediaServerContext;
import com.ldf.media.pool.MediaServerThreadPool;
import com.aizuda.zlm4j.callback.IMKNoReaderCallBack;
import com.aizuda.zlm4j.structure.MK_MEDIA_SOURCE;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import org.springframework.stereotype.Component;

/**
 * 无人观看回调
 *
 * @author lidaofu
 * @since 2023/11/23
 **/
@Component
public class MKNoReaderCallBack implements IMKNoReaderCallBack {
    public MKNoReaderCallBack() {
        //回调使用同一个线程
        Native.setCallbackThreadInitializer(this, new CallbackThreadInitializer(true, false, "MediaNoReaderThread"));
    }

    /**
     * 某个流无人消费时触发，目的为了实现无人观看时主动断开拉流等业务逻辑
     *
     * @param sender 该MediaSource对象
     */
    public void invoke(MK_MEDIA_SOURCE sender) {
        //无人观看时候可以调用下面的实现关流 不调用就代表不关流 需要配置protocol.auto_close 为 0 这里才会有回调
        //MediaServerContext.ZLM_API.mk_media_source_close(sender,0);
    }
}
