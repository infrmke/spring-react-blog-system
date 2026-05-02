package br.com.spring_react.blog.user.internal;

import br.com.spring_react.blog.comment.internal.Comment;
import br.com.spring_react.blog.infra.utils.SlugGenerator;
import br.com.spring_react.blog.like.internal.PostLike;
import br.com.spring_react.blog.post.internal.Post;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.PROTECTED)
    private UUID id;

    @Column(nullable = false, length = 54)
    @Size(min = 2, max = 54)
    private String name;

    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    private String password;

    private String avatar;

    @Column(unique = true, nullable = false)
    private String slug;

    @Enumerated(EnumType.STRING) // pode ser "USER" ou "ADMIN"
    @Column(nullable = false)
    private UserRole role = UserRole.USER; // o padrão é "USER"

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PostLike> likes;

    @Column(name = "created_at", updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // o role ADMIN tem permissão de ADMIN e de USER
        if (this.role == UserRole.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        // o role USER possui apenas permissão de USER
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    @NonNull
    public String getUsername() {
        return email; // o campo "email" é usado como "username" para identificação
    }

    @Override
    public String getPassword() {
        return password; // aponta para onde a senha criptografada está guardada
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // define que qualquer conta criada é permanente e não expira
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // define que as contas não possuem travas de acesso
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // define que a senha do usuário não expira então não é necessário trocá-la
    }

    @Override
    public boolean isEnabled() {
        return true; // define que todos os usuário que estão no banco estão ativos
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        // cria um slug único para o usuário (nome-e-uuid OU nome-sobrenome-e-uuid)
        if (this.slug == null || this.slug.isEmpty()) {
            String[] names = this.name.trim().split("\\s+"); // desconsidera espaços extras
            String slugInput = (names.length > 1)
                    ? names[0] + " " + names[names.length - 1]
                    : names[0];

            this.slug = SlugGenerator.generate(slugInput);
        }

        // atribui um avatar ao usuário a partir do seu primeiro nome
        if (this.avatar == null || this.avatar.isEmpty()) {
            String firstName = name.split(" ")[0];
            this.avatar = "https://ui-avatars.com/api/?name=" + firstName + "&background=random";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
