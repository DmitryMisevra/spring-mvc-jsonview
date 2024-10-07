package ru.javacode.springmvcjsonview.service;

import ru.javacode.springmvcjsonview.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User createUser(User user);

    User updateUser(Long userId, User user);

    User getUserById(Long userId);

    void deleteUser(Long userId);

    List<User> getAllUsers();
}
