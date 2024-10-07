package ru.javacode.springmvcjsonview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.javacode.springmvcjsonview.exception.ResourceNotFoundException;
import ru.javacode.springmvcjsonview.model.Order;
import ru.javacode.springmvcjsonview.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrder(Long orderId, Order order) {
        Order orderToUpdate = getOrderById(orderId);
        orderToUpdate.setAmount(order.getAmount());
        orderToUpdate.setOrderStatus(order.getOrderStatus());
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException(
                "Order with id " + orderId + " not found"));
    }

    @Override
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
