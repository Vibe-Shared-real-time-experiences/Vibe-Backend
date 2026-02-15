package vn.vibeteam.vibe.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChannelHistoryResponse {
//    private List<MessageResponse> messages;
    @JsonRawValue
    private String messages;
}
