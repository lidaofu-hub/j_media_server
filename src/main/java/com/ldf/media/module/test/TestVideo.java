package com.ldf.media.module.test;

import com.aizuda.zlm4j.structure.MK_INI;
import com.aizuda.zlm4j.structure.MK_MEDIA;
import com.ldf.media.api.model.param.TestVideoParam;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.ldf.media.context.MediaServerContext.ZLM_API;

public class TestVideo {
    private TestVideoParam param;
    private MK_MEDIA mkMedia;
    private Pointer yPointer;
    private Pointer uPointer;
    private Pointer vPointer;
    private int[] linesize;
    private LinkedBlockingQueue<Long> frameQueue;
    private long pts = 0;

    public TestVideo(TestVideoParam param) {
        this.param = param;
        this.frameQueue = new LinkedBlockingQueue<>(25);
    }

    /**
     * 初始化视频
     */
    public void initVideo() {
        //创建媒体流 支持配置参数
        MK_INI mkIni = ZLM_API.mk_ini_create();
        ZLM_API.mk_ini_set_option_int(mkIni, "enable_rtsp", param.getEnableRtsp());
        ZLM_API.mk_ini_set_option_int(mkIni, "enable_rtmp", param.getEnableRtmp());
        ZLM_API.mk_ini_set_option_int(mkIni, "enable_fmp4", param.getEnableFmp4());
        ZLM_API.mk_ini_set_option_int(mkIni, "enable_hls", param.getEnableHls());
        ZLM_API.mk_ini_set_option_int(mkIni, "enable_ts", param.getEnableTs());
        ZLM_API.mk_ini_set_option_int(mkIni, "enable_mp4", param.getEnableMp4());
        ZLM_API.mk_ini_set_option_int(mkIni, "mp4_max_second", param.getMp4MaxSecond());
        ZLM_API.mk_ini_set_option_int(mkIni, "auto_close", param.getAutoClose());
        mkMedia = ZLM_API.mk_media_create2("__defaultVhost__", param.getApp(), param.getStream(), 0, mkIni);
        ZLM_API.mk_ini_release(mkIni);
        ZLM_API.mk_media_init_video(mkMedia, 0, param.getWidth(), param.getHeight(), param.getFps(), param.getBitRate());
        ZLM_API.mk_media_init_complete(mkMedia);
    }

    /**
     * 开始测试视频
     */
    public void startTestVideo() {
        new Thread(() -> {
            createVideoFrame();
        }).start();
        new Thread(() -> {
            sendVideoFrame();
        }).start();
    }

    /**
     * 创建视频帧
     */
    private void createVideoFrame() {
        int timebase = 1000 / param.getFps();
        while (mkMedia != null) {
            long startTime = System.currentTimeMillis();
            YUV420PBarGenerator.YUV420PFrame yuv420PFrame = YUV420PBarGenerator.generateBarFrame(param.getWidth(), param.getHeight());
            if (yPointer == null) {
                yPointer = new Memory((long) yuv420PFrame.getHeight() * yuv420PFrame.getLinesize()[0]);
            }
            if (uPointer == null) {
                uPointer = new Memory((long) yuv420PFrame.getHeight() * yuv420PFrame.getLinesize()[1] / 2);
            }
            if (vPointer == null) {
                vPointer = new Memory((long) yuv420PFrame.getHeight() * yuv420PFrame.getLinesize()[2] / 2);
            }
            yPointer.write(0, yuv420PFrame.getYData(), 0, yuv420PFrame.getHeight() * yuv420PFrame.getLinesize()[0]);
            uPointer.write(0, yuv420PFrame.getUData(), 0, yuv420PFrame.getHeight() * yuv420PFrame.getLinesize()[1] / 2);
            vPointer.write(0, yuv420PFrame.getVData(), 0, yuv420PFrame.getHeight() * yuv420PFrame.getLinesize()[2] / 2);
            linesize = yuv420PFrame.getLinesize();
            while (true) {
                long diffTime = System.currentTimeMillis() - startTime;
                if (diffTime < timebase) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                    }
                } else {
                    break;
                }
            }
            frameQueue.offer(pts);
            pts += timebase;
        }
    }

    /**
     * 发送帧
     */
    private void sendVideoFrame() {
        while (mkMedia != null) {
            try {
                Long ptsNow = frameQueue.poll(5, TimeUnit.SECONDS);
                synchronized (this) {
                    if (mkMedia != null) {
                        ZLM_API.mk_media_input_yuv(mkMedia, new Pointer[]{yPointer, uPointer, vPointer}, linesize, ptsNow);
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    }

    /**
     * 关闭视频
     */
    public void closeVideo() {
        synchronized (this) {
            if (mkMedia != null) {
                ZLM_API.mk_media_release(mkMedia);
                mkMedia = null;
            }
            if (yPointer != null) {
                ((Memory) yPointer).close();
            }
            if (uPointer != null) {
                ((Memory) uPointer).close();
            }
            if (vPointer != null) {
                ((Memory) vPointer).close();
            }
        }
    }

}
