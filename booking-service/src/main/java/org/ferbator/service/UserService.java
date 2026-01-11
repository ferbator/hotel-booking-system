package org.ferbator.service;

import org.ferbator.entity.enums.Role;
import org.ferbator.entity.User;
import org.ferbator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Transactional
    public User register(
            String username,
            String rawPassword,
            Role role
    ) {
        if (users.findByUsername(username).isPresent()) throw new IllegalArgumentException("Username taken");
        var user = User.builder()
                .username(username)
                .password(encoder.encode(rawPassword))
                .role(role)
                .build();
        return users.save(user);
    }

    public Optional<User> byUsername(String user) {
        return users.findByUsername(user);
    }

    @Transactional
    public User update(Long id, String rawPass, Role role) {
        var user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (rawPass != null && !rawPass.isBlank()) user.setPassword(encoder.encode(rawPass));
        if (role != null) user.setRole(role);
        return user;
    }

    @Transactional
    public void delete(Long id) {
        users.deleteById(id);
    }
}
