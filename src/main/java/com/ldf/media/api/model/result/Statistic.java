package com.ldf.media.api.model.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 内存占用信息
 *
 * @author lidaofu
 * @since 2024/5/20
 **/
@Data
public class Statistic implements Serializable {
    private static final long serialVersionUID = 1;

    private Long mediaSource;
    private Long multiMediaSourceMuxer;
    private Long tcpServer;
    private Long tcpSession;
    private Long udpServer;
    private Long udpSession;
    private Long tcpClient;
    private Long socket;
    private Long frameImp;
    private Long frame;
    private Long buffer;
    private Long bufferRaw;
    private Long bufferLikeString;
    private Long bufferList;
    private Long rtpPacket;
    private Long rtmpPacket;
}
