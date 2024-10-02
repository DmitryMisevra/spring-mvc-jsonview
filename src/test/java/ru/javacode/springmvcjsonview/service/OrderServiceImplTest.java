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
import org.springframework.dao.EmptyResultDataAccessException;
import ru.javacode.springmvcjsonview.exception.ResourceNotFoundException;
import ru.javacode.springmvcjsonview.model.Order;
import ru.javacode.springmvcjsonview.model.OrderStatus;
import ru.javacode.springmvcjsonview.model.User;
import ru.javacode.springmvcjsonview.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = createTestOrder();
    }

    // Вспомогательный метод для создания тестового заказа
    private Order createTestOrder() {
        return Order.builder()
                .orderId(UUID.randomUUID())
                .amount(new BigDecimal("99.99"))
                .orderStatus(OrderStatus.PROCESSING)
                .user(null) // В OrderSummary user не отображается
                .build();
    }

    // Вспомогательный метод для создания детального заказа с пользователем
    private Order createDetailedTestOrder() {
        User user = User.builder()
                .userId(UUID.randomUUID())
                .username("testuser")
                .email("testuser@example.com")
                .orders(Collections.emptyList())
                .build();

        return Order.builder()
                .orderId(UUID.randomUUID())
                .amount(new BigDecimal("149.99"))
                .orderStatus(OrderStatus.DELIVERY)
                .user(user)
                .build();
    }

    @Nested
    @DisplayName("Тесты для метода createOrder")
    class CreateOrderTests {

        @Test
        @DisplayName("Успешное создание заказа")
        void createOrder_Success() {
            // Arrange
            Order orderToSave = Order.builder()
                    .amount(testOrder.getAmount())
                    .orderStatus(testOrder.getOrderStatus())
                    .user(testOrder.getUser())
                    .build();

            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // Act
            Order createdOrder = orderService.createOrder(orderToSave);

            // Assert
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository, times(1)).save(orderCaptor.capture());

            Order capturedOrder = orderCaptor.getValue();
            assertThat(capturedOrder.getOrderId()).isNull(); // Должен быть сброшен перед сохранением
            assertThat(capturedOrder.getAmount()).isEqualTo(orderToSave.getAmount());
            assertThat(capturedOrder.getOrderStatus()).isEqualTo(orderToSave.getOrderStatus());
            assertThat(capturedOrder.getUser()).isEqualTo(orderToSave.getUser());

            assertThat(createdOrder).isNotNull();
            assertThat(createdOrder.getOrderId()).isNotNull(); // Должен быть сгенерирован
            assertThat(createdOrder.getAmount()).isEqualTo(testOrder.getAmount());
            assertThat(createdOrder.getOrderStatus()).isEqualTo(testOrder.getOrderStatus());
        }
    }

    @Nested
    @DisplayName("Тесты для метода updateOrder")
    class UpdateOrderTests {

        @Test
        @DisplayName("Успешное обновление заказа")
        void updateOrder_Success() {
            // Arrange
            UUID orderId = testOrder.getOrderId();
            Order updatedInfo = Order.builder()
                    .amount(new BigDecimal("199.99"))
                    .orderStatus(OrderStatus.DELIVERY)
                    .build();

            Order updatedOrder = Order.builder()
                    .orderId(orderId)
                    .amount(updatedInfo.getAmount())
                    .orderStatus(updatedInfo.getOrderStatus())
                    .user(testOrder.getUser())
                    .build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

            // Act
            Order result = orderService.updateOrder(orderId, updatedInfo);

            // Assert
            verify(orderRepository, times(1)).findById(orderId);
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository, times(1)).save(orderCaptor.capture());

            Order capturedOrder = orderCaptor.getValue();
            assertThat(capturedOrder.getAmount()).isEqualTo(updatedInfo.getAmount());
            assertThat(capturedOrder.getOrderStatus()).isEqualTo(updatedInfo.getOrderStatus());
            assertThat(capturedOrder.getUser()).isEqualTo(testOrder.getUser());

            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);
            assertThat(result.getAmount()).isEqualTo(updatedInfo.getAmount());
            assertThat(result.getOrderStatus()).isEqualTo(updatedInfo.getOrderStatus());
            assertThat(result.getUser()).isEqualTo(testOrder.getUser());
        }

        @Test
        @DisplayName("Неудачное обновление заказа - заказ не найден")
        void updateOrder_OrderNotFound() {
            // Arrange
            UUID orderId = UUID.randomUUID();
            Order updatedInfo = Order.builder()
                    .amount(new BigDecimal("199.99"))
                    .orderStatus(OrderStatus.DELIVERY)
                    .build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.updateOrder(orderId, updatedInfo))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order with id " + orderId + " not found");

            verify(orderRepository, times(1)).findById(orderId);
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Тесты для метода getOrderById")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Успешное получение заказа по ID")
        void getOrderById_Success() {
            // Arrange
            UUID orderId = testOrder.getOrderId();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

            // Act
            Order foundOrder = orderService.getOrderById(orderId);

            // Assert
            verify(orderRepository, times(1)).findById(orderId);
            assertThat(foundOrder).isNotNull();
            assertThat(foundOrder.getOrderId()).isEqualTo(orderId);
            assertThat(foundOrder.getAmount()).isEqualTo(testOrder.getAmount());
            assertThat(foundOrder.getOrderStatus()).isEqualTo(testOrder.getOrderStatus());
            assertThat(foundOrder.getUser()).isEqualTo(testOrder.getUser());
        }

        @Test
        @DisplayName("Неудачное получение заказа - заказ не найден")
        void getOrderById_OrderNotFound() {
            // Arrange
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.getOrderById(orderId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order with id " + orderId + " not found");

            verify(orderRepository, times(1)).findById(orderId);
        }
    }

    @Nested
    @DisplayName("Тесты для метода getAllOrders")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Успешное получение списка заказов")
        void getAllOrders_Success() {
            // Arrange
            Order order1 = testOrder;
            Order order2 = createTestOrder();
            order2.setOrderId(UUID.randomUUID());
            order2.setAmount(new BigDecimal("150.00"));
            order2.setOrderStatus(OrderStatus.DELIVERY);

            List<Order> orders = Arrays.asList(order1, order2);
            when(orderRepository.findAll()).thenReturn(orders);

            // Act
            List<Order> result = orderService.getAllOrders();

            // Assert
            verify(orderRepository, times(1)).findAll();
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(order1, order2);
        }

        @Test
        @DisplayName("Успешное получение пустого списка заказов")
        void getAllOrders_EmptyList() {
            // Arrange
            when(orderRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<Order> result = orderService.getAllOrders();

            // Assert
            verify(orderRepository, times(1)).findAll();
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }
}