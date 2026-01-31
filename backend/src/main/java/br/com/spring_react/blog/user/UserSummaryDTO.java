package br.com.spring_react.blog.user;

import java.util.UUID;

public record UserSummaryDTO(UUID id, String name, String slug) {
}
