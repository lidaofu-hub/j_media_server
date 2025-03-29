package com.ldf.media.api.service;

import com.ldf.media.api.model.param.VideoStackParam;

public interface IVideoStackService {

    /**
     * 开启拼接屏任务
     * @param param
     * @return
     */
    String startStack(VideoStackParam param);

    /**
     * 重设拼接屏任务
     * @param param
     * @return
     */
    String resetStack(VideoStackParam param);

    /**
     * 停止拼接屏任务
     * @param id
     * @return
     */
    String stopStack(String id);
}
