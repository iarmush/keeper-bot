package ru.my.bot.service;

import io.quarkus.logging.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FileDataService {

    private final Map<Long, ArrayList<FileData>> fileMap = new HashMap<>();

    public Map<Long, ArrayList<FileData>> getFileMap() {
        return fileMap;
    }

    public void storeFileDataToMap(Long chatId, FileData fileData) {
        if (fileMap.get(chatId) == null) {
            fileMap.put(chatId, new ArrayList<>(List.of((fileData))));
        } else {
            checkDuplicateFileName(chatId, fileData);
            fileMap.get(chatId).add(fileData);
        }
    }

    private void checkDuplicateFileName(Long chatId, FileData fileData) {
        fileMap.get(chatId).forEach(data -> {
            if (data.fileName().equals(fileData.fileName())) {
                Log.error("Duplicate file name");
                throw new RuntimeException("Duplicate file name");
            }
        });
    }
}
