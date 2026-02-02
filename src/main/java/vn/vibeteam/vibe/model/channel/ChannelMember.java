package vn.vibeteam.vibe.model.channel;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.model.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "channel_members")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChannelMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}
