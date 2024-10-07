package ru.javacode.springmvcjsonview.service;

import ru.javacode.springmvcjsonview.model.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    Order createOrder(Order order);

    Order updateOrder(Long orderId, Order order);

    Order getOrderById(Long orderId);

    void deleteOrder(Long orderId);

    List<Order> getAllOrders();
}
