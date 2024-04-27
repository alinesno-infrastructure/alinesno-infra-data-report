package com.alinesno.infra.data.report.gateway.thread;

import org.springframework.stereotype.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ReportSubmitThreadPool {
    private static final int MAX_THREADS = 150;
    private ExecutorService executorService;

    public ReportSubmitThreadPool() {
        executorService = Executors.newFixedThreadPool(MAX_THREADS);
    }

    public void submit(ReportSubmitTask task) {
        executorService.submit(task);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
