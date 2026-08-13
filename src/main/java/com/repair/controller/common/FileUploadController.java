package com.repair.controller.common;

import com.repair.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String newName = UUID.randomUUID().toString().replace("-", "") + ext;
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            File dateFolder = new File(UPLOAD_DIR + dateDir);
            if (!dateFolder.exists()) {
                dateFolder.mkdirs();
            }

            Path filePath = Paths.get(UPLOAD_DIR, dateDir, newName);
            Files.write(filePath, file.getBytes());

            String url = "/uploads/" + dateDir + "/" + newName;

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("url", url);
            data.put("name", originalName);
            return Result.success(data);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/multi")
    public Result<List<Map<String, Object>>> uploadMulti(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            Result<Map<String, Object>> r = upload(file);
            if (r.getData() != null) {
                results.add(r.getData());
            }
        }
        return Result.success(results);
    }
}
