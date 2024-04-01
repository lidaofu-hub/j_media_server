package com.ldf.media.callback;

import com.aizuda.zlm4j.structure.MK_MEDIA_SOURCE;

/**
 * 媒体资源回调接口
 *
 * @author lidaofu
 * @since 2023/11/30
 **/
public interface IMKSourceHandleCallBack {

    void invoke(MK_MEDIA_SOURCE ctx);
}
