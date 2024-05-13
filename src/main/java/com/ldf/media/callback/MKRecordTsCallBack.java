package com.ldf.media.callback;

import com.aizuda.zlm4j.callback.IMKRecordTsCallBack;
import com.aizuda.zlm4j.structure.MK_RECORD_INFO;
import com.ldf.media.context.MediaServerContext;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import org.springframework.stereotype.Component;

/**
 * 录制ts分片文件成功后广播
 */
@Component
public class MKRecordTsCallBack implements IMKRecordTsCallBack {
    public MKRecordTsCallBack() {
        //回调使用同一个线程
        Native.setCallbackThreadInitializer(this, new CallbackThreadInitializer(true, false, "RecordTsThread"));
    }

    /**
     * 录制ts分片文件成功后广播
     */
    public void invoke(MK_RECORD_INFO info) {
        //录制ts成功回调 通过mk_ts_info_get_*获取各种信息
        String path = MediaServerContext.ZLM_API.mk_record_info_get_file_path(info);
        String folder = MediaServerContext.ZLM_API.mk_record_info_get_folder(info);
        float timeLen = MediaServerContext.ZLM_API.mk_record_info_get_time_len(info);
        long startTime = MediaServerContext.ZLM_API.mk_record_info_get_start_time(info);
    }
}
