package ru.my.bot.service;

import io.quarkus.logging.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

        try {
            fileDataService.getFileMap().get(chatId).forEach(fileData -> {
                try {
                    byte[] bytes = telegramLongPollingBot.downloadFileAsStream(fileData.filePath()).readAllBytes();
                    ZipEntry entry = new ZipEntry(fileData.fileName());
                    entry.setSize(bytes.length);
                    try {
                        zos.putNextEntry(entry);
                        zos.write(bytes);
                        zos.closeEntry();
                    } catch (IOException e) {
                        Log.error("Error while close zos entry", e);
                        throw new RuntimeException(e);
                    }
                } catch (IOException | TelegramApiException e) {
                    e.printStackTrace();
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
}
