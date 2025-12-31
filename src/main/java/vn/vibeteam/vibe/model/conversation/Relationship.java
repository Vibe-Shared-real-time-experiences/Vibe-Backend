package vn.vibeteam.vibe.model.conversation;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.model.authorization.User;
import vn.vibeteam.vibe.model.common.BaseEntity;

@Entity
@Table(name = "relationships")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Relationship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_1")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_2")
    private User user2;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RelationshipStatus status;

    public enum RelationshipStatus {
        PENDING,
        FRIEND,
        BLOCKED
    }
}

