package com.alinesno.infra.data.report.gateway.thread;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReportSubmitTaskManager {

    private Map<String, ReportSubmitTask> taskMap;

    public ReportSubmitTaskManager() {
        taskMap = new ConcurrentHashMap<>();
    }

    public void addTask(String fileID, ReportSubmitTask task) {
        taskMap.put(fileID, task);
    }

    public ReportSubmitTask getTask(String fileID) {
        return taskMap.get(fileID);
    }

    public void removeTask(String fileID) {
        taskMap.remove(fileID);
    }

    public boolean containsTask(String fileID) {
        return taskMap.containsKey(fileID);
    }
}
