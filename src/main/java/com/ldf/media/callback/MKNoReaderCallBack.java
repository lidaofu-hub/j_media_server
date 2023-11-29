package com.ldf.media.callback;

import com.ldf.media.context.MediaServerContext;
import com.ldf.media.pool.MediaServerThreadPool;
import com.ldf.media.sdk.callback.IMKNoReaderCallBack;
import com.ldf.media.sdk.structure.MK_MEDIA_SOURCE;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;

/**
 * 无人观看回调
 *
 * @author lidaofu
 * @since 2023/11/23
 **/
public class MKNoReaderCallBack implements IMKNoReaderCallBack {
    public MKNoReaderCallBack() {
        CallbackThreadInitializer mediaServerLogThread = new CallbackThreadInitializer(true, false, "MediaNoReaderThread");
        //回调使用同一个线程
        Native.setCallbackThreadInitializer(this, mediaServerLogThread);
    }

    /**
     * 某个流无人消费时触发，目的为了实现无人观看时主动断开拉流等业务逻辑
     *
     * @param sender 该MediaSource对象
     */
    public void invoke(MK_MEDIA_SOURCE sender) {
        MediaServerContext.ZLM_API.mk_media_source_close(sender,1);
    }
}
