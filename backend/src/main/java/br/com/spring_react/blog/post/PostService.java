package br.com.spring_react.blog.post;

import br.com.spring_react.blog.infra.exceptions.ForbiddenActionException;
import br.com.spring_react.blog.infra.exceptions.ResourceNotFoundException;
import br.com.spring_react.blog.post.internal.Post;
import br.com.spring_react.blog.post.internal.PostRepository;
import br.com.spring_react.blog.user.UserService;
import br.com.spring_react.blog.user.internal.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;

    public PostService(PostRepository postRepository, UserService userService) {
        this.postRepository = postRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<Post> findAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Post findById(UUID id) {
        return postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post " +
                "not found."));
    }

    @Transactional(readOnly = true)
    public Post findBySlug(String slug) {
        return postRepository.findBySlug(slug).orElseThrow(() -> new ResourceNotFoundException(
                "Post not found."));
    }

    @Transactional(readOnly = true)
    public Page<Post> findByAuthor(String author, Pageable pageable) {
        return postRepository.findAllByAuthorSlug(author, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> findByTitle(String title, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    @Transactional
    public Post createPost(PostCreateDTO data, UUID authorId) {
        User author = userService.findById(authorId);

        if (author == null) {
            throw new ResourceNotFoundException("Author not found.");
        }

        Post post = new Post();

        post.setTitle(data.title());
        post.setSummary(data.summary());
        post.setContent(data.content());
        post.setAuthor(author);

        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(UUID postId, UUID authenticatedUserId, PostUpdateDTO data) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found."));

        if (!post.getAuthor().getId().equals(authenticatedUserId)) {
            throw new ForbiddenActionException("You are not authorized to modify this post.");
        }

        if (data.title() != null) {
            post.setTitle(data.title());
        }

        if (data.summary() != null) {
            post.setSummary(data.summary());
        }

        if (data.content() != null) {
            post.setContent(data.content());
        }

        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(UUID id, UUID authenticatedUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found."));

        if (!post.getAuthor().getId().equals(authenticatedUserId)) {
            throw new ForbiddenActionException("You are not authorized to modify this post.");
        }

        postRepository.deleteById(id);
    }
}
