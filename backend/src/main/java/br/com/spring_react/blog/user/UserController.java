package br.com.spring_react.blog.user;

import br.com.spring_react.blog.infra.ratelimit.RateLimit;
import br.com.spring_react.blog.infra.ratelimit.RateLimitType;
import br.com.spring_react.blog.user.dto.UserCreateDTO;
import br.com.spring_react.blog.user.dto.UserDTO;
import br.com.spring_react.blog.user.dto.UserUpdateDTO;
import br.com.spring_react.blog.user.internal.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping // GET /users
    @Operation(summary = "Lista todos os usuários registrados", description = "Retorna os dados " +
            "básicos de todos os usuários registrados")
    public ResponseEntity<Object> getAllUsers(@PageableDefault(size = 10, sort = "createdAt",
            direction = Sort.Direction.DESC) Pageable pageable) {
        Page<User> usersPage = userService.findAllUsers(pageable);

        Page<UserDTO> dtoPage = usersPage.map(user ->
                new UserDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getAvatar(),
                        user.getSlug(),
                        user.getRole()
                )
        );
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}") // GET /users/id
    @Operation(summary = "Lista o usuário solicitado por ID", description = "Retorna os dados " +
            "básicos do usuário associado ao ID providenciado")
    public ResponseEntity<Object> getUserById(@PathVariable UUID id) {
        User user = userService.findById(id);

        return ResponseEntity.ok(
                new UserDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getAvatar(),
                        user.getSlug(),
                        user.getRole()
                )
        );
    }

    @GetMapping("/profile/{userSlug}") // GET /users/profile/userSlug
    @Operation(summary = "Lista o usuário solicitado por slug", description = "Retorna os dados " +
            "básicos do usuário associado ao slug providenciado")
    public ResponseEntity<Object> getUserBySlug(@PathVariable String userSlug) {
        User user = userService.findBySlug(userSlug);

        return ResponseEntity.ok(
                new UserDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getAvatar(),
                    user.getSlug(),
                    user.getRole()
                )
        );
    }

    @PostMapping // POST /users
    @RateLimit(type = RateLimitType.SIGNUP)
    @Operation(summary = "Cria um novo usuário", description = "Cria um novo usuário no banco de " +
            "dados com role padrão \"USER\" e avatar gerado a partir de suas iniciais")
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserCreateDTO user) {
        User savedUser = userService.createUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new UserDTO(
                            savedUser.getId(),
                            savedUser.getName(),
                            savedUser.getEmail(),
                            savedUser.getAvatar(),
                            savedUser.getSlug(),
                            savedUser.getRole()
                        )
                );
    }

    @PatchMapping("/{id}") // PATCH /users/id
    @Operation(summary = "Atualiza o usuário informado por ID", description = "Atualiza os dados " +
            "básicos do usuário associado ao ID providenciado")
    public ResponseEntity<Object> updateUser(@PathVariable UUID id, HttpServletRequest request,
                                             @Valid @RequestBody UserUpdateDTO updateData) {
        String userId = (String) request.getAttribute("userId"); // recuperando o id anexado
        User updatedUser = userService.updateUser(id, UUID.fromString(userId), updateData);

        return ResponseEntity.ok((Object)
                new UserDTO(
                    updatedUser.getId(),
                    updatedUser.getName(),
                    updatedUser.getEmail(),
                    updatedUser.getAvatar(),
                    updatedUser.getSlug(),
                    updatedUser.getRole()
                )
        );
    }

    @PatchMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Atualiza o avatar do usuário informado por ID", description = "Atualiza" +
            " o avatar do usuário associado ao ID providenciado, excluindo o arquivo binário " +
            "antigo caso o mesmo exista")
    public ResponseEntity<Object> updateAvatar(@PathVariable UUID id,
                                               HttpServletRequest request,
                                               @RequestParam("avatar") MultipartFile file) {
        String userId = (String) request.getAttribute("userId"); // recuperando o id anexado
        userService.updateAvatar(id, UUID.fromString(userId), file);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}") // DELETE /users/id
    @Operation(summary = "Exclui os dados do usuário informado por ID", description = "Deleta o " +
            "usuário associado ao ID providenciado, incluindo dados, avatar, publicações, " +
            "comentários e curtidas")
    public ResponseEntity<Object> deleteUser(@PathVariable UUID id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId"); // recuperando o id anexado
        userService.deleteUser(id, UUID.fromString(userId));

        return ResponseEntity.noContent().build();
    }
}
