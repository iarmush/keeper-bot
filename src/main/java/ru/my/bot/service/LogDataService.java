package ru.my.bot.service;

import java.time.LocalDateTime;
import javax.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import ru.my.bot.model.LogData;
import ru.my.bot.repository.LogDataRepository;

@ApplicationScoped
public class LogDataService {

    private static final Logger LOGGER = Logger.getLogger(KeeperBotImpl.class);

    private final LogDataRepository logDataRepository;

    public LogDataService(LogDataRepository logDataRepository) {
        this.logDataRepository = logDataRepository;
    }

    public void save(Long chatId) {
        LOGGER.infof("Attempt to save in %s", chatId);
        var logData = new LogData();
        logData.setChatId(chatId);
        logData.setDownloadDate(LocalDateTime.now());
        logDataRepository.persist(logData);
    }
}
