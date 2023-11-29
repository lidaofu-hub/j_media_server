package com.ldf.media.api.service.impl;

import com.ldf.media.api.model.StreamProxyParam;
import com.ldf.media.api.service.IApiService;
import com.ldf.media.context.MediaServerContext;
import com.ldf.media.sdk.callback.IMKProxyPlayCloseCallBack;
import com.ldf.media.sdk.structure.MK_MEDIA_SOURCE;
import com.ldf.media.sdk.structure.MK_PROXY_PLAYER;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Service;

/**
 * 接口服务
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
@Service
public class ApiServiceImpl implements IApiService {
    @Override
    public Boolean addStreamProxy(StreamProxyParam param) {
        //查询流是是否存在
        MK_MEDIA_SOURCE mkMediaSource = MediaServerContext.ZLM_API.mk_media_source_find2(param.getEnableRtmp()==1?"rtmp":"rtsp", "__defaultVhost__", param.getApp(), param.getStream(), 0);
        if (mkMediaSource!=null){
            return false;
        }
        //创建拉流代理
        MK_PROXY_PLAYER mk_proxy = MediaServerContext.ZLM_API.mk_proxy_player_create("__defaultVhost__", param.getApp(), param.getStream(), param.getEnableHls(), param.getEnableMp4(), param.getEnableFmp4(), param.getEnableTs(), param.getEnableRtmp(), param.getEnableRtsp());
        //回调关闭时间
        IMKProxyPlayCloseCallBack imkProxyPlayCloseCallBack = (pUser, err, what, sys_err) -> {
            //这里Pointer是ZLM维护的不需要我们释放 遵循谁申请谁释放原则
            MediaServerContext.ZLM_API.mk_proxy_player_release(new MK_PROXY_PLAYER(pUser));
        };
        MediaServerContext.ZLM_API.mk_proxy_player_set_option(mk_proxy,"rtp_type",param.getRtpType().toString());
        //开始播放
        MediaServerContext. ZLM_API.mk_proxy_player_play(mk_proxy, param.getUrl());
        //添加代理关闭回调 并把代理客户端传过去释放
        MediaServerContext.ZLM_API.mk_proxy_player_set_on_close(mk_proxy, imkProxyPlayCloseCallBack, mk_proxy.getPointer());
        return true;
    }
}
