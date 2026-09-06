package com.projectmatch.service;

import com.projectmatch.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED = new HashSet<String>(Arrays.asList(
            "zip", "rar", "7z", "md", "txt", "pdf", "doc", "docx", "sql", "png", "jpg", "jpeg"
    ));

    private final Path root;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public StoredFile store(Long projectId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的文件");
        }
        String original = file.getOriginalFilename() == null ? "file.bin" : file.getOriginalFilename();
        String ext = extension(original);
        if (!ALLOWED.contains(ext)) {
            throw new BizException("不支持的文件类型：" + ext);
        }
        try {
            Path dir = root.resolve(String.valueOf(projectId));
            Files.createDirectories(dir);
            String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(original, root.relativize(target).toString().replace('\\', '/'), file.getSize());
        } catch (IOException e) {
            throw new BizException("文件保存失败");
        }
    }

    public Path resolve(String relative) {
        Path path = root.resolve(relative).normalize();
        if (!path.startsWith(root)) {
            throw new BizException("非法文件路径");
        }
        return path;
    }

    public byte[] read(String relative) {
        try {
            Path path = resolve(relative);
            if (!Files.exists(path)) {
                throw new BizException("文件不存在或已被移除");
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new BizException("读取文件失败");
        }
    }

    public void deleteQuietly(String relative) {
        if (relative == null || relative.isEmpty()) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(relative));
        } catch (Exception ignored) {
        }
    }

    private static String extension(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) {
            return "";
        }
        return name.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    public static class StoredFile {
        private final String fileName;
        private final String filePath;
        private final long fileSize;

        public StoredFile(String fileName, String filePath, long fileSize) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.fileSize = fileSize;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public long getFileSize() {
            return fileSize;
        }
    }
}
