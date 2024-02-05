package com.ldf.media.callback;

import com.aizuda.callback.IMKNoFoundCallBack;
import com.aizuda.structure.MK_MEDIA_INFO;
import com.aizuda.structure.MK_SOCK_INFO;
import com.sun.jna.Callback;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;

/**
 * 未找到流后会广播该事件，请在监听该事件后去拉流或其他方式产生流，这样就能按需拉流了
 *
 * @author lidaofu
 * @since 2023/11/23
 **/
public class MKNoFoundCallBack implements IMKNoFoundCallBack {

    public MKNoFoundCallBack() {
        //回调使用同一个线程
        Native.setCallbackThreadInitializer(this, new CallbackThreadInitializer(true, false, "MediaNoFoundThread"));
    }
    /**
     * 未找到流后会广播该事件，请在监听该事件后去拉流或其他方式产生流，这样就能按需拉流了
     *
     * @param url_info 播放url相关信息
     * @param sender   播放客户端相关信息
     * @return 1 直接关闭
     * 0 等待流注册
     */
    public int invoke(MK_MEDIA_INFO url_info, MK_SOCK_INFO sender){
        //这里可以实现按需拉流，这里面新起个线程去操作拉起流
        return 1;
    }
}
