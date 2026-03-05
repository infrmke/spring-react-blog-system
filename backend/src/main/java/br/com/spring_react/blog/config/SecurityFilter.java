package br.com.spring_react.blog.config;

import br.com.spring_react.blog.infra.services.TokenService;
import br.com.spring_react.blog.user.internal.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public SecurityFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        var token = retrieveToken(request);

        if (token != null) {
            var userId = tokenService.validateToken(token);

            if (!userId.isEmpty()) {
                // busca o usuário no banco
                userRepository.findById(UUID.fromString(userId)).ifPresent(user -> {

                    // o usuário é autenticado
                    var authentication = new UsernamePasswordAuthenticationToken(user, null,
                            user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // id é anexado à requisição
                    request.setAttribute("userId", userId);
                });
            }
        }

        // vai para o próximo filtro na corrente
        filterChain.doFilter(request, response);
    }

    // recupera o valor do jwt dentro do cookie de acesso
    private String retrieveToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie -> Cookie.getValue())
                .orElse(null);
    }
}
