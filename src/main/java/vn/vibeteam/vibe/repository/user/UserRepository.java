package vn.vibeteam.vibe.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vibeteam.vibe.model.authorization.User;
import vn.vibeteam.vibe.model.authorization.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByIdAndIsActiveTrue(Long id);
    Boolean existsByUsername(String username);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.user.id = :id AND ur.role.isActive = true")
    Optional<List<UserRole>> findUserRolesByUserId(Long id);
}
