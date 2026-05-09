package br.com.spring_react.blog.infra.ratelimit;

import br.com.spring_react.blog.infra.exceptions.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // não aplica o limiter se o objeto não for uma função do controller
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // pega a anotação da função que vai ser executada
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        // define o type do limiter utilizando ou o que foi passado na anotação ou o padrão
        RateLimitType type = (rateLimit != null) ? rateLimit.type() : RateLimitType.GLOBAL;

        String identifier = request.getRemoteAddr(); // IP por padrão
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // se o usuário estiver autenticado (e não for anônimo) usa o e-mail dele como id
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            identifier = auth.getName();
        }

        // passa o id para o service
        Bucket bucket = rateLimitService.resolveBucket(type, identifier);

        // tenta consumir 1 token do bucket
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        // draft headers da IETF para rate limit
        response.addHeader("RateLimit-Limit", String.valueOf(type.getCapacity()));
        response.addHeader("RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        response.addHeader("RateLimit-Reset",
                String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));

        if (probe.isConsumed()) {
            return true;
        }

        // se o limite for excedido, dá 429
        throw new RateLimitExceededException("Too many requests. Please try again later.");
    }
}
