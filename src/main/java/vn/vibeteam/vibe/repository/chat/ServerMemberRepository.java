package vn.vibeteam.vibe.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vibeteam.vibe.model.server.ServerMember;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ServerMemberRepository extends JpaRepository<ServerMember, Long> {

    @Query("SELECT sm FROM ServerMember sm WHERE sm.server.id = :serverId AND sm.user.id = :userId")
    Optional<ServerMember> findByServerIdAndUserId(Long serverId, Long userId);

    @Query("SELECT COUNT(sm) > 0 FROM ServerMember sm WHERE sm.server.id = :serverId AND sm.user.id = :userId")
    boolean existsByServerIdAndUserId(Long serverId, Long userId);

    @Query("SELECT sm FROM ServerMember sm JOIN FETCH sm.server s WHERE sm.user.id = :userId AND s.isActive = true AND sm.isActive = true")
    List<ServerMember> findByUserId(Long userId);

    @Query("SELECT sm FROM ServerMember sm " +
           "JOIN FETCH sm.user u " +
           "JOIN FETCH u.userProfile " +
           "WHERE sm.server.id = :serverId AND sm.isActive = true AND u.isActive = true")
    Set<ServerMember> findDetailsByServerIdAndIn(Long serverId, Set<Long> memberIds);

    @Modifying
    @Query("UPDATE ServerMember sm SET sm.isActive = false WHERE sm.server.id = :serverId AND sm.user.id = :userId")
    void deleteServerMemberByMemberId(Long serverId, Long userId);
}
