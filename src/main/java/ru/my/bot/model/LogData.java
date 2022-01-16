package ru.my.bot.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.bson.codecs.pojo.annotations.BsonProperty;

@MongoEntity(collection = "fileData")
public class LogData extends PanacheMongoEntity {

    @BsonProperty("chatId")
    @NotNull
    private Long chatId;

    @NotEmpty
    @BsonProperty("fileName")
    private String fileName;

    @NotEmpty
    @BsonProperty("filePath")
    private String filePath;

    @BsonProperty("bytes")
    private byte[] bytes;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
