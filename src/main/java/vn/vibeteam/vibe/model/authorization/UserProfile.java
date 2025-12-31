package vn.vibeteam.vibe.model.authorization;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.model.common.BaseEntity;

@Entity
@Table(name = "user_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserProfile extends BaseEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "is_public")
    private Boolean isPublic;
}

