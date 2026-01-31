package vn.vibeteam.vibe.repository.chat;

import aj.org.objectweb.asm.commons.Remapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vibeteam.vibe.model.server.ChannelMessage;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<ChannelMessage, Long> {
    @Query("SELECT cm FROM ChannelMessage cm " +
           "LEFT JOIN FETCH cm.author " +
           "WHERE cm.channel.id = :channelId " +
           "AND cm.id < :currentMessageId ")
    List<ChannelMessage> findOlderMessagesById(Long channelId,
                                               Long currentMessageId,
                                               Pageable pageable);

    @Query("SELECT cm FROM ChannelMessage cm " +
           "LEFT JOIN ServerMember sm ON cm.author.id = sm.id " +
           "WHERE cm.channel.id = :channelId ")
    List<ChannelMessage> findLatestMessages(Long channelId, Pageable pageable);

    @Modifying
    @Query("UPDATE ChannelMessage cm SET cm.isDeleted = true WHERE cm.id = :messageId")
    void deleteMessage(Long messageId);

    Optional<ChannelMessage> findByClientUniqueId(String uniqueId);
}
