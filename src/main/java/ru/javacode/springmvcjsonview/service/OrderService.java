package ru.javacode.springmvcjsonview.service;

import ru.javacode.springmvcjsonview.model.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    Order createOrder(Order order);

    Order updateOrder(UUID orderId, Order order);

    Order getOrderById(UUID orderId);

    void deleteOrder(UUID orderId);

    List<Order> getAllOrders();
}
