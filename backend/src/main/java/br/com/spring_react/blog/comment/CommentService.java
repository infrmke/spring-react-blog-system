package br.com.spring_react.blog.comment;

import br.com.spring_react.blog.comment.dto.CommentCreateDTO;
import br.com.spring_react.blog.comment.dto.CommentUpdateDTO;
import br.com.spring_react.blog.comment.internal.Comment;
import br.com.spring_react.blog.comment.internal.CommentRepository;
import br.com.spring_react.blog.infra.exceptions.ForbiddenActionException;
import br.com.spring_react.blog.infra.exceptions.ResourceNotFoundException;
import br.com.spring_react.blog.post.PostService;
import br.com.spring_react.blog.post.internal.Post;
import br.com.spring_react.blog.user.UserService;
import br.com.spring_react.blog.user.internal.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, PostService postService, UserService userService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<Comment> findAllByPostId(UUID postId, Pageable pageable) {
        postService.findById(postId);
        return commentRepository.findAllByPostId(postId, pageable);
    }

    @Transactional(readOnly = true)
    public Comment findById(UUID id) {
        return commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Comment not found"));
    }

    @Transactional
    public Comment createComment(UUID postId, CommentCreateDTO data, UUID authenticatedUserId) {
        Post post = postService.findById(postId);
        User author = userService.findById(authenticatedUserId);

        Comment comment = new Comment();
        comment.setContent(data.content());
        comment.setPost(post);
        comment.setAuthor(author);

        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(UUID commentId, CommentUpdateDTO data, UUID authenticatedUserId) {
        Comment comment =
                commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException(
                        "Comment not found"));

        if (!comment.getAuthor().getId().equals(authenticatedUserId)) {
            throw new ForbiddenActionException("You are not authorized to modify this comment");
        }

        if(data.content() != null) {
            comment.setContent(data.content());
        }

        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID authenticatedUserId) {
        Comment comment =
                commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException(
                "Comment not found"));

        if (!comment.getAuthor().getId().equals(authenticatedUserId)) {
            throw new ForbiddenActionException("You are not authorized to modify this comment");
        }

        commentRepository.delete(comment);
    }
}
