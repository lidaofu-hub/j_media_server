package com.ldf.media.api.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.zlm4j.structure.MK_MEDIA_SOURCE;
import com.ldf.media.api.model.param.TestVideoParam;
import com.ldf.media.api.model.param.TranscodeParam;
import com.ldf.media.api.model.result.StreamUrlResult;
import com.ldf.media.api.service.ITestVideoService;
import com.ldf.media.api.service.ITranscodeService;
import com.ldf.media.config.MediaServerConfig;
import com.ldf.media.constants.MediaServerConstants;
import com.ldf.media.module.test.TestVideo;
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
public class TestVideoService implements ITestVideoService {
    @Autowired
    private MediaServerConfig config;
    /**
     * 测试视频
     */
    private static final Map<String, TestVideo> TEST_VIDEO_MAP = new HashMap<>();

    @Override
    public StreamUrlResult createTestVideo(TestVideoParam param) {
        TestVideo testVideo = new TestVideo(param);
        testVideo.initVideo();
        testVideo.startTestVideo();
        TEST_VIDEO_MAP.put(param.getApp() + param.getStream(), testVideo);
        return new StreamUrlResult(config, param);
    }

    @Override
    public Boolean stopTestVideo(String app, String stream) {
        String key = app + stream;
        TestVideo testVideo = TEST_VIDEO_MAP.get(key);
        if (testVideo != null) {
            testVideo.closeVideo();
            TEST_VIDEO_MAP.remove(key);
            return true;
        }
        return false;
    }
}
