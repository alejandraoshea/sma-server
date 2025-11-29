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

/**
 * Service responsible for administrative operations on the Telemedicine server.
 * Provides methods to stop the server, check if its running, retrieve uptime,
 * monitor CPU and memory usage, and get thread count.
 */
@Service
public class AdminService {

    private final String secretPassword;

    @Getter
    private final Instant startTime = Instant.ofEpochMilli(
            ManagementFactory.getRuntimeMXBean().getStartTime()
    );

    @Autowired
    private ApplicationContext context;

    /**
     * Constructs an AdminService with the specified secret password and Spring application context.
     *
     * @param secretPassword the operator password used for administrative actions
     * @param context        the Spring application context, used for clean shutdown
     */
    public AdminService(
            @Value("${operator.password}") String secretPassword,
            ApplicationContext context
    ) {
        this.secretPassword = secretPassword;
        this.context = context;
    }

    /**
     * Stops the server if the provided password matches the secret password.
     * Performs a clean shutdown using SpringApplication.exit.
     *
     * @param password the password for authorization
     */
    public void stop(String password) {
        if (!password.equals(secretPassword)) return;

        System.out.println("Stopping server...");

        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }

            SpringApplication.exit(context, () -> 0);
        }).start();
    }

    /**
     * Checks if the server is currently running.
     *
     * @return true if the server uptime is greater than zero, false otherwise
     */
    public boolean isRunning() {
        return ManagementFactory.getRuntimeMXBean().getUptime() > 0;
    }

    /**
     * Returns a human-readable string representing the server uptime.
     *
     * @return a string in the format "HHh MMm SSs"
     */
    public String getUptime() {
        Duration duration = Duration.between(startTime, Instant.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }

    /**
     * Returns the amount of memory currently used by the JVM in bytes.
     *
     * @return used memory in bytes
     */
    public long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Returns the maximum memory available to the JVM in bytes.
     *
     * @return maximum memory in bytes
     */
    public long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Returns the current CPU load of the process as a percentage.
     *
     * @return CPU load percentage, or 0 if not available
     */
    public double getCpuLoad() {
        java.lang.management.OperatingSystemMXBean baseOsBean = ManagementFactory.getOperatingSystemMXBean();
        if (baseOsBean instanceof com.sun.management.OperatingSystemMXBean osBean) {
            double load = osBean.getProcessCpuLoad();
            return load < 0 ? 0 : load * 100;
        }
        return 0;
    }

    /**
     * Returns the number of threads currently running in the JVM.
     *
     * @return thread count
     */
    public int getThreadCount() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return threadBean.getThreadCount();
    }

}