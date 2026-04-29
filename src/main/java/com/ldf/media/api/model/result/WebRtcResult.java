package com.ldf.media.api.model.result;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WebRtcResult {

    private Integer code;

    private String sdp;

    private String msg;

    public WebRtcResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public WebRtcResult(Integer code, String msg, String sdp) {
        this.code = code;
        this.msg = msg;
        this.sdp = sdp;
    }


}
