package vn.vibeteam.vibe.service.media.media;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import vn.vibeteam.vibe.common.MediaType;
import vn.vibeteam.vibe.dto.request.media.UploadMediaRequest;
import vn.vibeteam.vibe.exception.AppException;
import vn.vibeteam.vibe.exception.ErrorCode;
import vn.vibeteam.vibe.service.media.MediaService;
import vn.vibeteam.vibe.util.SecurityUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final S3Client s3Client;

    private final Tika tika;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private final SecurityUtils securityUtils;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", // Image
            "application/pdf", "text/plain", // Document
            "video/mp4", "video/mpeg", "video/quicktime" // Video
    );

    public String uploadFile(UploadMediaRequest request) {
        if (request.getFile().isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        String originalFilename = request.getFile().getOriginalFilename();
        log.info("Uploading file: {}", request.getFile().getOriginalFilename());

        try {

            // 1. Validate file type base on binary (InputStream)
            String detectedMimeType = tika.detect(request.getFile().getInputStream());
            log.info("Detected MIME type: {}", detectedMimeType);

            if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
                throw new AppException(ErrorCode.UN_SUPPORTED_FILE_TYPE);
            }

            // 2. Create unique file name (UUID + original file extension)
            // For ex: avatar.png -> 550e8400-e29b...png
            String extension = extractExtension(originalFilename);
            String objectKey = "";

            switch (request.getType()) {
                case MediaType.AVATAR:
                    String userId = String.valueOf(securityUtils.getCurrentUserId());
                    objectKey = String.format("users/%s/avatar%s", userId, extension);
                    break;

                case MediaType.SERVER_ICON:
                    objectKey = String.format("servers/%s/icon%s", request.getId(), extension);
                    break;

                case MediaType.ATTACHMENT:
                default:
                    // Partition by date for attachments
                    String datePath = LocalDate.now().toString(); // 2026-01-28
                    String uuid = UUID.randomUUID().toString();
                    objectKey = String.format("attachments/%s/%s%s", datePath, uuid, extension);
                    break;
            }

            // 2. Create upload request
            PutObjectRequest putOb = PutObjectRequest.builder()
                                                     .bucket(bucketName)
                                                     .key(objectKey)
                                                     .contentType(detectedMimeType)
                                                     .build();

            // 3. Push file to MinIO server
            s3Client.putObject(putOb, RequestBody.fromBytes(request.getFile().getBytes()));

            // 4. Return file URL for FE to access
            String finalObjectKey = objectKey;
            String finalUrl = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(finalObjectKey)).toExternalForm();
            log.info("File uploaded successfully: {}", finalUrl);

            return finalUrl;
        } catch (IOException e) {
            throw new RuntimeException("Error when upload file " + e.getMessage());
        }
    }

    private String extractExtension(String filename) {
        return filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf("."))
                : "";
    }
}
