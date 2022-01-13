package ru.my.bot.service;

import io.quarkus.logging.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import ru.my.bot.model.FileData;
import ru.my.bot.repository.FileDataRepository;

@ApplicationScoped
public class FileDataService {

    private final FileDataRepository fileDataRepository;

    public FileDataService(FileDataRepository fileDataRepository) {
        this.fileDataRepository = fileDataRepository;
    }

    public void saveFileData(String fileName, String filePath, Long chatId, byte[] bytes) {
        var fileData = new FileData();
        fileData.setFileName(fileName);
        fileData.setFilePath(filePath);
        fileData.setChatId(chatId);
        fileData.setBytes(bytes);

        fileDataRepository.persist(fileData);
    }

    public void deleteFileData(Long chatId) {
        fileDataRepository.deleteByChatId(chatId);
    }

    public Response downloadFilesByChatId(Long chatId) {
        var fileDataList = fileDataRepository.findByChatId(chatId);
        checkFileDataIsNullOrEmpty(chatId, fileDataList);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (FileData fileData : fileDataList) {
                    ZipEntry entry = new ZipEntry(fileData.getFileName());
                    entry.setSize(fileData.getBytes().length);
                    zos.putNextEntry(entry);
                    zos.write(fileData.getBytes());
                    zos.closeEntry();
                }
            }
            return Response.ok(baos.toByteArray())
                .header("Content-Disposition", "attachment; filename=keeper_bot.zip")
                .build();
        } catch (IOException e) {
            Log.error("Error while close baos", e);
            throw new RuntimeException(e);
        }
    }

    private void checkFileDataIsNullOrEmpty(Long chatId, List<FileData> fileDataList) {
        if (fileDataList == null || fileDataList.isEmpty()) {
            Log.error("FileData is empty, please send medias: " + chatId);
            throw new RuntimeException("FileData is empty, please send media");
        }
    }
}
