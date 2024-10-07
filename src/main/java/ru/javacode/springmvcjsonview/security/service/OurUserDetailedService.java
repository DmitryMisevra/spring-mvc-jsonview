package ru.javacode.springmvcjsonview.security.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import ru.javacode.springmvcjsonview.exception.ResourceNotFoundException;
import ru.javacode.springmvcjsonview.model.User;
import ru.javacode.springmvcjsonview.repository.UserRepository;

@Service
@AllArgsConstructor
public class OurUserDetailedService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public ru.javacode.springmvcjsonview.model.User loadUserByUsername(String username)
            throws ResourceNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с таким email не найден"));
    }

    public void unlockWhenTimeExpired(User user) {
        Long lockTimeInMillis = user.getLockTime();
        Long currentTimeInMillis = System.currentTimeMillis();

        if (user.isAccountNonLocked() && lockTimeInMillis != null) {
            long lockDuration = 15 * 60 * 1000; // 15 минут
            if (currentTimeInMillis - lockTimeInMillis > lockDuration) {
                user.setAccountNonLocked(true);
                user.setLockTime(null);
                user.setFailedAttempts(0);
                userRepository.save(user);
            }
        }
    }
}
