package com.ldf.media.callback;

import com.aizuda.zlm4j.callback.IMKHttpRequestCallBack;
import com.aizuda.zlm4j.structure.MK_HTTP_RESPONSE_INVOKER;
import com.aizuda.zlm4j.structure.MK_PARSER;
import com.aizuda.zlm4j.structure.MK_SOCK_INFO;
import com.sun.jna.ptr.IntByReference;
import org.springframework.stereotype.Component;

/**
 * 收到http api请求广播(包括GET/POST)
 */
@Component
public class MKHttpRequestCallBack implements IMKHttpRequestCallBack {
    /**
     * 收到http api请求广播(包括GET/POST)
     *
     * @param parser   http请求内容对象
     * @param invoker  执行该invoker返回http回复
     * @param consumed 置1则说明我们要处理该事件
     * @param sender   http客户端相关信息
     */
    public void invoke(MK_PARSER parser, MK_HTTP_RESPONSE_INVOKER invoker, IntByReference consumed, MK_SOCK_INFO sender) {
        consumed.setValue(0);
    }
}
