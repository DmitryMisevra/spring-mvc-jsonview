package ru.javacode.springmvcjsonview.security.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.javacode.springmvcjsonview.model.User;
import ru.javacode.springmvcjsonview.model.Role;
import ru.javacode.springmvcjsonview.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
            User admin = User.builder()
                    .name("admin")
                    .email("admin@admin.com")
                    .password(passwordEncoder.encode("0000")) // Хеширование пароля
                    .role(Role.SUPER_ADMIN)
                    .isAccountNonLocked(true)
                    .build();
            userRepository.save(admin);
            System.out.println("Администратор был создан.");
        }
    }
}
