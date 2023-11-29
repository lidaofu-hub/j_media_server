package com.ldf.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 媒体服务器配置
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "media")
public class MediaServerConfig {

    private Integer thread_num;

    private Integer rtmp_port;

    private Integer rtsp_port;

    private Integer http_port;

    private Integer auto_close;

    private Integer streamNoneReaderDelayMS;

    private Integer maxStreamWaitMS;

    private Integer enable_ts;

    private Integer enable_hls;

    private Integer enable_fmp4;

    private Integer enable_rtsp;

    private Integer enable_rtmp;

    private Integer enable_mp4;

    private Integer enable_hls_fmp4;

    private Integer enable_audio;

    private Integer mp4_as_player;

    private Integer mp4_max_second;

    private String mp4_save_path;

    private String hls_save_path;

    private Integer hls_demand;

    private Integer rtsp_demand;

    private Integer rtmp_demand;

    private Integer ts_demand;

    private Integer fmp4_demand;

    private Integer log_level;

    private Integer log_mask;

    private Integer log_file_days;

    private String log_path;
}
