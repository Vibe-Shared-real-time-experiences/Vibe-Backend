package vn.vibeteam.vibe.dto.response.chat;

import lombok.*;
import vn.vibeteam.vibe.common.MessageStatus;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreateMessageResponse implements Serializable {
    private String clientUniqueId;
    private String messageId;
    private MessageStatus status;
}
