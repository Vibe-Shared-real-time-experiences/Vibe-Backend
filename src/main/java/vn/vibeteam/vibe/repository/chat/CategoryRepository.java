package vn.vibeteam.vibe.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vibeteam.vibe.model.server.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.channels WHERE c.id = :id")
    Optional<Category> findByIdWithChannels(Long id);

    List<Category> findByServerId(Long serverId);

    @Modifying
    @Query("UPDATE Category c SET c.isDeleted = true WHERE c.id = :categoryId")
    void deleteCategoryById(Long categoryId);
}
