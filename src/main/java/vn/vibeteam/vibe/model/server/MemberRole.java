package vn.vibeteam.vibe.model.server;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.model.common.BaseEntity;

@Entity
@Table(name = "member_roles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MemberRole extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "server_id", nullable = false)
    private Long serverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", insertable = false, updatable = false)
    private Server server;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "color")
    private String color;

    @Column(name = "position")
    private Integer position;

    @Column(name = "is_active")
    private Boolean isActive;
}

