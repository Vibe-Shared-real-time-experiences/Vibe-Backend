package vn.vibeteam.vibe.model.server;

import jakarta.persistence.*;
import lombok.*;
import vn.vibeteam.vibe.constraint.ChannelType;
import vn.vibeteam.vibe.model.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "channels")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Channel extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "server_id")
    private Long serverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", insertable = false, updatable = false)
    private Server server;

    @Column(name = "category_id")
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @Column(name = "name")
    private String name;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChannelType type;

    @Column(name = "position")
    private Integer position;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ChannelMessage> messages = new HashSet<>();
}

