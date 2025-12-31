package vn.vibeteam.vibe.model.server;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MessageAttachment implements Serializable {
    private String url;
    private String type; // "IMAGE", "VIDEO", "FILE"
    private String contentType; // "image/png", "video/mp4"
    private int width;
    private int height;
    private long size;
}