package com.example.telemedicine.service;

import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AdminService {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final String secretPassword;
    private int pid;
    @Getter
    private final Instant startTime = Instant.ofEpochMilli(
            ManagementFactory.getRuntimeMXBean().getStartTime()
    );
    @Autowired
    private ApplicationContext context;

    public AdminService(
            @Value("${operator.password}") String secretPassword,
            ApplicationContext context
    ) {
        this.secretPassword = secretPassword;
        this.context = context;
    }

    public void stop(String password) {
        if (!password.equals(secretPassword)) return;

        System.out.println("Stopping server...");
        SpringApplication.exit(context, () -> 0);
    }

    public boolean isRunning() {
        return ManagementFactory.getRuntimeMXBean().getUptime() > 0;
    }

    public String getUptime() {
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