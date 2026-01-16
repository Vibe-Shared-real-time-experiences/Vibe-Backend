package vn.vibeteam.vibe.model.server;

import lombok.*;
import vn.vibeteam.vibe.common.MessageAttachmentType;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MessageAttachment implements Serializable {
    private String url;
    private MessageAttachmentType type;
    private String contentType; // "image/png", "video/mp4"
    private int width;
    private int height;
    private long size;
}