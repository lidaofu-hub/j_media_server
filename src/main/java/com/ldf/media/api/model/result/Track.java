package com.ldf.media.api.model.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class Track implements Serializable {
    private static final long serialVersionUID = 1;

    private Integer is_video;
    private Integer codec_id;
    private String codec_id_name;
    private Integer codec_type;
    private Integer fps;
    private Integer frames;
    private Integer bit_rate;
    private Integer gop_interval_ms;
    private Integer gop_size;
    private Integer height;
    private Integer key_frames;
    private Integer loss;
    private Boolean ready;
    private Integer width;
    private Integer sample_rate;
    private Integer audio_channel;
    private Integer audio_sample_bit;

}