package br.com.spring_react.blog.post.internal;

import br.com.spring_react.blog.comment.internal.Comment;
import br.com.spring_react.blog.infra.utils.SlugGenerator;
import br.com.spring_react.blog.like.internal.PostLike;
import br.com.spring_react.blog.user.internal.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    @Size(min = 5, max = 150)
    private String title;

    @Column(length = 200)
    @Size(max = 200)
    private String summary;

    @Column(columnDefinition = "TEXT", nullable = false, length = 20000)
    @Size(min = 100, max = 20000)
    private String content;

    @Column
    private String banner = "https://placehold.co/600x400/png";

    @Column(unique = true, nullable = false)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PostLike> likes;

    @Column(name = "created_at", updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        // cria um slug único para o post (titulo-completo-e-uuid)
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = SlugGenerator.generate(this.title);
        }

        // atribui um summary ao post se o autor não defini-lo
        if (this.summary == null || this.summary.isBlank()) {
            int limit = Math.min(content.length(), 150);
            this.summary = content.substring(0, limit) + "...";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
