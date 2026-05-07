package br.com.spring_react.blog.post;

import br.com.spring_react.blog.infra.exceptions.ForbiddenActionException;
import br.com.spring_react.blog.infra.exceptions.ResourceNotFoundException;
import br.com.spring_react.blog.infra.services.MultiPartService;
import br.com.spring_react.blog.post.dto.PostCreateDTO;
import br.com.spring_react.blog.post.dto.PostUpdateDTO;
import br.com.spring_react.blog.post.internal.Post;
import br.com.spring_react.blog.post.internal.PostRepository;
import br.com.spring_react.blog.user.UserService;
import br.com.spring_react.blog.user.internal.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final MultiPartService multiPartService;

    public PostService(PostRepository postRepository, UserService userService, MultiPartService multiPartService) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.multiPartService = multiPartService;
    }

    @Transactional(readOnly = true)
    public Page<Post> findAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Post findById(UUID id) {
        return postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post " +
                "not found"));
    }

    @Transactional(readOnly = true)
    public Post findBySlug(String slug) {
        return postRepository.findBySlug(slug).orElseThrow(() -> new ResourceNotFoundException(
                "Post not found"));
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
            throw new ResourceNotFoundException("Author not found");
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
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getAuthor().getId().equals(authenticatedUserId)) {
            throw new ForbiddenActionException("You are not authorized to modify this post");
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
    public Post updateBanner(UUID id, UUID authenticatedUserId, MultipartFile file) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getAuthor().getId().equals(authenticatedUserId)) {
            throw new ForbiddenActionException("You are not authorized to modify this post");
        }

        // deleta a imagem antiga se ela existir e não for um link (pra salvar espaço)
        if (post.getBanner() != null && !post.getBanner().startsWith("http")) {
            multiPartService.deleteImage("banners", post.getBanner());
        }

        // redimensiona e converte a imagem para .webp
        String newFileName = multiPartService.processImage(file, "banners", 1200);

        post.setBanner(newFileName);
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(UUID id, UUID authenticatedUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getAuthor().getId().equals(authenticatedUserId)) {
            throw new ForbiddenActionException("You are not authorized to modify this post");
        }

        // deleta a imagem antiga se ela existir e não for um link (pra salvar espaço)
        if (post.getBanner() != null && !post.getBanner().startsWith("http")) {
            multiPartService.deleteImage("banners", post.getBanner());
        }

        postRepository.deleteById(id);
    }
}
