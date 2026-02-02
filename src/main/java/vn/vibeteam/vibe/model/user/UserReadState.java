package vn.vibeteam.vibe.model.user;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.model.channel.Channel;
import vn.vibeteam.vibe.model.channel.ChannelMessage;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_read_states")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserReadState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    private ChannelMessage lastReadMessage;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
