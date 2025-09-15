package com.ldf.media.module.test;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class SystemMonitor {

    // 保存上一次CPU时间用于计算CPU占用率
    private static long lastSystemTime = 0;
    private static long lastProcessCpuTime = 0;

    /**
     * 获取当前JVM进程的CPU占用率
     *
     * @return CPU占用率，范围0.0-100.0
     */
    public static float getProcessCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        long currentProcessCpuTime = osBean.getProcessCpuTime();
        long currentSystemTime = System.nanoTime();

        // 初始情况，返回0
        if (lastSystemTime == 0) {
            lastSystemTime = currentSystemTime;
            lastProcessCpuTime = currentProcessCpuTime;
            return 0.0f;
        }

        long elapsedCpu = currentProcessCpuTime - lastProcessCpuTime;
        long elapsedTime = currentSystemTime - lastSystemTime;


        // 更新上一次的时间
        lastSystemTime = currentSystemTime;
        lastProcessCpuTime = currentProcessCpuTime;

        // 获取CPU核心数
        int availableProcessors = Runtime.getRuntime().availableProcessors();
// 计算单核心视角的CPU占用率（限制在100%以内）
        float cpuUsage = (elapsedCpu / (float) elapsedTime / availableProcessors) * 100;
        return cpuUsage;
    }

    /**
     * 获取系统RAM内存占用率
     *
     * @return 内存占用率，范围0.0-100.0
     */
    public static float getMemoryUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        // 总物理内存
        long totalMemory = osBean.getTotalPhysicalMemorySize();
        // 可用物理内存
        long freeMemory = osBean.getFreePhysicalMemorySize();
        // 已使用内存
        long usedMemory = totalMemory - freeMemory;

        // 计算内存占用率
        return (usedMemory / (float) totalMemory) * 100;
    }
}
