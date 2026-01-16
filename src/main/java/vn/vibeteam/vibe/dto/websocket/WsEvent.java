package vn.vibeteam.vibe.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import vn.vibeteam.vibe.common.EventType;

@AllArgsConstructor
@Getter
@Builder
public class WsEvent<T> {
    private EventType eventType;
    private T data;
}
