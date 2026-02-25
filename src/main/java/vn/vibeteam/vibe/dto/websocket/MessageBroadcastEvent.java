package vn.vibeteam.vibe.dto.websocket;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import vn.vibeteam.vibe.model.channel.ChannelMessage;

@Getter
public class MessageBroadcastEvent extends ApplicationEvent {
    private final ChannelMessage channelMessage;
    private final Long serverId;

    public MessageBroadcastEvent(Object source, ChannelMessage channelMessage, Long serverId) {
        super(source);
        this.channelMessage = channelMessage;
        this.serverId = serverId;
    }
}

