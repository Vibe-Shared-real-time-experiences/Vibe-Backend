package vn.vibeteam.vibe.repository.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vibeteam.vibe.model.channel.ChannelMessage;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<ChannelMessage, Long> {
    @Query("SELECT cm FROM ChannelMessage cm " +
           "WHERE cm.channel.id = :channelId " +
           "AND cm.id < :currentMessageId " +
           "ORDER BY cm.id DESC")
    List<ChannelMessage> findOlderMessagesById(Long channelId,
                                               Long currentMessageId,
                                               Pageable pageable);

    @Query("SELECT cm FROM ChannelMessage cm " +
           "WHERE cm.channel.id = :channelId " +
           "AND cm.id > :currentMessageId " +
           "ORDER BY cm.id ASC")
    List<ChannelMessage> findNewerMessagesById(Long channelId,
                                               Long currentMessageId,
                                               Pageable pageable);

    @Query("SELECT cm FROM ChannelMessage cm " +
           "WHERE cm.channel.id = :channelId " +
           "ORDER BY cm.id DESC")
    List<ChannelMessage> findLatestMessages(Long channelId, Pageable pageable);

    @Modifying
    @Query("UPDATE ChannelMessage cm SET cm.isDeleted = true WHERE cm.id = :messageId")
    void deleteMessage(Long messageId);

    Optional<ChannelMessage> findByClientUniqueId(String uniqueId);

    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT 1 
            FROM channel_messages 
            WHERE channel_id = :channelId 
            AND id > :lastReadId 
            LIMIT :limit
        ) AS subquery_limit
    """, nativeQuery = true)
    Long countUnreadMessagesInChannel(Long channelId, Long lastReadId, int limit);
}
