package com.ldf.media.callback;

import com.ldf.media.sdk.callback.IMKRecordMp4CallBack;
import com.ldf.media.sdk.structure.MK_MP4_INFO;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;

/**
 * 录制mp4分片文件成功后广播
 */
public class MKRecordMp4CallBack implements IMKRecordMp4CallBack {
    public MKRecordMp4CallBack() {
        //回调使用同一个线程
        Native.setCallbackThreadInitializer(this, new CallbackThreadInitializer(true, false, "RecordMp4Thread"));
    }

    /**
     * 录制mp4分片文件成功后广播
     */
    public void invoke(MK_MP4_INFO mp4) {

    }
}
