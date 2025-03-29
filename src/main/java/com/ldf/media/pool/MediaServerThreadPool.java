package com.ldf.media.pool;


import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MediaServer模块线程池配置
 *
 * @author lidaofu
 * @since 2023/4/11
 **/
public class MediaServerThreadPool {

    /**
     * MediaServer模块线程池
     */
    private final static ThreadPoolExecutor SYSTEM_THREAD_POOL = new ThreadPoolExecutor(
            32, 64, 60, TimeUnit.SECONDS
            , new LinkedBlockingDeque<>(256), new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "MediaServerThread-" + threadNumber.getAndIncrement());
        }
    });

    /**
     * 执行
     *
     * @param runnable
     */
    public static void execute(Runnable runnable) {
        SYSTEM_THREAD_POOL.execute(runnable);
    }

    /**
     * 停止
     */
    public static void shutdown() {
        SYSTEM_THREAD_POOL.shutdown();
    }
}
