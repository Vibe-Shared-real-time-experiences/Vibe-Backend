package vn.vibeteam.vibe.dto.common;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CursorResponse<T> {
    private String nextCursor;
    private boolean hasMore;
    private T items;
}