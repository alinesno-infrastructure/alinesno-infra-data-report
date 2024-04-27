package com.alinesno.infra.data.report.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MinioConfig {
    /**
     * 服务地址
     */
    @Value("${minio.endpoint}")
    private String url;

    /**
     * 服务地址
     */
    @Value("${minio.port}")
    private String port;

    /**
     * 用户名
     */
    @Value("${minio.accesskey}")
    private String accessKey;

    /**
     * 密码
     */
    @Value("${minio.secretkey}")
    private String secretKey;

    /**
     * 存储桶名称
     */
    @Value("${minio.bucketname}")
    private String bucketName;

    @Value("${minio.local-storage-path}")
    private String localPath;

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey()
    {
        return secretKey;
    }

    public void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Bean
    public MinioClient getMinioClient()
    {
        return MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
    }


}
