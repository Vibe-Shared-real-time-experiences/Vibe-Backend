package vn.vibeteam.vibe.model.authorization;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import vn.vibeteam.vibe.model.common.BaseEntity;

import java.util.Set;

@Entity
@Table(name = "users")
@SQLRestriction("is_deleted = false")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile userProfile;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new java.util.HashSet<>();

    @Column(name = "is_active")
    private Boolean isActive;

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        if (userProfile != null) {
            userProfile.setUser(this);
        }
    }

    public void addUserRole(UserRole userRole) {
        this.userRoles.add(userRole);
        if (userRole != null) {
            userRole.setUser(this);
        }
    }
}
