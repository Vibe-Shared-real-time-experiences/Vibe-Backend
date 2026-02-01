package vn.vibeteam.vibe.dto.common;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CursorResponse<T> {
    private Long nextCursor;
    private Boolean hasMore;
    private T items;
}