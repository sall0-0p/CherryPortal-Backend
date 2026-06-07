package eu.lordbucket.cherryportal.core.identity.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    private UUID accountId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private String displayName;
}
