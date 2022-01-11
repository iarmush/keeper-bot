package ru.my.bot.service;

import io.quarkus.logging.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@ApplicationScoped
public class AttachmentService {

    private FileDataService fileDataService;
    private TelegramLongPollingBot telegramLongPollingBot;

    public AttachmentService(FileDataService fileDataService, TelegramLongPollingBot telegramLongPollingBot) {
        this.fileDataService = fileDataService;
        this.telegramLongPollingBot = telegramLongPollingBot;
    }

    public Response downloadFiles(Long chatId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        ArrayList<FileData> fileDataList = fileDataService.getFileMap().get(chatId);
        checkFileDataIsNullOrEmpty(chatId, fileDataList);
        Map<byte[], FileData> bytes = computeFileDataMap(fileDataList);

        try {
            bytes.forEach((k, v) -> {
                ZipEntry entry = new ZipEntry(v.fileName());
                entry.setSize(k.length);
                try {
                    zos.putNextEntry(entry);
                    zos.write(k);
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

    private Map<byte[], FileData> computeFileDataMap(ArrayList<FileData> fileDataList) {
        Map<Future<byte[]>, FileData> submit = new HashMap<>();
        var executor = Executors.newFixedThreadPool(3);
        for (FileData data : fileDataList) {
            submit.put(executor.submit(() -> telegramLongPollingBot.downloadFileAsStream(data.filePath()).readAllBytes()), data);
        }

        Map<byte[], FileData> bytes = new HashMap<>();
        submit.forEach((k, v) -> {
            try {
                bytes.put(k.get(), v);
            } catch (InterruptedException | ExecutionException e) {
                Log.error("Error while computeFileDataMa: " + v.fileName());
                throw new RuntimeException(e);
            }
        });
        return bytes;
    }

    private void checkFileDataIsNullOrEmpty(Long chatId, ArrayList<FileData> fileDataList) {
        if (fileDataList == null || fileDataList.isEmpty()) {
            Log.error("FileData is empty, please send media: " + chatId);
            throw new RuntimeException("FileData is empty, please send media");
        }
    }
}
