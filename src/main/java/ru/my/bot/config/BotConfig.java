package ru.my.bot.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "bot")
public interface BotConfig {

    String username();

    String token();
}
