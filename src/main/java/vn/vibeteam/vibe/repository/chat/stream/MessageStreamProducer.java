package vn.vibeteam.vibe.repository.chat.stream;

import vn.vibeteam.vibe.dto.event.ChannelMessageCreatedEvent;

public interface MessageStreamProducer {
    void sendToStream(ChannelMessageCreatedEvent message);
}
