package vn.vibeteam.vibe.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vibeteam.vibe.dto.response.chat.ChannelUnreadResponse;
import vn.vibeteam.vibe.model.channel.Channel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    List<Channel> findByServerId(Long serverId);

    @Modifying
    @Query("UPDATE Channel c SET c.isDeleted = true WHERE c.id = :channelId")
    void deleteChannelById(Long channelId);

    Optional<Channel> findServerIdById(Long channelId);

    @Modifying
    @Query(value = """
                UPDATE channels
                SET last_message_id = :id
                WHERE id = :channelId
                AND (last_message_id IS NULL OR last_message_id < :id)
            """, nativeQuery = true)
    void updateLastMessageId(Long channelId, Long id);

    @Query(value = """
                SELECT
                    c.id as channel_id,
                    c.last_message_id,
                    rs.last_read_message_id
                FROM channels c
                LEFT JOIN user_read_states rs ON c.id = rs.channel_id AND rs.user_id = :userId
                WHERE c.server_id = :serverId
                            AND c.type = 'TEXT'
                            AND c.is_deleted = false
            """, nativeQuery = true)
    List<ChannelUnreadProjection> getChannelUnreadStates(Long serverId, Long userId);

    interface ChannelUnreadProjection {
        Long getChannelId();
        Long getLastMessageId();
        Long getLastReadMessageId();
    }
}
