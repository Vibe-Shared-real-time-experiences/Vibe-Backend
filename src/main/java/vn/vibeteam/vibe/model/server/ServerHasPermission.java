package vn.vibeteam.vibe.model.server;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "server_has_permissions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServerHasPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private ServerPermission permission;
}
