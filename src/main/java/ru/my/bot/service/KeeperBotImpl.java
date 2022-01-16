package ru.my.bot.service;

import java.io.IOException;
import javax.inject.Singleton;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.my.bot.config.BotConfig;

public record KeeperBotImpl(BotConfig botConfig, MessageService messageService) {

    private static final Logger LOGGER = Logger.getLogger(KeeperBotImpl.class);

    @Singleton
    public TelegramLongPollingBot telegramLongPollingBot() {
        return new TelegramLongPollingBot() {
            @Override
            public String getBotUsername() {
                return botConfig.username();
            }

            @Override
            public String getBotToken() {
                return botConfig.token();
            }

            @Override
            public void onUpdateReceived(Update update) {
                try {
                    var message = update.getMessage();
                    if (message.hasText()) {
                        messageService.handleText(message);
                    } else if (message.hasDocument() || message.hasPhoto() || message.hasVideo()) {
                        messageService.handleMedia(message);
                    } else {
                        execute(SendMessage.builder().text("Please send medias").build());
                    }
                } catch (TelegramApiException | IOException e) {
                    LOGGER.errorf(e, "Error while receive update");
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
