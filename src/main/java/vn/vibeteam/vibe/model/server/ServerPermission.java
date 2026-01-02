package vn.vibeteam.vibe.model.server;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.model.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "server_permissions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServerPermission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "bitmask", nullable = false)
    private Long bitmask;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ServerHasPermission> serverHasPermissions = new HashSet<>();
}
