package vn.vibeteam.vibe.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
public class MessageResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private Long channelId;
    private Long senderId;
    private String content;
    private List<MessageAttachmentResponse> attachments;
    private MessageMetadataResponse metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
