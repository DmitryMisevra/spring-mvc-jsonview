package ru.javacode.springmvcjsonview.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.javacode.springmvcjsonview.view.Views;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Order {

    @Id
    @Column(name = "order_id", nullable = false)
    @JsonView({Views.OrderSummary.class, Views.UserDetails.class})
    private UUID orderId;

    @Column(name = "order_amount", nullable = false)
    @JsonView({Views.OrderSummary.class, Views.UserDetails.class})
    @NotNull(message = "не указана сумма заказа")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    @JsonView({Views.OrderSummary.class, Views.UserDetails.class})
    @NotNull(message = "не указан статус заказа")
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonView(Views.OrderDetails.class)
    @NotNull(message = "не указан пользователь")
    private User user;


    @PrePersist
    public void generateUUID() {
        if (orderId == null) {
            orderId = UUID.randomUUID();
        }
    }
}
