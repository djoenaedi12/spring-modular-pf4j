package gasi.gps.auth.infrastructure.security;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import gasi.gps.auth.infrastructure.entity.UserEntity;
import gasi.gps.auth.infrastructure.persistence.UserEntityRepository;

/**
 * UserDetailsService implementation that loads user data from the database.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserEntityRepository userRepository;

    /**
     * Constructs the service.
     */
    public CustomUserDetailsService(UserEntityRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!Boolean.TRUE.equals(user.getIsEnabled()))
                .accountLocked(user.getLockedUntil() != null
                        && user.getLockedUntil().isAfter(java.time.Instant.now()))
                .authorities(user.getRoles() != null
                        ? user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority(role.getCode()))
                                .collect(Collectors.toList())
                        : java.util.List.of())
                .build();
    }
}
