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

    String addStreamProxy(StreamProxyParam param);

    Boolean delStreamProxy(String key);

    String addStreamPusherProxy(StreamPushProxyParam param);

    Boolean delStreamPusherProxy(String key);

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

    Integer closeRtpServer(String stream);

    List<RtpServerResult> listRtpServer();
}
