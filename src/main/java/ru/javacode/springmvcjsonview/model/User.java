package ru.javacode.springmvcjsonview.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.javacode.springmvcjsonview.view.Views;

import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class User {

    @Id
    @Column(name = "user_id", nullable = false)
    @JsonView(Views.UserSummary.class)
    private UUID userId;

    @NotBlank(message = "не указано имя")
    @Column(name = "user_name", nullable = false)
    @JsonView(Views.UserSummary.class)
    private String username;

    @NotNull(message = "не указан Email")
    @Email(message = "неправильный формат Email")
    @Column(name = "user_email", nullable = false)
    @JsonView(Views.UserSummary.class)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonView(Views.UserDetails.class)
    List<Order> orders;


    @PrePersist
    public void generateUUID() {
        if (userId == null) {
            userId = UUID.randomUUID();
        }
    }
}
