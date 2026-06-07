package eu.lordbucket.cherryportal.core.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name="roles")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    private String emoji;
    private String color;

    @CreationTimestamp
    private Instant createdAt;
    private Instant archivedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permission", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission_key")
    private Set<String> permissions = new HashSet<>();

    public boolean exists() {
        return archivedAt == null;
    }
}
