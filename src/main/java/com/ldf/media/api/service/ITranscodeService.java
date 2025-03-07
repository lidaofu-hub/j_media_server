package com.ldf.media.api.service;

import com.ldf.media.api.model.param.TranscodeParam;

public interface ITranscodeService {

    /**
     * 转码
     * @param param
     * @return
     */
    void transcode(TranscodeParam param);
}
