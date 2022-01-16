package ru.my.bot.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;
import ru.my.bot.model.LogData;

@ApplicationScoped
public class LogDataRepository implements PanacheMongoRepository<LogData> {

}