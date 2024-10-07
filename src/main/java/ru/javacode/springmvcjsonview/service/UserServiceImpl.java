package ru.javacode.springmvcjsonview.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.javacode.springmvcjsonview.exception.ResourceNotFoundException;
import ru.javacode.springmvcjsonview.model.User;
import ru.javacode.springmvcjsonview.repository.UserRepository;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, User user) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(
                "User with id " + userId + " not found"));
        return userRepository.save(updateRows(userToUpdate, user));
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(
                "User with id " + userId + " not found"));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private User updateRows(User updatedUser, User userToUpdate) {

        if (userToUpdate.getName() != null) {
            updatedUser.setName(userToUpdate.getName());
        }
        if (userToUpdate.getEmail() != null) {
            updatedUser.setEmail(userToUpdate.getEmail());
        }
        if (userToUpdate.getPassword() != null) {
            updatedUser.setPassword(userToUpdate.getPassword());
        }

        if (userToUpdate.getPassword().matches("^\\$2[ayb]\\$.{56}$")) {
            updatedUser.setPassword(userToUpdate.getPassword());
        } else {
            String encodedPassword = passwordEncoder.encode(userToUpdate.getPassword());
            updatedUser.setPassword(encodedPassword);
        }
        return updatedUser;
    }
}
