package ru.my.bot.controller;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import ru.my.bot.service.MinioStorageService;

@Path("/api/keeper-bot")
public class BotController {

    private final MinioStorageService minIOStorageService;

    public BotController(MinioStorageService minIOStorageService) {
        this.minIOStorageService = minIOStorageService;
    }

    @GET
    @Path("/download")
    @Produces("application/zip")
    public Response downloadZip(@NotNull @QueryParam("chat_id") Long chatId) {
        return minIOStorageService.downloadZip(String.valueOf(chatId));
    }
}