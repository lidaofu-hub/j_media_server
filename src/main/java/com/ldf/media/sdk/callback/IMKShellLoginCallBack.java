package com.ldf.media.sdk.callback;

import com.ldf.media.sdk.structure.MK_AUTH_INVOKER;
import com.ldf.media.sdk.structure.MK_SOCK_INFO;
import com.sun.jna.Callback;

/**
 * shell登录鉴权
 *
 * @author lidaofu
 * @since 2023/11/23
 **/
public interface IMKShellLoginCallBack extends Callback {
    /**
     * shell登录鉴权
     */
    public void invoke(String user_name, String passwd, MK_AUTH_INVOKER invoker, MK_SOCK_INFO sender);
}
