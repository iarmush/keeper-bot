package ru.my.bot.controller;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.jboss.logging.Logger;
import ru.my.bot.service.LogDataService;
import ru.my.bot.service.MinioService;

@Path("")
public class BotController {

    private static final Logger LOGGER = Logger.getLogger(BotController.class);

    private final MinioService minioService;
    private final LogDataService logDataService;

    public BotController(MinioService minioService, LogDataService logDataService) {
        this.minioService = minioService;
        this.logDataService = logDataService;
    }

    @GET
    @Path("/api/keeper-bot/download")
    @Produces("application/zip")
    public Response downloadZip(@NotNull @QueryParam("chatId") Long chatId) {
        LOGGER.infof("Attempt to download in %s", chatId);
        byte[] bytes = minioService.downloadZip(String.valueOf(chatId));
        logDataService.save(chatId);
        return Response.ok(bytes)
            .header("Content-Disposition", "attachment; filename=keeper_bot.zip")
            .build();
    }
}