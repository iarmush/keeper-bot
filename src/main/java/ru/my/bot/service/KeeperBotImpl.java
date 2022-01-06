package ru.my.bot.service;

import io.quarkus.logging.Log;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.inject.Singleton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.my.bot.config.BotConfig;

public class KeeperBotImpl {

    private BotConfig botConfig;
    private FileDataService fileDataService;

    public KeeperBotImpl(BotConfig botConfig, FileDataService fileDataService) {
        this.botConfig = botConfig;
        this.fileDataService = fileDataService;
    }

    @Singleton
    public TelegramLongPollingBot telegramLongPollingBot() {
        return new TelegramLongPollingBot() {
            @Override
            public String getBotToken() {
                return botConfig.token();
            }

            @Override
            public void onUpdateReceived(Update update) {
                try {
                    Message message = update.getMessage();
                    Long chatId = message.getChatId();
                    Log.info("Message in chatId: " + chatId);

                    if (message.hasText()) {
                        if (message.getText().equals(BotCommand.START.getName())) {
                            this.execute(SendMessage.builder().text(
                                """
                                    Hello! It's KeeperBot.
                                    Send me file, photo, or another media.
                                    Then text me /download for external link for downloading.
                                         """
                            ).chatId(String.valueOf(chatId)).build());
                        } else if (message.getText().equals(BotCommand.DOWNLOAD.getName())) {
                            var sendMessage = new SendMessage();
                            var link = "http://localhost:3006/?chat_id=" + chatId;
                            // TODO: 08.01.2022 doesn't work with ip host
/*
                            var parse = "Click to download [DOWNLOAD](" + link + ")";
                            sendMessage.enableMarkdown(true);
                            sendMessage.setParseMode("MarkdownV2");
 */
                            sendMessage.setText("Copy and paste this link to your browser: " + link);
                            sendMessage.setChatId(String.valueOf(chatId));

                            this.execute(sendMessage);
                        }
                    } else if (message.getDocument() != null) {
                        String fileName = message.getDocument().getFileName();
                        String fileId = message.getDocument().getFileId();
                        String filePath = execute(new GetFile(fileId)).getFilePath();

                        fileDataService.storeFileDataToMap(chatId, new FileData(fileName, filePath));
                    } else if (message.getPhoto() != null) {
                        for (PhotoSize photo : message.getPhoto()) {
                            String fileId = photo.getFileId();
                            String filePath = execute(new GetFile(fileId)).getFilePath();

                            fileDataService.storeFileDataToMap(chatId,
                                new FileData("photo_" +
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + ".jpeg",
                                    filePath));
                        }
                    }
                } catch (
                    TelegramApiException e) {
                    Log.error("Error while receive update");
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getBotUsername() {
                return botConfig.username();
            }
        };
    }
}
