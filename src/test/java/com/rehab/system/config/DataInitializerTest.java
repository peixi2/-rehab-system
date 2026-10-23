package com.rehab.system.config;

import com.rehab.system.common.RoleConstants;
import com.rehab.system.entity.SysUser;
import com.rehab.system.repository.PatientRepository;
import com.rehab.system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void rotatesOnlyKnownDemoAccountsAndCreatesMissingOnes() {
        SysUser admin = user("admin", RoleConstants.ADMIN, "old-hash");
        SysUser custom = user("custom-admin", RoleConstants.ADMIN, "custom-hash");
        when(passwordEncoder.encode("new-demo-password")).thenReturn("new-hash");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("doctor")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("therapist")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("deptadmin")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("patient")).thenReturn(Optional.empty());
        when(userRepository.findAll()).thenReturn(List.of(custom));

        new DataInitializer(userRepository, patientRepository, passwordEncoder, "new-demo-password").run();

        ArgumentCaptor<SysUser> savedUsers = ArgumentCaptor.forClass(SysUser.class);
        verify(userRepository, org.mockito.Mockito.times(5)).save(savedUsers.capture());
        Set<String> savedNames = savedUsers.getAllValues().stream()
                .map(SysUser::getUsername)
                .collect(Collectors.toSet());
        assertEquals(Set.of("admin", "doctor", "therapist", "deptadmin", "patient"), savedNames);
        assertFalse(savedUsers.getAllValues().stream()
                .anyMatch(user -> !"new-hash".equals(user.getPassword())));
        assertEquals("custom-hash", custom.getPassword());
    }

    private SysUser user(String username, String role, String password) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setRole(role);
        user.setPassword(password);
        user.setEnabled(true);
        return user;
    }
}
