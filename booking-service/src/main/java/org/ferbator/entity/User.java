package org.ferbator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ferbator.entity.enums.Role;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue
    private Long id;


    @Column(unique = true)
    private String username;
    private String password;


    @Enumerated(EnumType.STRING)
    private Role role;
}

