package com.ldf.media.callback;

import com.aizuda.zlm4j.callback.IMKSourceFindCallBack;
import com.aizuda.zlm4j.structure.MK_MEDIA_SOURCE;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * 寻找流回调
 */
public class MKSourceFindCallBack implements IMKSourceFindCallBack {
    private IMKSourceHandleCallBack imkSourceHandleCallBack;

    public MKSourceFindCallBack(IMKSourceHandleCallBack imkSourceHandleCallBack) {
        Native.setCallbackThreadInitializer(this, new CallbackThreadInitializer(true, false, "MediaSourceFindThread"));
        this.imkSourceHandleCallBack=imkSourceHandleCallBack;
    }

    public void invoke(Pointer user_data, MK_MEDIA_SOURCE ctx) {
        imkSourceHandleCallBack.invoke(ctx);
    }
}