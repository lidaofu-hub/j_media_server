package com.ldf.media.sdk.callback;

import com.ldf.media.sdk.structure.MK_MP4_INFO;
import com.sun.jna.Callback;

/**
 * 录制mp4分片文件成功后广播
 */
public interface IMKRecordMp4CallBack extends Callback {
    /**
     * 录制mp4分片文件成功后广播
     */
    public void invoke(MK_MP4_INFO mp4);
}
