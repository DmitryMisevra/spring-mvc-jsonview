package ru.javacode.springmvcjsonview.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.javacode.springmvcjsonview.exception.ResourceNotFoundException;
import ru.javacode.springmvcjsonview.model.User;
import ru.javacode.springmvcjsonview.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
    }

    // Вспомогательный метод для создания тестового пользователя
    private User createTestUser() {
        return User.builder()
                .userId(1L)
                .name("testuser")
                .email("testuser@example.com")
                .orders(List.of()) // Инициализируем пустым списком
                .build();
    }

    @Nested
    @DisplayName("Тесты для метода createUser")
    class CreateUserTests {

        @Test
        @DisplayName("Успешное создание пользователя")
        void createUser_Success() {
            // Arrange
            User userToSave = User.builder()
                    .name(testUser.getUsername())
                    .email(testUser.getEmail())
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            User createdUser = userService.createUser(userToSave);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getUserId()).isNull(); // Должен быть сброшен перед сохранением
            assertThat(capturedUser.getUsername()).isEqualTo(userToSave.getUsername());
            assertThat(capturedUser.getEmail()).isEqualTo(userToSave.getEmail());

            assertThat(createdUser).isNotNull();
            assertThat(createdUser.getUserId()).isNotNull(); // Должен быть сгенерирован
            assertThat(createdUser.getUsername()).isEqualTo(testUser.getUsername());
            assertThat(createdUser.getEmail()).isEqualTo(testUser.getEmail());
        }
    }

    @Nested
    @DisplayName("Тесты для метода updateUser")
    class UpdateUserTests {

        @Test
        @DisplayName("Успешное обновление пользователя")
        void updateUser_Success() {
            // Arrange
            Long userId = testUser.getUserId();
            User updatedInfo = User.builder()
                    .name("updatedUser")
                    .email("updated@example.com")
                    .build();

            // Создаем объект пользователя с обновленной информацией
            User updatedUser = User.builder()
                    .userId(userId)
                    .name(updatedInfo.getUsername())
                    .email(updatedInfo.getEmail())
                    .orders(testUser.getOrders()) // Сохраняем заказы
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            // Act
            User result = userService.updateUser(userId, updatedInfo);

            // Assert
            verify(userRepository, times(1)).findById(userId);

            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getUsername()).isEqualTo(updatedInfo.getUsername());
            assertThat(result.getEmail()).isEqualTo(updatedInfo.getEmail());
        }

        @Test
        @DisplayName("Неудачное обновление пользователя - пользователь не найден")
        void updateUser_UserNotFound() {
            // Arrange
            Long userId = 1L;
            User updatedInfo = User.builder()
                    .name("updatedUser")
                    .email("updated@example.com")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.updateUser(userId, updatedInfo))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, times(1)).findById(userId);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Тесты для метода getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("Успешное получение пользователя по ID")
        void getUserById_Success() {
            // Arrange
            Long userId = testUser.getUserId();
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // Act
            User foundUser = userService.getUserById(userId);

            // Assert
            verify(userRepository, times(1)).findById(userId);
            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getUserId()).isEqualTo(userId);
            assertThat(foundUser.getUsername()).isEqualTo(testUser.getUsername());
            assertThat(foundUser.getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Неудачное получение пользователя - пользователь не найден")
        void getUserById_UserNotFound() {
            // Arrange
           Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(ResourceNotFoundException.class); // Возможно, опечатка в сообщении
        }
    }

    @Nested
    @DisplayName("Тесты для метода deleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("Успешное удаление пользователя")
        void deleteUser_Success() {
            // Arrange
            Long userId = testUser.getUserId();

            // Act
            userService.deleteUser(userId);

            // Assert
            verify(userRepository, times(1)).deleteById(userId);
        }

        @Nested
        @DisplayName("Тесты для метода getAllUsers")
        class GetAllUsersTests {

            @Test
            @DisplayName("Успешное получение списка пользователей")
            void getAllUsers_Success() {
                // Arrange
                User user1 = testUser;
                User user2 = createTestUser();
                user2.setUserId(2L);
                user2.setName("anotherUser");
                user2.setEmail("another@example.com");

                List<User> users = Arrays.asList(user1, user2);
                when(userRepository.findAll()).thenReturn(users);

                // Act
                List<User> result = userService.getAllUsers();

                // Assert
                verify(userRepository, times(1)).findAll();
                assertThat(result).isNotNull();
                assertThat(result).hasSize(2);
                assertThat(result).containsExactlyInAnyOrder(user1, user2);
            }

            @Test
            @DisplayName("Успешное получение пустого списка пользователей")
            void getAllUsers_EmptyList() {
                // Arrange
                when(userRepository.findAll()).thenReturn(Arrays.asList());

                // Act
                List<User> result = userService.getAllUsers();

                // Assert
                verify(userRepository, times(1)).findAll();
                assertThat(result).isNotNull();
                assertThat(result).isEmpty();
            }
        }
    }
}