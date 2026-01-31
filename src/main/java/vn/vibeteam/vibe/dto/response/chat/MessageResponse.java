package vn.vibeteam.vibe.dto.response.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MessageResponse {
    private long id;
    private long channelId;
    private long senderId;
    private String content;
    private List<MessageAttachmentResponse> attachments;
    private MessageMetadataResponse metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
