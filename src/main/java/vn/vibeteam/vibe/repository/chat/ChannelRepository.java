package vn.vibeteam.vibe.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vibeteam.vibe.model.server.Channel;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    List<Channel> findByServerId(Long serverId);

    @Modifying
    @Query("UPDATE Channel c SET c.isDeleted = true WHERE c.id = :channelId")
    void deleteChannelById(Long channelId);
}
