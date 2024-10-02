package ru.javacode.springmvcjsonview.service;

import ru.javacode.springmvcjsonview.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User createUser(User user);

    User updateUser(UUID userId, User user);

    User getUserById(UUID userId);

    void deleteUser(UUID userId);

    List<User> getAllUsers();
}
