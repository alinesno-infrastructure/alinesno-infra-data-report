package com.alinesno.infra.data.report.gateway.thread;

import org.springframework.stereotype.Component;

@Component
public interface ReportSubmitCallback {
    void onComplete();
    void onException(Exception e);
}
