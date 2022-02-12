package ru.my.bot.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "minio")
public interface MinioConfig {

    boolean useSsl();

    String host();

    int port();

    String accessKey();

    String secretKey();
}
