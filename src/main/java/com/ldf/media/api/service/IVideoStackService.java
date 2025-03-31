package com.ldf.media.api.service;

import com.ldf.media.api.model.param.VideoStackParam;

public interface IVideoStackService {

    /**
     * 开启拼接屏任务
     * @param param
     * @return
     */
    void startStack(VideoStackParam param);

    /**
     * 重设拼接屏任务
     * @param param
     * @return
     */
    void resetStack(VideoStackParam param);

    /**
     * 停止拼接屏任务
     * @param id
     * @return
     */
    void stopStack(String id);
}
