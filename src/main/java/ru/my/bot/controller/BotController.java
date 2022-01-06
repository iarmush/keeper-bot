package ru.my.bot.controller;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import ru.my.bot.service.AttachmentService;

@Path("/api/keeper-bot")
public class BotController {

    private AttachmentService attachmentService;

    public BotController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GET
    @Path("/download")
    @Produces("application/zip")
    public Response download(@NotNull @QueryParam("chat_id") Long chatId) {
        return attachmentService.downloadFiles(chatId);
    }
}