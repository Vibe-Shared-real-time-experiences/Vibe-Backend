package vn.vibeteam.vibe.model.server;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.model.authorization.User;
import vn.vibeteam.vibe.model.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "servers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Server extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private User owner;

    @Column(name = "name", nullable = false)
    private String name;

    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Channel> channels = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ServerMember> serverMembers = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ServerRole> serverRoles = new HashSet<>();
}

