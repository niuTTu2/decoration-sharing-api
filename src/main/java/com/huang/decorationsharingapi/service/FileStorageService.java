package com.huang.decorationsharingapi.service;

import com.huang.decorationsharingapi.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.allowed-types}")
    private String allowedTypesStr;

    @Value("${file.max-size}")
    private long maxFileSize;

    private Path fileStorageLocation;
    private List<String> allowedTypes;

    @PostConstruct
    public void init() {
        this.allowedTypes = Arrays.asList(allowedTypesStr.split(","));
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("无法创建文件上传目录", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        validateFile(file);

        // 生成唯一文件名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // 创建目标路径
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("无法存储文件 " + fileName, ex);
        }
    }

    public String createThumbnail(MultipartFile file) {
        validateFile(file);

        // 生成唯一文件名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String thumbFileName = "thumb_" + UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // 读取原图并创建缩略图
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            int thumbWidth = 300;
            int thumbHeight = (int) (originalImage.getHeight() * (thumbWidth / (double) originalImage.getWidth()));

            BufferedImage thumbnail = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = thumbnail.createGraphics();
            g.drawImage(originalImage.getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH), 0, 0, null);
            g.dispose();

            // 保存缩略图
            Path targetLocation = this.fileStorageLocation.resolve(thumbFileName);
            ImageIO.write(thumbnail, fileExtension, targetLocation.toFile());

            return thumbFileName;
        } catch (IOException ex) {
            throw new FileStorageException("无法创建缩略图 " + thumbFileName, ex);
        }
    }

    private void validateFile(MultipartFile file) {
        // 检查文件名
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName.contains("..")) {
            throw new FileStorageException("文件名包含无效路径序列 " + fileName);
        }

        // 检查文件类型
        if (!allowedTypes.contains(file.getContentType())) {
            throw new FileStorageException("不支持的文件类型: " + file.getContentType() + "，仅支持: " + allowedTypesStr);
        }

        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new FileStorageException("文件大小超过限制 " + (maxFileSize / 1024 / 1024) + "MB");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "jpg"; // 默认扩展名
        }
    }

    // 在现有的 FileStorageService 类中添加此方法

    /**
     * 存储用户头像文件
     */
    public String storeAvatar(MultipartFile file) {
        validateFile(file);

        // 生成唯一文件名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String fileName = "avatar_" + UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // 确保头像目录存在
            Path avatarStorageLocation = this.fileStorageLocation.resolve("avatars");
            Files.createDirectories(avatarStorageLocation);

            // 创建目标路径
            Path targetLocation = avatarStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "avatars/" + fileName;
        } catch (IOException ex) {
            throw new FileStorageException("无法存储头像文件 " + fileName, ex);
        }
    }
}