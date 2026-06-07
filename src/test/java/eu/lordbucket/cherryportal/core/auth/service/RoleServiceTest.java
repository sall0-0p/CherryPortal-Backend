package eu.lordbucket.cherryportal.core.auth.service;

import eu.lordbucket.cherryportal.core.auth.dto.RoleUpdatableProperties;
import eu.lordbucket.cherryportal.core.auth.model.Role;
import eu.lordbucket.cherryportal.core.auth.model.RoleAssignment;
import eu.lordbucket.cherryportal.core.auth.permission.PermissionRegistry;
import eu.lordbucket.cherryportal.core.auth.repository.RoleAssignmentRepository;
import eu.lordbucket.cherryportal.core.auth.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final RoleAssignmentRepository roleAssignmentRepository = mock(RoleAssignmentRepository.class);
    private final PermissionRegistry permissionRegistry = mock(PermissionRegistry.class);
    private final RoleService service = new RoleService(roleRepository, roleAssignmentRepository, permissionRegistry);

    @Test
    void createRole_savesRoleWithAllFields() {
        Role saved = new Role();
        when(roleRepository.save(any())).thenReturn(saved);

        Role result = service.createRole("Moderator", "Can moderate", "🛡", "#FF0000", Set.of());

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        Role captured = captor.getValue();

        assertEquals("Moderator", captured.getName());
        assertEquals("Can moderate", captured.getDescription());
        assertEquals("🛡", captured.getEmoji());
        assertEquals("#FF0000", captured.getColor());
        assertSame(saved, result);
    }

    @Test
    void createRole_nullPermissions_defaultsToEmptySet() {
        when(roleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Role result = service.createRole("Moderator", null, null, null, null);

        assertNotNull(result.getPermissions());
        assertTrue(result.getPermissions().isEmpty());
    }

    @Test
    void archiveRole_setsArchivedAtAndRevokesAllAssignments() {
        UUID id = UUID.randomUUID();
        Role role = new Role();
        RoleAssignment a1 = new RoleAssignment();
        RoleAssignment a2 = new RoleAssignment();

        when(roleRepository.findById(id)).thenReturn(Optional.of(role));
        when(roleAssignmentRepository.findAllByRoleId(id)).thenReturn(List.of(a1, a2));

        service.archiveRole(id);

        assertNotNull(role.getArchivedAt());
        assertNotNull(a1.getRevokedAt());
        assertNotNull(a2.getRevokedAt());
        verify(roleAssignmentRepository).saveAll(List.of(a1, a2));
        verify(roleRepository).save(role);
    }

    @Test
    void archiveRole_unknownId_throws404() {
        UUID id = UUID.randomUUID();
        when(roleRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> service.archiveRole(id));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void listRoles_returnsOnlyNonArchived() {
        Role r1 = new Role();
        Role r2 = new Role();
        when(roleRepository.findAllByArchivedAtIsNull()).thenReturn(List.of(r1, r2));

        List<Role> result = service.listRoles();

        assertEquals(2, result.size());
        verify(roleRepository).findAllByArchivedAtIsNull();
        verify(roleRepository, never()).findAll();
    }

    @Test
    void getRole_knownId_returnsRole() {
        UUID id = UUID.randomUUID();
        Role role = new Role();
        when(roleRepository.findById(id)).thenReturn(Optional.of(role));

        assertSame(role, service.getRole(id));
    }

    @Test
    void getRole_unknownId_throws404() {
        UUID id = UUID.randomUUID();
        when(roleRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> service.getRole(id));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateRole_onlyNonNullFieldsAreChanged() {
        UUID id = UUID.randomUUID();
        Role role = new Role();
        role.setName("Old");
        role.setDescription("Keep this");
        role.setEmoji("🔴");
        role.setColor("#000");

        when(roleRepository.findById(id)).thenReturn(Optional.of(role));
        when(roleRepository.save(role)).thenReturn(role);

        service.updateRole(id, new RoleUpdatableProperties("New", null, null, null));

        assertEquals("New", role.getName());
        assertEquals("Keep this", role.getDescription());
        assertEquals("🔴", role.getEmoji());
        assertEquals("#000", role.getColor());
    }

    @Test
    void updateRole_savesAndReturnsUpdatedRole() {
        UUID id = UUID.randomUUID();
        Role role = new Role();
        when(roleRepository.findById(id)).thenReturn(Optional.of(role));
        when(roleRepository.save(role)).thenReturn(role);

        Role result = service.updateRole(id, new RoleUpdatableProperties("Name", null, null, null));

        verify(roleRepository).save(role);
        assertSame(role, result);
    }

    @Test
    void addPermission_knownKey_addsToRole() {
        UUID id = UUID.randomUUID();
        Role role = new Role();
        role.setPermissions(new HashSet<>());
        when(roleRepository.findById(id)).thenReturn(Optional.of(role));
        when(permissionRegistry.isKnown("core.role.manage")).thenReturn(true);

        service.addPermission(id, "core.role.manage");

        assertTrue(role.getPermissions().contains("core.role.manage"));
        verify(roleRepository).save(role);
    }

    @Test
    void addPermission_unknownKey_throws400() {
        UUID id = UUID.randomUUID();
        when(permissionRegistry.isKnown("not.a.real.key")).thenReturn(false);

        var ex = assertThrows(ResponseStatusException.class, () -> service.addPermission(id, "not.a.real.key"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(roleRepository, never()).save(any());
    }

    @Test
    void removePermission_knownKey_removesFromRole() {
        UUID id = UUID.randomUUID();
        Role role = new Role();
        role.setPermissions(new HashSet<>(Set.of("core.role.manage")));
        when(roleRepository.findById(id)).thenReturn(Optional.of(role));
        when(permissionRegistry.isKnown("core.role.manage")).thenReturn(true);

        service.removePermission(id, "core.role.manage");

        assertFalse(role.getPermissions().contains("core.role.manage"));
        verify(roleRepository).save(role);
    }

    @Test
    void removePermission_unknownKey_throws400() {
        UUID id = UUID.randomUUID();
        when(permissionRegistry.isKnown("not.a.real.key")).thenReturn(false);

        var ex = assertThrows(ResponseStatusException.class, () -> service.removePermission(id, "not.a.real.key"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(roleRepository, never()).save(any());
    }
}
