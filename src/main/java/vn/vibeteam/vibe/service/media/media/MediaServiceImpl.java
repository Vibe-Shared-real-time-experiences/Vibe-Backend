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
import vn.vibeteam.vibe.common.MessageAttachmentType;
import vn.vibeteam.vibe.dto.request.media.UploadMediaRequest;
import vn.vibeteam.vibe.dto.response.media.UploadMediaResponse;
import vn.vibeteam.vibe.exception.AppException;
import vn.vibeteam.vibe.exception.ErrorCode;
import vn.vibeteam.vibe.service.media.MediaService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
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

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", // Image
            "application/pdf", "text/plain", // Document
            "video/mp4", "video/mpeg", "video/quicktime" // Video
    );

    @Override
    public UploadMediaResponse uploadFile(long userId, UploadMediaRequest request) {
        MultipartFile file = request.getFile();

        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        String originalFilename = file.getOriginalFilename();
        log.info("Uploading file: {}", file.getOriginalFilename());

        try {
            // 1. Validate file type based on binary (InputStream)
            String detectedMimeType = tika.detect(file.getInputStream());

            if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
                throw new AppException(ErrorCode.UN_SUPPORTED_FILE_TYPE);
            }

            // 2. Create object key based on media type
            String extension = extractExtension(originalFilename);
            String objectKey = "";

            switch (request.getType()) {
                case MediaType.AVATAR:
                    objectKey = String.format("users/%d/avatar%s", userId, extension);
                    break;

                case MediaType.SERVER_ICON:
                    objectKey = String.format("servers/%s/icon%s", request.getId(), extension);
                    break;

                case MediaType.ATTACHMENT:
                default:
                    // Partition by date for attachments
                    String datePath = LocalDateTime.now().toString(); // 2026-01-28
                    String uuid = UUID.randomUUID().toString();
                    objectKey = String.format("attachments/%s/%s%s", datePath, uuid, extension);
                    break;
            }

            // 3. Create upload request
            PutObjectRequest putOb = PutObjectRequest.builder()
                                                     .bucket(bucketName)
                                                     .key(objectKey)
                                                     .contentType(detectedMimeType)
                                                     .build();

            // 4. Push file to MinIO server
            s3Client.putObject(putOb, RequestBody.fromBytes(file.getBytes()));

            // 5. Return file metadata info for FE
            String finalObjectKey = objectKey;
            String finalUrl = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(finalObjectKey)).toExternalForm();

            UploadMediaResponse uploadMediaResponse = createUploadMediaResponse(file, finalUrl);
            log.info("File uploaded successfully: {}", uploadMediaResponse);

            return uploadMediaResponse;
        } catch (IOException e) {
            throw new RuntimeException("Error when upload file " + e.getMessage());
        }
    }

    private UploadMediaResponse createUploadMediaResponse(MultipartFile file, String finalUrl) {
        UploadMediaResponse uploadMediaResponse = new UploadMediaResponse();
        uploadMediaResponse.setUrl(finalUrl);
        uploadMediaResponse.setContentType(file.getContentType());
        uploadMediaResponse.setSize(file.getSize());

        if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image != null) {
                    uploadMediaResponse.setType(MessageAttachmentType.IMAGE);
                    uploadMediaResponse.setWidth(image.getWidth());
                    uploadMediaResponse.setHeight(image.getHeight());
                }
            } catch (Exception e) {
                log.warn("Can`t read image size: {}", e.getMessage());
            }
        } else if (file.getContentType() != null && file.getContentType().startsWith("video/")) {
            uploadMediaResponse.setType(MessageAttachmentType.VIDEO);

            // TODO: Replace with real video dimension extraction
            uploadMediaResponse.setWidth(0);
            uploadMediaResponse.setHeight(0);
        } else {
            uploadMediaResponse.setType(MessageAttachmentType.FILE);
        }

        return uploadMediaResponse;
    }

    private String extractExtension(String filename) {
        return filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf("."))
                : "";
    }
}
