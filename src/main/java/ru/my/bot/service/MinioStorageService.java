package ru.my.bot.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import io.quarkus.logging.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MinioStorageService {

    private static final Logger LOG = Logger.getLogger(MinioStorageService.class);

    private final MinioClient minioClient;

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void uploadFile(String bucketName, String fileName, byte[] bytes) {
        try {
            createBucketIfNotExist(bucketName);
            var putObjectArgs = PutObjectArgs.builder()
                .object(fileName)
                .bucket(bucketName)
                .stream(new ByteArrayInputStream(bytes), bytes.length, -1L)
                .build();

            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            LOG.errorv(e, "Error while uploading file: {} to bucket: {}", fileName, bucketName);
            throw new RuntimeException(e);
        }
    }

    public Response downloadZip(String bucketName) {
        List<String> objectNameList = getObjectNamesInBucket(bucketName);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (String objectName : objectNameList) {
                    byte[] bytes = getFileAsBytes(bucketName, objectName);

                    ZipEntry entry = new ZipEntry(objectName);
                    entry.setSize(bytes.length);
                    zos.putNextEntry(entry);
                    zos.write(bytes);
                    zos.closeEntry();
                }
            }
            return Response.ok(baos.toByteArray())
                .header("Content-Disposition", "attachment; filename=keeper_bot.zip")
                .build();
        } catch (Exception e) {
            LOG.errorv(e, "Error while writing to zip file in bucket: {}", bucketName);
            throw new RuntimeException(e);
        }
    }

    public void deleteFilesAndBucket(String bucketName) {
        removeFiles(bucketName);
        removeBucket(bucketName);
    }

    private void removeFiles(String bucketName) {
        List<String> objectNameList = getObjectNamesInBucket(bucketName);
        List<DeleteObject> deleteObjects = new LinkedList<>();
        objectNameList.forEach(objectName -> deleteObjects.add(new DeleteObject(objectName)));

        try {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder().bucket(bucketName).objects(deleteObjects).build());

            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                System.out.println(
                    "Error in deleting object " + error.objectName() + "; " + error.message());
            }
        } catch (Exception e) {
            LOG.errorv(e, "Error while removing bucket: {}", bucketName);
            throw new RuntimeException(e);
        }
    }

    private void removeBucket(String bucketName) {
        try {
            if (isBucketExists(bucketName)) {
                minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            } else {
                LOG.errorv("Attempt to remove not existed bucket: {}", bucketName);
                throw new RuntimeException("Bucket doesn't exist");
            }
        } catch (Exception e) {
            LOG.errorv(e, "Error while removing bucket: {}", bucketName);
            throw new RuntimeException(e);
        }
    }

    private byte[] getFileAsBytes(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName).build()).readAllBytes();
        } catch (Exception e) {
            LOG.errorv(e, "Error while getting object: {}  from bucket: {}", objectName, bucketName);
            throw new RuntimeException(e);
        }
    }

    private List<String> getObjectNamesInBucket(String bucketName) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());

        return StreamSupport.stream(results.spliterator(), false)
            .map(itemResult -> {
                try {
                    return itemResult.get();
                } catch (Exception e) {
                    LOG.errorv(e, "Error while getting result: {} for bucket: {}", itemResult, bucketName);
                    throw new RuntimeException(e);
                }
            })
            .map(Item::objectName)
            .collect(Collectors.collectingAndThen(Collectors.toList(), result -> {
                if (result.isEmpty()) {
                    Log.error("FileData is empty, please send medias: " + bucketName);
                    throw new RuntimeException("FileData is empty, please send media");
                }
                return result;
            }));
    }

    private void createBucketIfNotExist(String bucketName) {
        try {
            if (!isBucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            }
        } catch (Exception e) {
            LOG.errorv(e, "Error while creating bucket with name: {}", bucketName);
            throw new RuntimeException(e);
        }
    }

    private boolean isBucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().
                bucket(bucketName)
                .build());
        } catch (Exception e) {
            LOG.errorv(e, "Error while checking is bucket exists: {}", bucketName);
            throw new RuntimeException(e);
        }
    }
}
