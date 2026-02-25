package vn.vibeteam.vibe.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.*;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChannelHistoryResponse {
    private List<MessageResponse> messages;
//    @JsonRawValue
//    private String messages;
//    @JsonIgnore
//    private Long nextId;
}
