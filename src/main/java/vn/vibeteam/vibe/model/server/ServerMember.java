package vn.vibeteam.vibe.model.server;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.model.authorization.User;
import vn.vibeteam.vibe.model.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "server_members")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServerMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "is_active")
    private Boolean isActive;
}

