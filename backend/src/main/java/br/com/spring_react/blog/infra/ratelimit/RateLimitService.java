package br.com.spring_react.blog.infra.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // instanciação do ConcurrentHashMap para armazenar cada tipo de rate limiter
    private final Map<RateLimitType, Map<String, Bucket>> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(RateLimitType type, String identifier) {
        return cache
                // verifica se já existe um ConcurrentHashMap para o tipo de limiter dado
                .computeIfAbsent(type, k -> new ConcurrentHashMap<>())
                // verifica se já existe um bucket para o IP fornecido
                .computeIfAbsent(identifier, id -> createNewBucket(type));
    }

    // cria um novo bucket com os atributos do rate limiter especificado
    private Bucket createNewBucket(RateLimitType type) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(type.getCapacity())
                        .refillIntervally(type.getCapacity(), type.getRefillDuration())
                        .build())
                .build();
    }
}
