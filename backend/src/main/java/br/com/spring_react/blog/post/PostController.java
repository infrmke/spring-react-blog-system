package br.com.spring_react.blog.post;

import br.com.spring_react.blog.post.internal.Post;
import br.com.spring_react.blog.user.MessageResponse;
import br.com.spring_react.blog.user.UserSummaryDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping // GET /posts
    public ResponseEntity<Object> getAllPosts() {
        List<Post> posts = postService.findAllPosts();

        if (posts.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("There are no recorded posts."));
        }

        List<PostDetailsDTO> dtos = posts.stream().map(post -> {
            UserSummaryDTO authorSummary = new UserSummaryDTO(
                    post.getAuthor().getId(),
                    post.getAuthor().getName(),
                    post.getAuthor().getSlug()
            );

            return new PostDetailsDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getSummary(),
                    post.getContent(),
                    post.getBanner(),
                    post.getSlug(),
                    authorSummary,
                    post.getCreatedAt(),
                    post.getUpdatedAt()
            );
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}") // GET /posts/{id}
    public ResponseEntity<Object> getPostById(@PathVariable UUID id) {
        try {
            Post post = postService.findById(id);

            UserSummaryDTO authorSummary = new UserSummaryDTO(
                    post.getAuthor().getId(),
                    post.getAuthor().getName(),
                    post.getAuthor().getSlug()
            );

            return ResponseEntity.ok(new PostDetailsDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getSummary(),
                    post.getContent(),
                    post.getBanner(),
                    post.getSlug(),
                    authorSummary,
                    post.getCreatedAt(),
                    post.getUpdatedAt()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/slug/{postSlug}") // GET posts/slug/postSlug
    public ResponseEntity<Object> getPostBySlug(@PathVariable String postSlug) {
        try {
            Post post = postService.findBySlug(postSlug);

            UserSummaryDTO authorSummary = new UserSummaryDTO(
                    post.getAuthor().getId(),
                    post.getAuthor().getName(),
                    post.getAuthor().getSlug()
            );

            return ResponseEntity.ok(new PostDetailsDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getSummary(),
                    post.getContent(),
                    post.getBanner(),
                    post.getSlug(),
                    authorSummary,
                    post.getCreatedAt(),
                    post.getUpdatedAt()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/author/{authorSlug}") // GET /posts/author/authorSlug
    public ResponseEntity<Object> getAllPostsByAuthor(@PathVariable String authorSlug) {
        List<Post> posts = postService.findByAuthor(authorSlug);

        if (posts.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("There are no recorded posts from this " +
                    "author yet."));
        }

        List<PostDetailsDTO> dtos = posts.stream().map(post -> {
            UserSummaryDTO authorSummary = new UserSummaryDTO(
                    post.getAuthor().getId(),
                    post.getAuthor().getName(),
                    post.getAuthor().getSlug()
            );

            return new PostDetailsDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getSummary(),
                    post.getContent(),
                    post.getBanner(),
                    post.getSlug(),
                    authorSummary,
                    post.getCreatedAt(),
                    post.getUpdatedAt()
            );
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search") // GET /posts/search?title=...
    public ResponseEntity<Object> getAllPostsByTitle(@RequestParam String title) {
        List<Post> posts = postService.findByTitle(title);

        if (posts.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("There are no matching results to this " +
                    "search."));
        }

        List<PostDetailsDTO> dtos = posts.stream().map(post -> {
            UserSummaryDTO authorSummary = new UserSummaryDTO(
                    post.getAuthor().getId(),
                    post.getAuthor().getName(),
                    post.getAuthor().getSlug()
            );

            return new PostDetailsDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getSummary(),
                    post.getContent(),
                    post.getBanner(),
                    post.getSlug(),
                    authorSummary,
                    post.getCreatedAt(),
                    post.getUpdatedAt()
            );
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping // POST /posts
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createPost(@Valid @RequestBody PostCreateDTO data,
                                             HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId"); // recuperando o id anexado

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(
                        "User " +
                        "not authenticated."));
            }

            Post savedPost = postService.createPost(data, UUID.fromString(userId));

            UserSummaryDTO authorSummary = new UserSummaryDTO(
                    savedPost.getAuthor().getId(),
                    savedPost.getAuthor().getName(),
                    savedPost.getAuthor().getSlug()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(new PostDetailsDTO(savedPost.getId(),
                    savedPost.getTitle(),
                    savedPost.getSummary(),
                    savedPost.getContent(),
                    savedPost.getBanner(),
                    savedPost.getSlug(),
                    authorSummary,
                    savedPost.getCreatedAt(),
                    savedPost.getUpdatedAt()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}") // PATCH /posts/{id}
    public ResponseEntity<Object> updatePost(@PathVariable UUID id,
                                             @RequestBody PostUpdateDTO updateData,
                                             HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId"); // recuperando o id anexado

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("User not authenticated."));
            }

            Post updatedPost = postService.updatePost(id, updateData);

            UserSummaryDTO authorSummary = new UserSummaryDTO(
                    updatedPost.getAuthor().getId(),
                    updatedPost.getAuthor().getName(),
                    updatedPost.getAuthor().getSlug()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(new PostDetailsDTO(updatedPost.getId(),
                    updatedPost.getTitle(),
                    updatedPost.getSummary(),
                    updatedPost.getContent(),
                    updatedPost.getBanner(),
                    updatedPost.getSlug(),
                    authorSummary,
                    updatedPost.getCreatedAt(),
                    updatedPost.getUpdatedAt()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}") // DELETE /posts/{id}
    public ResponseEntity<Object> deletePost(@PathVariable UUID id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }
}
