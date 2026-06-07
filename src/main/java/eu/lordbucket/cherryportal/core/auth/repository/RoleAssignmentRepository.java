package eu.lordbucket.cherryportal.core.auth.repository;

import eu.lordbucket.cherryportal.core.auth.model.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, UUID> {
    List<RoleAssignment> findAllByAccountId(UUID accountId);
    List<RoleAssignment> findAllByRoleId(UUID roleId);

    @Query("""
    SELECT ra FROM RoleAssignment ra
    WHERE ra.account.id = :accountId
      AND ra.revokedAt IS NULL
      AND (ra.startsAt IS NULL OR ra.startsAt <= :now)
      AND (ra.expiresAt IS NULL OR ra.expiresAt > :now)
    """)
    List<RoleAssignment> findAllEffectiveForAccount(
            @Param("accountId") UUID accountId,
            @Param("now") Instant now
    );
}
