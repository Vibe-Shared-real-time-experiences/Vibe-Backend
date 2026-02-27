package vn.vibeteam.vibe.dto.request.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class UserSummaryRequest {
    private List<Long> userIds;
}
