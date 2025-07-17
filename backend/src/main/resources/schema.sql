
-- Bảng ticket_types
CREATE TABLE IF NOT EXISTS ticket_types (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    available_quantity INTEGER NOT NULL,
    event_id UUID NOT NULL,
    sales_start_date TIMESTAMP,
    sales_end_date TIMESTAMP,
    max_tickets_per_customer INTEGER,
    min_tickets_per_order INTEGER NOT NULL DEFAULT 1,
    is_early_bird BOOLEAN NOT NULL DEFAULT FALSE,
    is_vip BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_type_event FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Bảng tickets
CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY,
    ticket_number VARCHAR(255) UNIQUE,
    user_id UUID NOT NULL,
    event_id UUID NOT NULL,
    ticket_type_id UUID NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    qr_code VARCHAR(255),
    purchase_date TIMESTAMP,
    checked_in_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    payment_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ticket_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_ticket_type FOREIGN KEY (ticket_type_id) REFERENCES ticket_types(id)
);

-- Bảng password_reset_tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reset_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Bảng token_blacklist
CREATE TABLE IF NOT EXISTS token_blacklist (
    id UUID PRIMARY KEY,
    token VARCHAR(1000) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Bảng device_tokens
CREATE TABLE IF NOT EXISTS device_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_device_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Bảng notifications
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    reference_id UUID,
    reference_type VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Bảng comments
CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    parent_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comments(id)
);

-- Bảng ratings
CREATE TABLE IF NOT EXISTS ratings (
    id UUID PRIMARY KEY,
    score INTEGER NOT NULL,
    review TEXT,
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    is_reported BOOLEAN NOT NULL DEFAULT FALSE,
    report_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rating_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_user_event_rating UNIQUE (user_id, event_id)
);

-- Bảng reports
CREATE TABLE IF NOT EXISTS reports (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    date_generated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    parameters TEXT,
    result_data TEXT,
    file_path VARCHAR(255),
    user_id UUID NOT NULL,
    event_id UUID,
    CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_report_event FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Tạo các index cho hiệu suất truy vấn
CREATE INDEX IF NOT EXISTS idx_ticket_types_event_id ON ticket_types(event_id);
CREATE INDEX IF NOT EXISTS idx_ticket_types_price ON ticket_types(price);
CREATE INDEX IF NOT EXISTS idx_ticket_types_active ON ticket_types(is_active);

CREATE INDEX IF NOT EXISTS idx_tickets_user_id ON tickets(user_id);
CREATE INDEX IF NOT EXISTS idx_tickets_event_id ON tickets(event_id);
CREATE INDEX IF NOT EXISTS idx_tickets_ticket_type_id ON tickets(ticket_type_id);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_tickets_purchase_date ON tickets(purchase_date);

CREATE INDEX IF NOT EXISTS idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_user_id ON password_reset_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_blacklist_token ON token_blacklist(token);
CREATE INDEX IF NOT EXISTS idx_blacklist_username ON token_blacklist(username);
CREATE INDEX IF NOT EXISTS idx_blacklist_expiry ON token_blacklist(expiry_date);

CREATE INDEX IF NOT EXISTS idx_device_tokens_user_id ON device_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_device_tokens_token ON device_tokens(token);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_reference_id ON notifications(reference_id);

CREATE INDEX IF NOT EXISTS idx_comment_event ON comments(event_id);
CREATE INDEX IF NOT EXISTS idx_comment_user ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comment_parent ON comments(parent_id);
CREATE INDEX IF NOT EXISTS idx_comment_status ON comments(status);

CREATE INDEX IF NOT EXISTS idx_rating_event ON ratings(event_id);
CREATE INDEX IF NOT EXISTS idx_rating_user ON ratings(user_id);
CREATE INDEX IF NOT EXISTS idx_rating_status ON ratings(status);
CREATE INDEX IF NOT EXISTS idx_rating_reported ON ratings(is_reported);

CREATE INDEX IF NOT EXISTS idx_report_type ON reports(type);
CREATE INDEX IF NOT EXISTS idx_report_date ON reports(date_generated);
CREATE INDEX IF NOT EXISTS idx_report_user ON reports(user_id);
CREATE INDEX IF NOT EXISTS idx_report_event ON reports(event_id); 