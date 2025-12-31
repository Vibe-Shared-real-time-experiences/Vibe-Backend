package vn.vibeteam.vibe.model.authorization;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.model.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Permission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer code;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "permission")
    @Builder.Default
    private Set<RolePermission> rolePermissions = new HashSet<>();
}

