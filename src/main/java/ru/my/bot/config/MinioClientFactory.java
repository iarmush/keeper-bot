package ru.my.bot.config;

import io.minio.MinioClient;
import javax.inject.Singleton;
import org.jboss.logging.Logger;

public record MinioClientFactory(MinioConfig minIOConfig) {

    private static final Logger LOG = Logger.getLogger(MinioClientFactory.class);

    @Singleton
    public MinioClient minioClient() {
        try {
            return MinioClient.builder()
                .endpoint(
                    minIOConfig.host(),
                    minIOConfig.port(),
                    minIOConfig.useSsl())
                .credentials(
                    minIOConfig.accessKey(),
                    minIOConfig.secretKey())
                .build();
        } catch (Exception e) {
            LOG.errorv(e, "Error while initializing miniClient");
            throw new RuntimeException(e);
        }
    }
}
