package vn.vibeteam.vibe.dto.response.chat;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MemberSummaryInfoResponse {
    @EqualsAndHashCode.Include
    private long memberId;
    private String displayName;
    private String avatarUrl;
}
