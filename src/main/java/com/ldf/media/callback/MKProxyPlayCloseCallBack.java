package com.ldf.media.callback;

import com.aizuda.zlm4j.callback.IMKProxyPlayerCallBack;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Component;

/**
 * MediaSource.close()回调事件
 */
@Component
public class MKProxyPlayCloseCallBack implements IMKProxyPlayerCallBack {
    /**
     * MediaSource.close()回调事件
     * 在选择关闭一个关联的MediaSource时，将会最终触发到该回调
     * 你应该通过该事件调用mk_proxy_player_release函数并且释放其他资源
     * 如果你不调用mk_proxy_player_release函数，那么MediaSource.close()操作将无效
     *
     * @param pUser 用户数据指针，通过mk_proxy_player_set_on_close函数设置
     */
    public void invoke(Pointer pUser, int err, String what, int sys_err) {

    }
}
