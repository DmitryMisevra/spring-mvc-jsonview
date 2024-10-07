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
import ru.javacode.springmvcjsonview.model.OrderStatus;
import ru.javacode.springmvcjsonview.model.User;
import ru.javacode.springmvcjsonview.service.OrderService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    // Вспомогательный метод для создания тестового заказа
    private Order createTestOrder() {
        User user = User.builder()
                .userId(1L)
                .name("testuser")
                .email("testuser@example.com")
                .orders(Collections.emptyList())
                .build();
        return Order.builder()
                .orderId(1L)
                .amount(new BigDecimal("99.99"))
                .orderStatus(OrderStatus.PROCESSING)
                .user(user) // В UserSummary user не отображается
                .build();
    }

    // Вспомогательный метод для создания детального заказа с пользователем

    @Nested
    @DisplayName("Тесты для POST /api/v1/orders")
    class CreateOrderTests {

        @Test
        @DisplayName("Успешное создание заказа")
        void createOrder_Success() throws Exception {
            Order order = createTestOrder();
            Order orderToSave = Order.builder()
                    .amount(order.getAmount())
                    .orderStatus(order.getOrderStatus())
                    .user(order.getUser())
                    .build();
            when(orderService.createOrder(any(Order.class))).thenReturn(order);

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderToSave)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.orderId", is(order.getOrderId().toString())))
                    .andExpect(jsonPath("$.amount", is(order.getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderStatus", is(order.getOrderStatus().toString())))
                    .andExpect(jsonPath("$.user").doesNotExist()); // В OrderSummary пользователь не отображается
        }

        @Test
        @DisplayName("Неудачное создание заказа из-за валидации")
        void createOrder_ValidationFailure() throws Exception {
            Order invalidOrder = Order.builder()
                    .amount(null) // Отсутствует сумма
                    .orderStatus(null) // Отсутствует статус
                    .user(null) // Отсутствует пользователь
                    .build();

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidOrder)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Тесты для PUT /api/v1/orders/{orderId}")
    class UpdateOrderTests {

        @Test
        @DisplayName("Успешное обновление заказа")
        void updateOrder_Success() throws Exception {
            Long orderId = 1L;
            Order existingOrder = createTestOrder();
            Order updatedOrder = Order.builder()
                    .orderId(orderId)
                    .amount(new BigDecimal("199.99"))
                    .orderStatus(OrderStatus.DELIVERY)
                    .user(existingOrder.getUser())
                    .build();

            when(orderService.updateOrder(ArgumentMatchers.eq(orderId), any(Order.class))).thenReturn(updatedOrder);

            mockMvc.perform(put("/api/v1/orders/{orderId}", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedOrder)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.orderId", is(updatedOrder.getOrderId().toString())))
                    .andExpect(jsonPath("$.amount", is(updatedOrder.getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderStatus", is(updatedOrder.getOrderStatus().toString()))); // В OrderSummary пользователь не отображается
        }

        @Test
        @DisplayName("Неудачное обновление заказа - заказ не найден")
        void updateOrder_NotFound() throws Exception {
            Long orderId = 100L;
            Order updatedOrder = Order.builder()
                    .amount(new BigDecimal("199.99"))
                    .orderStatus(OrderStatus.DELIVERY)
                    .user(null)
                    .build();

            when(orderService.updateOrder(ArgumentMatchers.eq(orderId), any(Order.class)))
                    .thenThrow(new ResourceNotFoundException("Order with id " + orderId + " not found"));

            mockMvc.perform(put("/api/v1/orders/{orderId}", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedOrder)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Неудачное обновление заказа из-за валидации")
        void updateOrder_ValidationFailure() throws Exception {
            UUID orderId = UUID.randomUUID();
            Order invalidOrder = Order.builder()
                    .amount(null) // Отсутствует сумма
                    .orderStatus(null) // Отсутствует статус
                    .user(null) // Отсутствует пользователь
                    .build();

            mockMvc.perform(put("/api/v1/orders/{orderId}", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidOrder)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Тесты для GET /api/v1/orders/{orderId}")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Успешное получение заказа по ID")
        void getOrderById_Success() throws Exception {
            Long orderId = 1L;
            Order order = createTestOrder();

            when(orderService.getOrderById(orderId)).thenReturn(order);

            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.orderId", is(order.getOrderId().toString())))
                    .andExpect(jsonPath("$.amount", is(order.getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderStatus", is(order.getOrderStatus().toString())))
                    .andExpect(jsonPath("$.user").exists());
        }

        @Test
        @DisplayName("Неудачное получение заказа - не найдено")
        void getOrderById_NotFound() throws Exception {
            Long orderId = 1L;

            when(orderService.getOrderById(orderId))
                    .thenThrow(new ResourceNotFoundException("Order with id " + orderId + " not found"));

            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("Order with id " + orderId + " not found")));
        }
    }

    @Nested
    @DisplayName("Тесты для DELETE /api/v1/orders/{orderId}")
    class DeleteOrderTests {

        @Test
        @DisplayName("Успешное удаление заказа")
        void deleteOrder_Success() throws Exception {
            Long orderId = 1L;
            doNothing().when(orderService).deleteOrder(orderId);

            mockMvc.perform(delete("/api/v1/orders/{orderId}", orderId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Неудачное удаление заказа - не найдено")
        void deleteOrder_NotFound() throws Exception {
            Long orderId = 1L;

            // Предполагаем, что при попытке удалить несуществующий заказ, сервис бросает ResourceNotFoundException
            org.mockito.Mockito.doThrow(new ResourceNotFoundException("Order with id " + orderId + " not found"))
                    .when(orderService).deleteOrder(orderId);

            mockMvc.perform(delete("/api/v1/orders/{orderId}", orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("Order with id " + orderId + " not found")));
        }
    }

    @Nested
    @DisplayName("Тесты для GET /api/v1/orders")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Успешное получение списка заказов")
        void getAllOrders_Success() throws Exception {
            Order order1 = createTestOrder();
            Order order2 = createTestOrder();
            List<Order> orders = Arrays.asList(order1, order2);

            when(orderService.getAllOrders()).thenReturn(orders);

            mockMvc.perform(get("/api/v1/orders"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].orderId", is(order1.getOrderId().toString())))
                    .andExpect(jsonPath("$[0].amount", is(order1.getAmount().doubleValue())))
                    .andExpect(jsonPath("$[0].orderStatus", is(order1.getOrderStatus().toString())))
                    .andExpect(jsonPath("$[0].user").doesNotExist()) // В OrderSummary пользователь не отображается
                    .andExpect(jsonPath("$[1].orderId", is(order2.getOrderId().toString())))
                    .andExpect(jsonPath("$[1].amount", is(order2.getAmount().doubleValue())))
                    .andExpect(jsonPath("$[1].orderStatus", is(order2.getOrderStatus().toString())))
                    .andExpect(jsonPath("$[1].user").doesNotExist());
        }

        @Test
        @DisplayName("Успешное получение пустого списка заказов")
        void getAllOrders_EmptyList() throws Exception {
            when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/orders"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}