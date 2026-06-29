CREATE TABLE IF NOT EXISTS payment_orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    plan_key VARCHAR(20) NOT NULL,
    amount INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    transfer_ref VARCHAR(100),
    admin_note VARCHAR(255),
    verified_by VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    submitted_at DATETIME NULL,
    verified_at DATETIME NULL,
    expired_at DATETIME NULL,
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_payment_orders_user
        FOREIGN KEY (user_id) REFERENCES user(user_id),
    INDEX idx_payment_orders_status (status),
    INDEX idx_payment_orders_user_status (user_id, status),
    INDEX idx_payment_orders_expired (status, expired_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
