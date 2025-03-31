package com.ldf.media.api.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.zlm4j.structure.MK_MEDIA_SOURCE;
import com.ldf.media.api.model.param.TranscodeParam;
import com.ldf.media.api.service.ITranscodeService;
import com.ldf.media.config.MediaServerConfig;
import com.ldf.media.constants.MediaServerConstants;
import com.ldf.media.module.transcode.Transcode;
import com.ldf.media.pool.MediaServerThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.ldf.media.context.MediaServerContext.ZLM_API;

@Slf4j
@Service
public class TranscodeService implements ITranscodeService {
    @Autowired
    private MediaServerConfig mediaServerConfig;
    private Map<String, Transcode> TRANSCODE_MAP = new HashMap<>();

    @Override
    public void transcode(TranscodeParam param) {
        MK_MEDIA_SOURCE mkMediaSource = ZLM_API.mk_media_source_find2("rtmp", MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getStream(), 0);
        Assert.isNull(mkMediaSource, "当前流已在线");
        Assert.isFalse(TRANSCODE_MAP.containsKey(param.getStream()), "转码任务已存在");
        String pushUrl = StrUtil.format("rtmp://127.0.0.1:{}/{}/{}", mediaServerConfig.getRtmp_port(), param.getApp(), param.getStream());
        Transcode transcode = new Transcode(param, pushUrl);
        MediaServerThreadPool.execute(() -> {
            transcode.start();
        });
        TRANSCODE_MAP.put(param.getStream(), transcode);
    }

    @Override
    public void stopTranscode(String stream) {
        Transcode transcode = TRANSCODE_MAP.get(stream);
        if (transcode!=null){
            transcode.stop();
            TRANSCODE_MAP.remove(stream);
        }
    }


}
