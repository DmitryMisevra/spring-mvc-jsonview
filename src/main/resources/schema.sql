CREATE TABLE IF NOT EXISTS orders
(
    order_id     UUID         NOT NULL,
    order_amount DECIMAL      NOT NULL,
    order_status VARCHAR(255) NOT NULL,
    user_id      UUID,
    CONSTRAINT pk_orders PRIMARY KEY (order_id)

);

CREATE TABLE IF NOT EXISTS users
(
    user_id    UUID         NOT NULL,
    user_name  VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id)
);