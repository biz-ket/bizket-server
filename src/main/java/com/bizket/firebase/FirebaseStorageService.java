package com.bizket.firebase;

import com.bizket.exception.BizException;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.bizket.exception.BizExceptionType.SERVER_ERROR;
import static com.google.cloud.storage.Acl.Role.READER;

@Slf4j
@RequiredArgsConstructor
@Service
public class FirebaseStorageService {

    private final Bucket bucket;

    private static final String UPLOAD_PATH = "upload/";
    private static final String IMAGE_URL_PREFIX = "https://storage.googleapis.com/";
    private static final String IMAGE_CONTENT_TYPE = "image/";
    private static final String DEFAULT_EXTENSION = "jpg";

    private static final int TARGET_WIDTH = 960;
    private static final int TARGET_HEIGHT = 540;
    private static final float IMAGE_QUALITY = 0.8f;

    private static final Set<String> VALID_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    /**
     * 다중 파일 업로드
     */
    public List<String> uploadAll(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        return files.stream()
            .map(this::uploadNewFile)
            .toList();
    }

    /**
     * 단일 이미지 업로드
     */
    public String uploadNewFile(MultipartFile file) {
        if (isInvalidFile(file)) {
            throw SERVER_ERROR.of("파일이 비어 있습니다.");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        byte[] optimizedImage = optimizeImage(getFileBytes(file), extension);

        return uploadToFirebase(
            optimizedImage,
            generateFileName(file.getOriginalFilename()),
            IMAGE_CONTENT_TYPE + extension
        );
    }

    /**
     * 기존 이미지 교체
     */
    public String updateFile(MultipartFile file, String previousImageUrl) {
        if (isInvalidFile(file)) {
            return previousImageUrl;
        }

        String newFileUrl = uploadNewFile(file);
        if (previousImageUrl != null && !previousImageUrl.isBlank()) {
            deleteFileFromFirebase(previousImageUrl);
        }

        return newFileUrl;
    }

    /**
     * Firebase에 저장된 파일 삭제
     */
    public void deleteFileFromFirebase(String imageUrl) {
        try {
            String fileName = extractFileName(imageUrl);
            Blob blob = bucket.get(fileName);

            if (blob == null) {
                throw SERVER_ERROR.of("파일을 찾을 수 없습니다. URL: " + imageUrl);
            }

            blob.delete();
        } catch (BizException e) {
            log.error("Firebase 파일 삭제 중 오류 발생. URL: {}, 에러: {}", imageUrl, e.getMessage());
        }
    }

    /**
     * 이미지 최적화 (너비/높이 조정, 압축)
     */
    private byte[] optimizeImage(byte[] imageBytes, String extension) {
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            BufferedImage image = ImageIO.read(inputStream);
            if (image == null || isSmallImage(image)) {
                return imageBytes;
            }

            Thumbnails.of(image)
                .size(TARGET_WIDTH, TARGET_HEIGHT)
                .outputQuality(IMAGE_QUALITY)
                .outputFormat(extension)
                .toOutputStream(outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw SERVER_ERROR.of("이미지 최적화 중 오류 발생");
        }
    }

    /**
     * Firebase Storage 업로드 처리
     */
    private String uploadToFirebase(byte[] fileData, String fileName, String contentType) {
        return Optional.ofNullable(bucket.create(fileName, fileData, contentType))
            .map(blob -> {
                blob.createAcl(Acl.of(Acl.User.ofAllUsers(), READER));
                return generateFileUrl(fileName);
            })
            .orElseThrow(() -> SERVER_ERROR.of("Firebase Storage 업로드 실패"));
    }

    /**
     * 이미지 최적화 불필요한 경우
     */
    private boolean isSmallImage(BufferedImage image) {
        return image.getWidth() <= TARGET_WIDTH && image.getHeight() <= TARGET_HEIGHT;
    }

    /**
     * 빈 파일 검사
     */
    private boolean isInvalidFile(MultipartFile file) {
        return file == null || file.isEmpty();
    }

    private byte[] getFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw SERVER_ERROR.of("파일을 읽을 수 없습니다.");
        }
    }

    private String getFileExtension(String fileName) {
        return Optional.ofNullable(fileName)
            .filter(f -> f.lastIndexOf('.') != -1)
            .map(f -> f.substring(f.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT))
            .filter(VALID_IMAGE_EXTENSIONS::contains)
            .orElse(DEFAULT_EXTENSION);
    }

    private String generateFileName(String originalFilename) {
        return UPLOAD_PATH + UUID.randomUUID() + "_" + extractFileNameFromUrl(originalFilename);
    }

    private String extractFileNameFromUrl(String fileName) {
        return Optional.ofNullable(fileName)
            .map(f -> f.substring(f.lastIndexOf('/') + 1))
            .orElse(UUID.randomUUID().toString());
    }

    private String extractFileName(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return "";
        }
        int index = fileUrl.indexOf(UPLOAD_PATH);
        return (index != -1) ? fileUrl.substring(index) : fileUrl;
    }

    private String generateFileUrl(String fileName) {
        return IMAGE_URL_PREFIX + bucket.getName() + "/" + fileName;
    }
}
