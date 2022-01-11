package ru.my.bot.service;

public enum BotCommand {
    START("/start"),
    DOWNLOAD("/download"),
    CLEAR("/clear");

    private final String name;

    BotCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
