package vn.vibeteam.vibe.dto.event;

import lombok.*;
import vn.vibeteam.vibe.model.channel.MessageAttachment;
import vn.vibeteam.vibe.model.channel.MessageMetadata;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChannelMessageCreatedEvent implements Serializable {
    private Long channelId;
    private Long authorId;
    private Long messageId;
    private String clientUniqueId;
    private String content;
    private List<MessageAttachment> attachments;
    private MessageMetadata metadata;
    private Boolean isPinned;
    private LocalDateTime createdAt;
}
