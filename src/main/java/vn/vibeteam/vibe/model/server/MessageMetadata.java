package vn.vibeteam.vibe.model.server;

import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MessageMetadata implements Serializable {
    private Map<String, Integer> reactions = new HashMap<>();
}