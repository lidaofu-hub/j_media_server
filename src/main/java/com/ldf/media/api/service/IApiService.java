package com.ldf.media.api.service;

import com.ldf.media.api.model.param.*;
import com.ldf.media.api.model.result.MediaInfoResult;
import com.ldf.media.api.model.result.RtpServerResult;
import com.ldf.media.api.model.result.Statistic;

import java.util.List;
import java.util.Map;

/**
 * 接口服务
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
public interface IApiService {

    void addStreamProxy(StreamProxyParam param);

    Integer closeStream(CloseStreamParam param);

    Integer closeStreams(CloseStreamsParam param);

    List<MediaInfoResult> getMediaList(GetMediaListParam param);

    Boolean isMediaOnline(MediaQueryParam param);

    MediaInfoResult getMediaInfo(MediaQueryParam param);

    Boolean startRecord(StartRecordParam param);

    Boolean stopRecord(StopRecordParam param);

    Boolean isRecording(RecordStatusParam param);

    Statistic getStatistic();

    String getServerConfig();

    Boolean restartServer();

    Integer setServerConfig(Map<String, String[]> parameterMap);


    Integer openRtpServer(OpenRtpServerParam param);

    Integer closeRtpServer(String streamId);

    List<RtpServerResult> listRtpServer();
}
