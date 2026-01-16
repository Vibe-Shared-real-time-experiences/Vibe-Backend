package vn.vibeteam.vibe.dto.response.chat;

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
    private List<MessageResponse> messages;
    private Set<MemberSummaryInfoResponse> memberInfos;
}
