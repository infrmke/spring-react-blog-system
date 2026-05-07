package br.com.spring_react.blog.session;

import br.com.spring_react.blog.infra.services.TokenService;
import br.com.spring_react.blog.session.dto.LoginRequestDTO;
import br.com.spring_react.blog.user.UserService;
import br.com.spring_react.blog.user.internal.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final UserService userService;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder;

    public SessionService(UserService userService, TokenService tokenService,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public String authenticate(LoginRequestDTO data) {
        User user = userService.findByEmailForAuth(data.email());

        if (!passwordEncoder.matches(data.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return tokenService.generateToken(user);
    }
}
