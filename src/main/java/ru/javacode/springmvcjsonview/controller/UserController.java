package ru.javacode.springmvcjsonview.controller;


import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.javacode.springmvcjsonview.model.User;
import ru.javacode.springmvcjsonview.service.UserService;
import ru.javacode.springmvcjsonview.view.Views;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @JsonView(Views.UserSummary.class)
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @JsonView(Views.UserSummary.class)
    public ResponseEntity<User> updateUser(@PathVariable Long userId,
                                           @Valid @RequestBody User user) {
        User updatedUser = userService.updateUser(userId, user);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping(path = "/{userId}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @JsonView(Views.UserDetails.class)
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping(path = "/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @JsonView(Views.UserSummary.class)
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/unlock/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> unlockUserAccount(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);
        user.setLockTime(null);
        userService.updateUser(userId, user);
        return ResponseEntity.ok(user);
    }
}
