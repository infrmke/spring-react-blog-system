package br.com.spring_react.blog.post.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Optional<Post> findBySlug(String slug);
    List<Post> findAllByAuthorSlug(String authorSlug);
    List<Post> findByTitleContainingIgnoreCase(String title);
}
