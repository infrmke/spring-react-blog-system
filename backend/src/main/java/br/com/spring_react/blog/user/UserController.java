package br.com.spring_react.blog.user;

import br.com.spring_react.blog.user.internal.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping // GET /users
    public ResponseEntity<Object> getAllUsers(@PageableDefault(size = 10, sort = "createdAt",
            direction = Sort.Direction.DESC) Pageable pageable) {
        Page<User> usersPage = userService.findAllUsers(pageable);

        if (usersPage.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("There are no registered users."));
        }

        Page<UserDTO> dtoPage = usersPage.map(user -> new UserDTO(user.getId(), user.getName(),
                user.getEmail(),
                user.getAvatar(), user.getSlug(), user.getRole()));

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}") // GET /users/id
    public ResponseEntity<Object> getUserById(@PathVariable UUID id) {
        User user = userService.findById(id);

        return ResponseEntity.ok(new UserDTO(user.getId(), user.getName(), user.getEmail(),
                user.getAvatar(), user.getSlug(), user.getRole()));
    }

    @GetMapping("/profile/{slug}") // GET /users/profile/slug
    public ResponseEntity<Object> getUserBySlug(@PathVariable String slug) {
        User user = userService.findBySlug(slug);

        return ResponseEntity.ok(new UserDTO(user.getId(), user.getName(), user.getEmail(),
                user.getAvatar(), user.getSlug(), user.getRole()));
    }

    @PostMapping // POST /users
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserCreateDTO user) {
        User savedUser = userService.createUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new UserDTO(
                savedUser.getId(), savedUser.getName(), savedUser.getEmail(),
                savedUser.getAvatar(), savedUser.getSlug(), savedUser.getRole()));
    }

    @PatchMapping("/{id}") // PATCH /users/id
    public ResponseEntity<Object> updateUser(@PathVariable UUID id, HttpServletRequest request,
                                             @Valid @RequestBody UserUpdateDTO updateData) {
        String userId = (String) request.getAttribute("userId"); // recuperando o id anexado

        User updatedUser = userService.updateUser(id, UUID.fromString(userId), updateData);

        return ResponseEntity.ok((Object) new UserDTO(
                updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail(),
                updatedUser.getAvatar(), updatedUser.getSlug(), updatedUser.getRole()));
    }

    @DeleteMapping("/{id}") // DELETE /users/id
    public ResponseEntity<Object> deleteUser(@PathVariable UUID id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId"); // recuperando o id anexado

        userService.deleteUser(id, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
