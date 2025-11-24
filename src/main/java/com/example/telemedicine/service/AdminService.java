package com.example.telemedicine.service;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.management.OperatingSystemMXBean;

@Service
public class AdminService {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private int pid;
    @Getter
    private Instant startTime;

    public synchronized void start() {
        if (running.get()) return;

        this.pid = ProcessHandle.current().pid() > Integer.MAX_VALUE ? -1 : (int) ProcessHandle.current().pid();
        this.startTime = Instant.now();
        running.set(true);

        System.out.println("Server started, PID: " + pid);
    }

    public synchronized void stop() {
        if (!running.get()) return;

        running.set(false);
        System.out.println("Server stopped, PID: " + pid);
    }

    public boolean isRunning() {
        return running.get();
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
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return osBean.getProcessCpuLoad() * 100;
    }

    public int getThreadCount() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return threadBean.getThreadCount();
    }

}