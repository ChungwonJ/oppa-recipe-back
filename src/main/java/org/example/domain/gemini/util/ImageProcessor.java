package org.example.domain.gemini.util;

import org.example.global.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Component
public class ImageProcessor {

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "파일이 존재하지 않습니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !isJpgFamily(file))) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다.");
        }
    }

    public String getMimeType(MultipartFile file) {
        String fileName = (file.getOriginalFilename() != null) ? file.getOriginalFilename().toLowerCase() : "";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".webp")) return "image/webp";
        if (fileName.endsWith(".heic")) return "image/heic";
        // JFIF를 포함한 JPEG 계열 통합
        if (isJpgFamily(file)) return "image/jpeg";
        return "image/jpeg";
    }

    private boolean isJpgFamily(MultipartFile file) {
        String fileName = file.getOriginalFilename().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".jfif");
    }

    public String encodeToBase64(MultipartFile file) {
        try {
            return Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 인코딩에 실패했습니다.");
        }
    }
}
