package vn.vibeteam.vibe.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vibeteam.vibe.model.user.UserReadState;

import java.util.Optional;

@Repository
public interface UserReadStateRepository extends JpaRepository<UserReadState, Long> {
    Optional<UserReadState> findByUserIdAndChannelId(Long userId, Long channelId);

    @Modifying
    @Query(value = """
        INSERT INTO user_read_states (user_id, channel_id, last_read_message_id, last_updated)
        VALUES (:userId, :channelId, :msgId, NOW())
        ON CONFLICT (user_id, channel_id) 
        DO UPDATE SET 
            last_read_message_id = :messageId,
            last_updated = NOW()
        """, nativeQuery = true)
    void upsertReadState(Long userId, Long channelId, Long messageId);
}
