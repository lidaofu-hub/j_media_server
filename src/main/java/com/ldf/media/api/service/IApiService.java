package com.ldf.media.api.service;

import com.ldf.media.api.model.param.*;
import com.ldf.media.api.model.result.MediaInfoResult;

import java.util.List;

/**
 * 接口服务
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
public interface IApiService {

    void addStreamProxy(StreamProxyParam param);

    Integer closeStream(CloseStreamParam  param);

    Integer closeStreams(CloseStreamsParam param);
    List<MediaInfoResult> getMediaList(GetMediaListParam param);

    Boolean startRecord(StartRecordParam param);

    Boolean stopRecord(StopRecordParam param);

    Boolean isRecording(RecordStatusParam param);
}
