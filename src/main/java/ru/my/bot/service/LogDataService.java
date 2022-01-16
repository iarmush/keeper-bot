package ru.my.bot.service;

import javax.enterprise.context.ApplicationScoped;
import ru.my.bot.repository.LogDataRepository;

@ApplicationScoped
public class LogDataService {

    private final LogDataRepository logDataRepository;

    public LogDataService(LogDataRepository logDataRepository) {
        this.logDataRepository = logDataRepository;
    }
}
