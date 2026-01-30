package br.com.spring_react.blog.user;

import br.com.spring_react.blog.user.internal.User;
import br.com.spring_react.blog.user.internal.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));
    }

    @Transactional(readOnly = true)
    public User findBySlug(String slug) {
        return userRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("User not found."));
    }

    public User findByEmailForAuth(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials."));
    }

    @Transactional
    public User createUser(UserCreateDTO data) {

        // verifica se o usuário já existe
        if (userRepository.findByEmail(data.email()).isPresent()) {
            throw new RuntimeException("This e-mail already exists.");
        }

        // verifica se as senhas correspondem
        if (!data.password().equals(data.confirmPassword())) {
            throw new RuntimeException("Passwords must match each other.");
        }

        User newUser = new User();
        newUser.setName(data.name());
        newUser.setEmail(data.email());
        newUser.setPassword(passwordEncoder.encode(data.password()));

        return userRepository.save(newUser);
    }

    @Transactional
    public User updateUser(UUID id, UserUpdateDTO data) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // altera nome se "name" não for null
        if (data.name() != null && !data.name().equals(user.getName())) {
            user.setName(data.name());

            // altera o slug do usuário de acordo com a mudança de nome
            String baseSlug = data.name().toLowerCase().replaceAll("[^a-z0-9]", "-");
            String shortId = UUID.randomUUID().toString().split("-")[0];
            user.setSlug(baseSlug + "-" + shortId);
        }

        // altera e-mail se "email" não for null
        if (data.email() != null && !data.email().equals(user.getEmail())) {
            if (userRepository.findByEmail(data.email()).isPresent()) {
                // verifica se o usuário já existe
                throw new RuntimeException("This e-mail already exists.");
            }
            user.setEmail(data.email());
        }

        // altera senha se "password" não for null
        if (data.password() != null) {
            // verifica se as senhas correspondem
            if (!data.password().equals(data.confirmPassword())) {
                throw new RuntimeException("Passwords must match each other.");
            }
            user.setPassword(passwordEncoder.encode(data.password()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found.");
        }
        userRepository.deleteById(id);
    }
}
