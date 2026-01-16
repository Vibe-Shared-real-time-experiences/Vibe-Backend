package vn.vibeteam.vibe.dto.response.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MessageMetadataResponse {
    private Map<String, Integer> reactions;
}
