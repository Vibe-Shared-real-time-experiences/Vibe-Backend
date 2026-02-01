package vn.vibeteam.vibe.dto.response.chat;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MemberSummaryInfoResponse {
    @EqualsAndHashCode.Include
    private Long memberId;
    private String displayName;
    private String avatarUrl;
}
