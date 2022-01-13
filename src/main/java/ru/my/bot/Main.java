package ru.my.bot;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@QuarkusMain
public class Main {

    public static void main(String... args) {
        Quarkus.run(MyApp.class, args);
    }

    public record MyApp(TelegramLongPollingBot telegramLongPollingBot) implements QuarkusApplication {

        @Override
        public int run(String... args) {
            try {
                TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
                telegramBotsApi.registerBot(telegramLongPollingBot);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            Quarkus.waitForExit();
            return 0;
        }
    }
}
