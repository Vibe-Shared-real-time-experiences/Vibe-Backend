package vn.vibeteam.vibe.model.conversation;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.model.authorization.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_participants")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConversationParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}
