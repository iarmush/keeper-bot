package ru.my.bot.service;

import io.quarkus.logging.Log;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
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
                    if (message.hasText()) {
                        handleText(message);
                    } else if (message.hasDocument() || message.hasPhoto() || message.hasVideo()) {
                        handleMedia(message);
                    } else {
                        execute(SendMessage.builder().text("Please send medias").build());
                    }
                } catch (TelegramApiException | IOException e) {
                    Log.error("Error while receive update");
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getBotUsername() {
                return botConfig.username();
            }

            private void handleText(Message message) throws TelegramApiException {
                var chatId = message.getChatId();
                Log.info("Handled text in chatId: " + chatId);

                if (message.getText().equals(BotCommand.START.getName())) {
                    execute(SendMessage.builder().text("""
                            Hello! It's KeeperBot.
                            Send me files, photos or videos.
                            After use command /link for getting external link for downloading.
                            """)
                        .chatId(String.valueOf(chatId))
                        .build());
                } else if (message.getText().equals(BotCommand.LINK.getName())) {
                    execute(SendMessage.builder().text("""
                            Use this link for downloading archive:
                            http://localhost:3006/?chat_id=%s
                            """.formatted(chatId))
                        .chatId(String.valueOf(chatId))
                        .build());
                } else if (message.getText().equals(BotCommand.FINISH.getName())) {
                    fileDataService.deleteFileData(chatId);
                    execute(SendMessage.builder().text("KeeperBot is ready for new medias")
                        .chatId(String.valueOf(chatId)).build());
                } else {
                    Log.error("Error while handling text in chatId: " + chatId);
                    throw new RuntimeException("Error while handling text in chatId: " + chatId);
                }
            }

            private void handleMedia(Message message) throws TelegramApiException, IOException {
                var chatId = message.getChatId();
                Log.info("Handled media in chatId: " + chatId);

                String fileName;
                String fileId;
                String filePath;
                if (message.getDocument() != null) {
                    fileName = message.getDocument().getFileName();
                    fileId = message.getDocument().getFileId();
                    filePath = execute(new GetFile(fileId)).getFilePath();
                } else if (message.getPhoto() != null) {
                    fileName = "photo_" + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".jpeg";
                    fileId = Collections.max(message.getPhoto(), Comparator.comparing(PhotoSize::getFileSize)).getFileId();
                    filePath = execute(new GetFile(fileId)).getFilePath();
                } else if (message.getVideo() != null) {
                    fileName = message.getVideo().getFileName();
                    fileId = message.getVideo().getFileId();
                    filePath = execute(new GetFile(fileId)).getFilePath();
                } else {
                    Log.error("Error while handling media in chatId: " + chatId);
                    throw new RuntimeException("Error while handling media in chatId: " + chatId);
                }

                byte[] bytes = downloadFileAsStream(filePath).readAllBytes();
                fileDataService.saveFileData(fileName, filePath, chatId, bytes);
            }
        };
    }
}
