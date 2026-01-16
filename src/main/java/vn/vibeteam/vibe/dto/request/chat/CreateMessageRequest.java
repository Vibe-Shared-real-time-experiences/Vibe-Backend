package vn.vibeteam.vibe.dto.request.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class CreateMessageRequest {
    private final String key;
    private final String content;
    private final List<MessageAttachment> attachments;
}
