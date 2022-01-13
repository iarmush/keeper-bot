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

    private FileDataRepository fileDataRepository;

    public FileDataService(FileDataRepository fileDataRepository) {
        this.fileDataRepository = fileDataRepository;
    }

    public void saveFileData(String fileName, String filePath, Long chatId, byte[] bytes) {
        FileData fileData = new FileData();
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
        List<FileData> fileDataList = fileDataRepository.findByChatId(chatId);
        checkFileDataIsNullOrEmpty(chatId, fileDataList);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            fileDataList.forEach(fileData -> {
                ZipEntry entry = new ZipEntry(fileData.getFileName());
                entry.setSize(fileData.getBytes().length);
                try {
                    zos.putNextEntry(entry);
                    zos.write(fileData.getBytes());
                    zos.closeEntry();
                } catch (IOException e) {
                    Log.error("Error while close zos entry", e);
                    throw new RuntimeException(e);
                }
            });

            zos.close();
        } catch (IOException e) {
            Log.error("Error while close zos", e);
            throw new RuntimeException(e);
        }

        return Response.ok(baos.toByteArray())
            .header("Content-Disposition", "attachment; filename=keeper_bot.zip")
            .build();
    }

    private void checkFileDataIsNullOrEmpty(Long chatId, List<FileData> fileDataList) {
        if (fileDataList == null || fileDataList.isEmpty()) {
            Log.error("FileData is empty, please send media: " + chatId);
            throw new RuntimeException("FileData is empty, please send media");
        }
    }
}
