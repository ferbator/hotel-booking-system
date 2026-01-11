package org.ferbator.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.ferbator.entities.enums.Role;

@Entity
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

