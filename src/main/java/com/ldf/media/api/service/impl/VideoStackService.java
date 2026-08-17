package com.ldf.media.api.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.zlm4j.structure.MK_MEDIA_SOURCE;
import com.ldf.media.api.model.param.VideoStackParam;
import com.ldf.media.api.service.IVideoStackService;
import com.ldf.media.config.MediaServerConfig;
import com.ldf.media.constants.MediaServerConstants;
import com.ldf.media.module.stack.VideoStack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import static com.ldf.media.context.MediaServerContext.ZLM_API;

@Slf4j
@Service
public class VideoStackService implements IVideoStackService {
    @Autowired
    private MediaServerConfig mediaServerConfig;
    private final static Map<String, VideoStack> VIDEO_STACK_MAP = new ConcurrentHashMap<>();


    @Override
    public void startStack(VideoStackParam param) {
        if (StrUtil.isBlank(param.getPushUrl())) {
            MK_MEDIA_SOURCE mkMediaSource = ZLM_API.mk_media_source_find2("rtmp", MediaServerConstants.DEFAULT_VHOST, param.getApp(), param.getId(), 0);
            Assert.isNull(mkMediaSource, "当前流已在线");
        }
        Assert.isFalse(VIDEO_STACK_MAP.containsKey(param.getId()), "拼接屏任务已存在");
        //String pushUrl = StrUtil.format("rtmp://127.0.0.1:{}/{}/{}", mediaServerConfig.getRtmp_port(), param.getApp(), param.getId());
        VideoStack videoStack = new VideoStack(param);
        videoStack.init();
        VideoStack old = VIDEO_STACK_MAP.putIfAbsent(param.getId(), videoStack);
        if (old != null) {
            videoStack.stop();
            throw new IllegalArgumentException("拼接屏任务已存在");
        }
    }


    @Override
    public void resetStack(VideoStackParam param) {
        VideoStack videoStack = VIDEO_STACK_MAP.get(param.getId());
        Assert.isTrue(VIDEO_STACK_MAP.containsKey(param.getId()), "拼接屏任务不存在");
        videoStack.reset(param);
    }

    @Override
    public void stopStack(String id) {
        VideoStack videoStack = VIDEO_STACK_MAP.remove(id);
        Assert.notNull(videoStack, "拼接屏任务不存在");
        videoStack.stop();
    }

    @PreDestroy
    public void destroy() {
        for (VideoStack videoStack : VIDEO_STACK_MAP.values()) {
            try {
                videoStack.stop();
            } catch (Exception e) {
                log.error("【拼接屏】停止任务异常", e);
            }
        }
        VIDEO_STACK_MAP.clear();
    }

    @Override
    public List<VideoStackParam> listStack() {
        List<VideoStackParam> list = new ArrayList<>();
        for (VideoStack videoStack : VIDEO_STACK_MAP.values()) {
            list.add(videoStack.getParam());
        }
        return list;
    }
}
