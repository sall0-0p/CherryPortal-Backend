package eu.lordbucket.cherryportal.core.identity.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name="local_credentials")
public class LocalCredential {
    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn
    private Account account;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;
}
