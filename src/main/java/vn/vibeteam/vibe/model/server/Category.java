package vn.vibeteam.vibe.model.server;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.model.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Category extends BaseEntity {
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

    @Column(name = "position")
    private Integer position;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "category", cascade = CascadeType.PERSIST)
    @Builder.Default
    private Set<Channel> channels = new HashSet<>();
}

