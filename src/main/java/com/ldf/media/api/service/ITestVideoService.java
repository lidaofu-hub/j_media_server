package com.ldf.media.api.service;

import com.ldf.media.api.model.param.TestVideoParam;
import com.ldf.media.api.model.result.StreamUrlResult;

public interface ITestVideoService {
    StreamUrlResult createTestVideo(TestVideoParam param);

    Boolean stopTestVideo(String app,String stream);
}
