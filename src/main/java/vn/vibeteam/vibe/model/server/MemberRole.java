package vn.vibeteam.vibe.model.server;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_roles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MemberRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private ServerMember serverMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private ServerRole serverRole;
}
