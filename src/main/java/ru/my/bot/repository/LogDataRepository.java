package ru.my.bot.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import ru.my.bot.model.LogData;

@ApplicationScoped
public class LogDataRepository implements PanacheMongoRepository<LogData> {

    public List<LogData> findByChatId(Long chatId) {
        return list("chatId", chatId);
    }

    public void deleteByChatId(Long chatId) {
        delete("chatId", chatId);
    }
}