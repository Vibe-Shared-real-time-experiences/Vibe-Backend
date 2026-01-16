package vn.vibeteam.vibe.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vibeteam.vibe.model.server.Server;
import vn.vibeteam.vibe.model.server.ServerMember;

import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

    @Query("SELECT sm FROM ServerMember sm WHERE sm.server.id = :serverId AND sm.id = :memberId")
    Optional<ServerMember> findMemberById(Long serverId, Long memberId);

    @Query("SELECT DISTINCT s FROM Server s " +
           "LEFT JOIN FETCH s.categories c " +
           "LEFT JOIN FETCH c.channels " +
           "WHERE s.id = :id")
    Optional<Server> findByIdWithCategoriesAndChannels(Long id);

    @Modifying
    @Query("UPDATE Server s SET s.isDeleted = true WHERE s.id = :serverId")
    void deleteServerById(Long serverId);
}
