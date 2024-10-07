package ru.javacode.springmvcjsonview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.javacode.springmvcjsonview.exception.ResourceNotFoundException;
import ru.javacode.springmvcjsonview.model.Order;
import ru.javacode.springmvcjsonview.model.User;
import ru.javacode.springmvcjsonview.service.UserService;
import ru.javacode.springmvcjsonview.view.Views;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // Вспомогательный метод для создания тестового пользователя
    private User createTestUser() {
        return User.builder()
                .userId(1L)
                .name("testuser")
                .email("testuser@example.com")
                .orders(Collections.emptyList()) // Инициализируем пустым списком
                .build();
    }

    @Nested
    @DisplayName("Тесты для POST /api/v1/users")
    class CreateUserTests {

        @Test
        @DisplayName("Успешное создание пользователя")
        void createUser_Success() throws Exception {
            User user = createTestUser();
            User userToSave = User.builder()
                    .name(user.getUsername())
                    .email(user.getEmail())
                    .build();
            when(userService.createUser(any(User.class))).thenReturn(user);

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userToSave)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId", is(user.getUserId().toString())))
                    .andExpect(jsonPath("$.username", is(user.getUsername())))
                    .andExpect(jsonPath("$.email", is(user.getEmail())))
                    .andExpect(jsonPath("$.orders").doesNotExist());
        }

        @Test
        @DisplayName("Неудачное создание пользователя из-за валидации")
        void createUser_ValidationFailure() throws Exception {
            User invalidUser = User.builder()
                    .email("invalid-email") // Неверный формат email
                    .build();

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidUser)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Тесты для PUT /api/v1/users/{userId}")
    class UpdateUserTests {

        @Test
        @DisplayName("Успешное обновление пользователя")
        void updateUser_Success() throws Exception {
            Long userId = 1L;
            User existingUser = createTestUser();
            User updatedUser = User.builder()
                    .userId(userId)
                    .name("updatedUser")
                    .email("updated@example.com")
                    .orders(Collections.emptyList())
                    .build();

            when(userService.updateUser(ArgumentMatchers.eq(userId), any(User.class))).thenReturn(updatedUser);

            mockMvc.perform(put("/api/v1/users/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedUser)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId", is(updatedUser.getUserId().toString())))
                    .andExpect(jsonPath("$.username", is(updatedUser.getUsername())))
                    .andExpect(jsonPath("$.email", is(updatedUser.getEmail())))
                    .andExpect(jsonPath("$.orders").doesNotExist()); // В UserSummary заказы не отображаются
        }

        @Test
        @DisplayName("Неудачное обновление пользователя - пользователь не найден")
        void updateUser_NotFound() throws Exception {
            Long userId = 1L;
            User updatedUser = User.builder()
                    .name("updatedUser")
                    .email("updated@example.com")
                    .build();

            when(userService.updateUser(ArgumentMatchers.eq(userId), any(User.class)))
                    .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

            mockMvc.perform(put("/api/v1/users/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedUser)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("User with id " + userId + " not found")));
        }

        @Test
        @DisplayName("Неудачное обновление пользователя из-за валидации")
        void updateUser_ValidationFailure() throws Exception {
            UUID userId = UUID.randomUUID();
            User invalidUser = User.builder()
                    .name("") // Пустое имя
                    .email("invalid-email") // Неверный формат email
                    .build();

            mockMvc.perform(put("/api/v1/users/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidUser)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Тесты для GET /api/v1/users/{userId}")
    class GetUserByIdTests {

        @Test
        @DisplayName("Успешное получение пользователя по ID")
        void getUserById_Success() throws Exception {
            Long userId = 1L;
            List<Order> orders = Arrays.asList();
            User user = User.builder()
                    .userId(userId)
                    .name("testuser")
                    .email("testuser@example.com")
                    .orders(orders)
                    .build();

            when(userService.getUserById(userId)).thenReturn(user);

            mockMvc.perform(get("/api/v1/users/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId", is(user.getUserId().toString())))
                    .andExpect(jsonPath("$.username", is(user.getUsername())))
                    .andExpect(jsonPath("$.email", is(user.getEmail())))
                    .andExpect(jsonPath("$.orders", hasSize(orders.size())));
        }

        @Test
        @DisplayName("Неудачное получение пользователя - не найдено")
        void getUserById_NotFound() throws Exception {
            Long userId = 1L;

            when(userService.getUserById(userId))
                    .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

            mockMvc.perform(get("/api/v1/users/{userId}", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("User with id " + userId + " not found")));
        }
    }

    @Nested
    @DisplayName("Тесты для DELETE /api/v1/users/{userId}")
    class DeleteUserTests {

        @Test
        @DisplayName("Успешное удаление пользователя")
        void deleteUser_Success() throws Exception {
            Long userId = 1L;
            doNothing().when(userService).deleteUser(userId);

            mockMvc.perform(delete("/api/v1/users/{userId}", userId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Неудачное удаление пользователя - не найдено")
        void deleteUser_NotFound() throws Exception {
            Long userId = 1L;

            // Предполагаем, что при попытке удалить несуществующего пользователя, сервис бросает ResourceNotFoundException
            org.mockito.Mockito.doThrow(new ResourceNotFoundException("User with id " + userId + " not found"))
                    .when(userService).deleteUser(userId);

            mockMvc.perform(delete("/api/v1/users/{userId}", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("User with id " + userId + " not found")));
        }
    }

    @Nested
    @DisplayName("Тесты для GET /api/v1/users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Успешное получение списка пользователей")
        void getAllUsers_Success() throws Exception {
            User user1 = createTestUser();
            User user2 = User.builder()
                    .userId(2L)
                    .name("anotherUser")
                    .email("another@example.com")
                    .orders(Collections.emptyList())
                    .build();
            List<User> users = Arrays.asList(user1, user2);

            when(userService.getAllUsers()).thenReturn(users);

            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].userId", is(user1.getUserId().toString())))
                    .andExpect(jsonPath("$[0].username", is(user1.getUsername())))
                    .andExpect(jsonPath("$[0].email", is(user1.getEmail())))
                    .andExpect(jsonPath("$[0].orders").doesNotExist()) // В UserSummary заказы не отображаются
                    .andExpect(jsonPath("$[1].userId", is(user2.getUserId().toString())))
                    .andExpect(jsonPath("$[1].username", is(user2.getUsername())))
                    .andExpect(jsonPath("$[1].email", is(user2.getEmail())))
                    .andExpect(jsonPath("$[1].orders").doesNotExist());
        }

        @Test
        @DisplayName("Успешное получение пустого списка пользователей")
        void getAllUsers_EmptyList() throws Exception {
            when(userService.getAllUsers()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}