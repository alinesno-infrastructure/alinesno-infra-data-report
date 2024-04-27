package com.alinesno.infra.data.report.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaConfig {

    //适用多台kafka服务器
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap;

    public String getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(String bootstrap) {
        this.bootstrap = bootstrap;
    }
}

