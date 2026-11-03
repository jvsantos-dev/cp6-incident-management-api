package br.com.fiap.incidentmanagement.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "senha", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    protected User() {
    }

    public User(String name, String login, String password, Role role) {
        this.name = name;
        this.login = login;
        this.password = password;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
