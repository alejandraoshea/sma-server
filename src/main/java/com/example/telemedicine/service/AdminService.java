package com.example.telemedicine.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AdminService {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private int pid;
    @Getter
    private Instant startTime;

    @PostConstruct
    public void markAsRunning() {
        running.set(true);
        startTime = Instant.now();
    }

    public synchronized void stop() {
        if (!running.get()) return;

        running.set(false);

        try {
            String scriptPath = "scripts/stop-server.sh";

            ProcessBuilder pb = new ProcessBuilder(scriptPath);
            pb.inheritIO();
            Process process = pb.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Server stopped successfully via script, PID: " + pid);
            } else {
                System.err.println("Script exited with code: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to stop server via script, PID: " + pid);
        }
    }

    public boolean isRunning() {
        return ManagementFactory.getRuntimeMXBean().getUptime() > 0;
    }

    public String getUptime() {
        if (!running.get() || startTime == null) return "N/A";
        Duration duration = Duration.between(startTime, Instant.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }

    public long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public double getCpuLoad() {
        java.lang.management.OperatingSystemMXBean baseOsBean = ManagementFactory.getOperatingSystemMXBean();
        if (baseOsBean instanceof com.sun.management.OperatingSystemMXBean osBean) {
            double load = osBean.getProcessCpuLoad();
            return load < 0 ? 0 : load * 100;
        }
        return 0;
    }

    public int getThreadCount() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return threadBean.getThreadCount();
    }

}