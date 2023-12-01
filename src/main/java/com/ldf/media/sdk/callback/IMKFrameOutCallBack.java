package com.ldf.media.sdk.callback;


import com.ldf.media.sdk.structure.MK_FRAME;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;

/**
 * 输出frame回调
 */
public interface IMKFrameOutCallBack extends Callback {
    /**
     * 输出frame回调
     */
    public void invoke(Pointer user_data, MK_FRAME frame);
}