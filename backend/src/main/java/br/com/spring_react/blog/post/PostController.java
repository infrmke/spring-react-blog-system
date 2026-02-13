package br.com.spring_react.blog.post;

import br.com.spring_react.blog.post.internal.Post;
import br.com.spring_react.blog.post.internal.PostMapper;
import br.com.spring_react.blog.user.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping // GET /posts
    public ResponseEntity<Object> getAllPosts(@PageableDefault(size = 10, sort = "createdAt",
            direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Post> postsPage = postService.findAllPosts(pageable);

        if (postsPage.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("There are no recorded posts."));
        }

        Page<PostDetailsDTO> dtoPage =
                postsPage.map(post -> PostMapper.toDetailsDTO(post));

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}") // GET /posts/{id}
    public ResponseEntity<Object> getPostById(@PathVariable UUID id) {
        Post post = postService.findById(id);
        return ResponseEntity.ok(PostMapper.toDetailsDTO(post));
    }

    @GetMapping("/slug/{postSlug}") // GET posts/slug/postSlug
    public ResponseEntity<Object> getPostBySlug(@PathVariable String postSlug) {
        Post post = postService.findBySlug(postSlug);
        return ResponseEntity.ok(PostMapper.toDetailsDTO(post));
    }

    @GetMapping("/author/{authorSlug}") // GET /posts/author/authorSlug
    public ResponseEntity<Object> getAllPostsByAuthor(@PathVariable String authorSlug,
                                                      @PageableDefault(size = 10, sort =
                                                              "createdAt", direction =
                                                              Sort.Direction.DESC) Pageable pageable) {
        Page<Post> postsPage = postService.findByAuthor(authorSlug, pageable);

        if (postsPage.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("There are no recorded posts from this " +
                    "author yet."));
        }

        Page<PostDetailsDTO> dtoPage =
                postsPage.map(post -> PostMapper.toDetailsDTO(post));

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/search") // GET /posts/search?title=...
    public ResponseEntity<Object> getAllPostsByTitle(@RequestParam String title,
                                                     @PageableDefault(size = 10, sort =
                                                             "createdAt", direction =
                                                             Sort.Direction.DESC) Pageable pageable) {
        Page<Post> postsPage = postService.findByTitle(title, pageable);

        if (postsPage.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("There are no matching results to this " +
                    "search."));
        }

        Page<PostDetailsDTO> dtoPage =
                postsPage.map(post -> PostMapper.toDetailsDTO(post));

        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping // POST /posts
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createPost(@Valid @RequestBody PostCreateDTO data,
                                             HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId"); // recuperando o id anexado

        Post savedPost = postService.createPost(data, UUID.fromString(userId));

        return ResponseEntity.status(HttpStatus.CREATED).body(PostMapper.toDetailsDTO(savedPost));
    }

    @PatchMapping("/{id}") // PATCH /posts/{id}
    public ResponseEntity<Object> updatePost(@PathVariable("id") UUID postId, HttpServletRequest request,
                                             @RequestBody PostUpdateDTO updateData) {
        String userId = (String) request.getAttribute("userId"); // recuperando o id anexado

        Post updatedPost = postService.updatePost(postId, UUID.fromString(userId), updateData);

        return ResponseEntity.status(HttpStatus.CREATED).body(PostMapper.toDetailsDTO(updatedPost));
    }

    @PatchMapping(value = "/{id}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateBanner(@PathVariable("id") UUID postId,
                                               HttpServletRequest request,
                                               @RequestParam("banner") MultipartFile file) {
        String userId = (String) request.getAttribute("userId");

        Post updatedPost = postService.updateBanner(postId, UUID.fromString(userId), file);

        return ResponseEntity.ok(new PostBannerResponse("Banner updated successfully.",
                updatedPost.getBanner()));
    }

    @DeleteMapping("/{id}") // DELETE /posts/{id}
    public ResponseEntity<Object> deletePost(@PathVariable("id") UUID postId, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId"); // recuperando o id anexado

        postService.deletePost(postId, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
