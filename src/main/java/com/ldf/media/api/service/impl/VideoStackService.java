package com.ldf.media.api.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ldf.media.api.model.param.VideoStackParam;
import com.ldf.media.api.service.IVideoStackService;
import com.ldf.media.config.MediaServerConfig;
import com.ldf.media.module.stack.VideoStack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VideoStackService implements IVideoStackService {
    @Autowired
    private MediaServerConfig mediaServerConfig;
    private final static Map<String, VideoStack> VIDEO_STACK_MAP = new HashMap<>();


    @Override
    public String startStack(VideoStackParam param) {
        if (VIDEO_STACK_MAP.containsKey(param.getId())){
            return "任务已存在";
        }
        String pushUrl = StrUtil.format("rtmp://127.0.0.1:{}/{}/{}", mediaServerConfig.getRtmp_port(), param.getApp(), param.getId());
        VideoStack videoStack = new VideoStack(param, pushUrl);
        videoStack.init();
        VIDEO_STACK_MAP.put(param.getId(), videoStack);
        return "success";
    }


    @Override
    public String resetStack(VideoStackParam param) {
        VideoStack videoStack = VIDEO_STACK_MAP.get(param.getId());
        if (videoStack != null) {
            videoStack.reset(param);
            return "success";
        }
        return "任务不存在";
    }

    @Override
    public String stopStack(String id) {
        VideoStack videoStack = VIDEO_STACK_MAP.get(id);
        if (videoStack != null) {
            videoStack.stop();
            VIDEO_STACK_MAP.remove(id);
            return "success";
        }
        return "任务不存在";
    }
}
