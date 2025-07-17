-- Chèn dữ liệu mẫu cho bảng categories
INSERT INTO categories (id, name, description, icon_url, is_active, created_at, updated_at)
VALUES 
  ('11111111-1111-1111-1111-111111111111', 'Âm nhạc', 'Các sự kiện âm nhạc, hòa nhạc, festival', 'music_icon.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('22222222-2222-2222-2222-222222222222', 'Thể thao', 'Các sự kiện thể thao, giải đấu', 'sport_icon.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('33333333-3333-3333-3333-333333333333', 'Giáo dục', 'Hội thảo, workshop, đào tạo', 'education_icon.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('44444444-4444-4444-4444-444444444444', 'Công nghệ', 'Sự kiện công nghệ, ra mắt sản phẩm', 'tech_icon.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('55555555-5555-5555-5555-555555555555', 'Văn hóa', 'Sự kiện văn hóa, triển lãm', 'culture_icon.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu mẫu cho bảng locations
INSERT INTO locations (id, name, address, city, state, country, postal_code, latitude, longitude, capacity, description, website, phone_number, created_at, updated_at)
VALUES 
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Nhà hát Lớn Hà Nội', '1 Tràng Tiền, Hoàn Kiếm', 'Hà Nội', NULL, 'Việt Nam', '100000', 21.0245, 105.8576, 600, 'Nhà hát lịch sử tại trung tâm Hà Nội', 'https://nhahatlon.vn', '+84 24 3933 0113', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Sân vận động Mỹ Đình', 'Đường Lê Đức Thọ, Mỹ Đình', 'Hà Nội', NULL, 'Việt Nam', '100000', 21.0305, 105.7638, 40000, 'Sân vận động quốc gia', 'https://mydinh.vn', '+84 24 3768 5512', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Trung tâm Hội nghị Quốc gia', 'Phạm Hùng, Cầu Giấy', 'Hà Nội', NULL, 'Việt Nam', '100000', 21.0048, 105.7877, 1200, 'Trung tâm hội nghị hiện đại', 'https://ncc.gov.vn', '+84 24 3792 7777', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Nhà Văn hóa Thanh niên', '4 Phạm Ngọc Thạch, Bến Nghé', 'Hồ Chí Minh', NULL, 'Việt Nam', '700000', 10.7756, 106.6960, 800, 'Trung tâm văn hóa thanh niên', 'https://nvhtn.org.vn', '+84 28 3822 8429', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Cung Văn hóa Hữu nghị Việt Xô', '91 Trần Hưng Đạo, Hoàn Kiếm', 'Hà Nội', NULL, 'Việt Nam', '100000', 21.0231, 105.8491, 1000, 'Cung văn hóa lịch sử', 'https://cungvanhoa.com.vn', '+84 24 3942 6118', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu mẫu cho bảng users
INSERT INTO users (id, email, password, full_name, phone_number, role, enabled, profile_picture_url, notification_preferences, created_at, updated_at)
VALUES 
  ('11111111-aaaa-bbbb-cccc-dddddddddddd', 'admin@example.com', '$2a$10$GQT/4UkSP6aQYw8qQJ9oCeQE6LFVuX8JI9RrM1ynlzJiGnWM5KxX.', 'Admin User', '0987654321', 'ADMIN', true, 'admin.jpg', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('22222222-aaaa-bbbb-cccc-dddddddddddd', 'organizer@example.com', '$2a$10$GQT/4UkSP6aQYw8qQJ9oCeQE6LFVuX8JI9RrM1ynlzJiGnWM5KxX.', 'Organizer User', '0987654322', 'ORGANIZER', true, 'organizer.jpg', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('33333333-aaaa-bbbb-cccc-dddddddddddd', 'user1@example.com', '$2a$10$GQT/4UkSP6aQYw8qQJ9oCeQE6LFVuX8JI9RrM1ynlzJiGnWM5KxX.', 'Normal User 1', '0987654323', 'USER', true, 'user1.jpg', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('44444444-aaaa-bbbb-cccc-dddddddddddd', 'user2@example.com', '$2a$10$GQT/4UkSP6aQYw8qQJ9oCeQE6LFVuX8JI9RrM1ynlzJiGnWM5KxX.', 'Normal User 2', '0987654324', 'USER', true, 'user2.jpg', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('55555555-aaaa-bbbb-cccc-dddddddddddd', 'user3@example.com', '$2a$10$GQT/4UkSP6aQYw8qQJ9oCeQE6LFVuX8JI9RrM1ynlzJiGnWM5KxX.', 'Normal User 3', '0987654325', 'USER', true, 'user3.jpg', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu mẫu cho bảng events
INSERT INTO events (id, organizer_id, category_id, location_id, title, description, short_description, address, city, latitude, longitude, max_attendees, current_attendees, featured_image_url, start_date, end_date, is_private, is_featured, is_free, status, cancellation_reason, average_rating, rating_count, created_at, updated_at)
VALUES 
  ('aaaaaaaa-1111-2222-3333-444444444444', '22222222-aaaa-bbbb-cccc-dddddddddddd', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Đêm nhạc Trịnh Công Sơn', 'Đêm nhạc tưởng nhớ nhạc sĩ Trịnh Công Sơn với sự tham gia của nhiều ca sĩ nổi tiếng', 'Đêm nhạc tưởng nhớ Trịnh Công Sơn', '1 Tràng Tiền, Hoàn Kiếm', 'Hà Nội', 21.0245, 105.8576, 500, 0, 'trinh_cong_son.jpg', '2025-08-15 19:00:00', '2025-08-15 22:00:00', false, true, false, 'PUBLISHED', NULL, 0.0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  ('bbbbbbbb-1111-2222-3333-444444444444', '22222222-aaaa-bbbb-cccc-dddddddddddd', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Giải bóng đá giao hữu', 'Giải bóng đá giao hữu giữa các đội tuyển doanh nghiệp', 'Giải bóng đá giao hữu doanh nghiệp', 'Đường Lê Đức Thọ, Mỹ Đình', 'Hà Nội', 21.0305, 105.7638, 1000, 0, 'football.jpg', '2025-09-20 08:00:00', '2025-09-20 17:00:00', false, false, true, 'PUBLISHED', NULL, 0.0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  ('cccccccc-1111-2222-3333-444444444444', '22222222-aaaa-bbbb-cccc-dddddddddddd', '33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Hội thảo Công nghệ AI', 'Hội thảo về ứng dụng trí tuệ nhân tạo trong cuộc sống và công việc', 'Hội thảo về AI và ứng dụng', 'Phạm Hùng, Cầu Giấy', 'Hà Nội', 21.0048, 105.7877, 800, 0, 'ai_conference.jpg', '2025-10-10 09:00:00', '2025-10-10 17:00:00', false, true, false, 'PUBLISHED', NULL, 0.0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  ('dddddddd-1111-2222-3333-444444444444', '22222222-aaaa-bbbb-cccc-dddddddddddd', '55555555-5555-5555-5555-555555555555', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'Triển lãm Nghệ thuật Đương đại', 'Triển lãm nghệ thuật đương đại với sự tham gia của nhiều nghệ sĩ trẻ', 'Triển lãm nghệ thuật đương đại', '4 Phạm Ngọc Thạch, Bến Nghé', 'Hồ Chí Minh', 10.7756, 106.6960, 300, 0, 'art_exhibition.jpg', '2025-11-05 10:00:00', '2025-11-15 18:00:00', false, false, false, 'PUBLISHED', NULL, 0.0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  ('eeeeeeee-1111-2222-3333-444444444444', '22222222-aaaa-bbbb-cccc-dddddddddddd', '44444444-4444-4444-4444-444444444444', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Tech Summit 2025', 'Hội nghị công nghệ lớn nhất năm với sự tham gia của nhiều chuyên gia hàng đầu', 'Hội nghị công nghệ lớn nhất năm', '91 Trần Hưng Đạo, Hoàn Kiếm', 'Hà Nội', 21.0231, 105.8491, 700, 0, 'tech_summit.jpg', '2025-12-01 08:30:00', '2025-12-02 17:30:00', false, true, false, 'DRAFT', NULL, 0.0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu mẫu cho bảng event_images
INSERT INTO event_images (id, event_id, url, is_primary, created_at)
VALUES 
  ('11111111-eeee-eeee-eeee-111111111111', 'aaaaaaaa-1111-2222-3333-444444444444', 'trinh_cong_son_1.jpg', true, CURRENT_TIMESTAMP),
  ('22222222-eeee-eeee-eeee-222222222222', 'aaaaaaaa-1111-2222-3333-444444444444', 'trinh_cong_son_2.jpg', false, CURRENT_TIMESTAMP),
  ('33333333-eeee-eeee-eeee-333333333333', 'bbbbbbbb-1111-2222-3333-444444444444', 'football_1.jpg', true, CURRENT_TIMESTAMP),
  ('44444444-eeee-eeee-eeee-444444444444', 'cccccccc-1111-2222-3333-444444444444', 'ai_conference_1.jpg', true, CURRENT_TIMESTAMP),
  ('55555555-eeee-eeee-eeee-555555555555', 'dddddddd-1111-2222-3333-444444444444', 'art_exhibition_1.jpg', true, CURRENT_TIMESTAMP),
  ('66666666-eeee-eeee-eeee-666666666666', 'eeeeeeee-1111-2222-3333-444444444444', 'tech_summit_1.jpg', true, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu mẫu cho bảng ticket_types
INSERT INTO ticket_types (id, event_id, name, description, price, quantity, available_quantity, sales_start_date, sales_end_date, max_tickets_per_customer, min_tickets_per_order, is_early_bird, is_vip, is_active, created_at, updated_at)
VALUES 
  ('a1111111-1111-1111-1111-111111111111', 'aaaaaaaa-1111-2222-3333-444444444444', 'Vé Thường', 'Vé tham dự bình thường', 200000, 300, 300, '2025-07-15 00:00:00', '2025-08-14 23:59:59', 5, 1, false, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('a2222222-2222-2222-2222-222222222222', 'aaaaaaaa-1111-2222-3333-444444444444', 'Vé VIP', 'Vé VIP với chỗ ngồi đẹp và đồ uống miễn phí', 500000, 100, 100, '2025-07-15 00:00:00', '2025-08-14 23:59:59', 3, 1, false, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('a3333333-3333-3333-3333-333333333333', 'aaaaaaaa-1111-2222-3333-444444444444', 'Vé Early Bird', 'Vé giá rẻ cho người đặt sớm', 150000, 100, 100, '2025-07-15 00:00:00', '2025-07-31 23:59:59', 2, 1, true, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  ('a4444444-4444-4444-4444-444444444444', 'bbbbbbbb-1111-2222-3333-444444444444', 'Vé Miễn Phí', 'Vé tham dự miễn phí', 0, 1000, 1000, '2025-08-20 00:00:00', '2025-09-19 23:59:59', 2, 1, false, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  ('a5555555-5555-5555-5555-555555555555', 'cccccccc-1111-2222-3333-444444444444', 'Vé Thường', 'Vé tham dự bình thường', 300000, 600, 600, '2025-09-10 00:00:00', '2025-10-09 23:59:59', 3, 1, false, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('a6666666-6666-6666-6666-666666666666', 'cccccccc-1111-2222-3333-444444444444', 'Vé VIP', 'Vé VIP với tài liệu và bữa trưa', 700000, 200, 200, '2025-09-10 00:00:00', '2025-10-09 23:59:59', 2, 1, false, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  ('a7777777-7777-7777-7777-777777777777', 'dddddddd-1111-2222-3333-444444444444', 'Vé Thường', 'Vé tham quan triển lãm', 100000, 300, 300, '2025-10-05 00:00:00', '2025-11-04 23:59:59', 5, 1, false, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  ('a8888888-8888-8888-8888-888888888888', 'eeeeeeee-1111-2222-3333-444444444444', 'Vé Thường', 'Vé tham dự 2 ngày', 500000, 500, 500, '2025-11-01 00:00:00', '2025-11-30 23:59:59', 2, 1, false, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('a9999999-9999-9999-9999-999999999999', 'eeeeeeee-1111-2222-3333-444444444444', 'Vé VIP', 'Vé VIP với quyền tham gia các workshop', 1200000, 200, 200, '2025-11-01 00:00:00', '2025-11-30 23:59:59', 1, 1, false, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu mẫu cho bảng tickets
INSERT INTO tickets (id, ticket_number, user_id, event_id, ticket_type_id, price, status, qr_code, purchase_date, checked_in_at, cancelled_at, payment_id, created_at, updated_at)
VALUES
  -- Vé đã mua cho sự kiện Đêm nhạc Trịnh Công Sơn (3 vé)
  ('b1111111-1111-1111-1111-111111111111', 'TCS-VIP-001', '33333333-aaaa-bbbb-cccc-dddddddddddd', 'aaaaaaaa-1111-2222-3333-444444444444', 'a2222222-2222-2222-2222-222222222222', 500000, 'PAID', 'qr_tcs_vip_001', '2025-07-20 10:15:00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('b2222222-2222-2222-2222-222222222222', 'TCS-EB-001', '33333333-aaaa-bbbb-cccc-dddddddddddd', 'aaaaaaaa-1111-2222-3333-444444444444', 'a3333333-3333-3333-3333-333333333333', 150000, 'PAID', 'qr_tcs_eb_001', '2025-07-18 14:30:00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('b3333333-3333-3333-3333-333333333333', 'TCS-STD-001', '44444444-aaaa-bbbb-cccc-dddddddddddd', 'aaaaaaaa-1111-2222-3333-444444444444', 'a1111111-1111-1111-1111-111111111111', 200000, 'PAID', 'qr_tcs_std_001', '2025-07-25 09:45:00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Vé đã mua cho sự kiện Giải bóng đá giao hữu (2 vé)
  ('b4444444-4444-4444-4444-444444444444', 'FB-FREE-001', '33333333-aaaa-bbbb-cccc-dddddddddddd', 'bbbbbbbb-1111-2222-3333-444444444444', 'a4444444-4444-4444-4444-444444444444', 0, 'PAID', 'qr_fb_free_001', '2025-08-25 11:20:00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('b5555555-5555-5555-5555-555555555555', 'FB-FREE-002', '44444444-aaaa-bbbb-cccc-dddddddddddd', 'bbbbbbbb-1111-2222-3333-444444444444', 'a4444444-4444-4444-4444-444444444444', 0, 'PAID', 'qr_fb_free_002', '2025-08-26 15:10:00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Vé đã mua cho sự kiện Hội thảo Công nghệ AI (3 vé)
  ('b6666666-6666-6666-6666-666666666666', 'AI-VIP-001', '44444444-aaaa-bbbb-cccc-dddddddddddd', 'cccccccc-1111-2222-3333-444444444444', 'a6666666-6666-6666-6666-666666666666', 700000, 'PAID', 'qr_ai_vip_001', '2025-09-15 08:30:00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('b7777777-7777-7777-7777-777777777777', 'AI-STD-001', '33333333-aaaa-bbbb-cccc-dddddddddddd', 'cccccccc-1111-2222-3333-444444444444', 'a5555555-5555-5555-5555-555555555555', 300000, 'PAID', 'qr_ai_std_001', '2025-09-20 10:45:00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('b8888888-8888-8888-8888-888888888888', 'AI-STD-002', '55555555-aaaa-bbbb-cccc-dddddddddddd', 'cccccccc-1111-2222-3333-444444444444', 'a5555555-5555-5555-5555-555555555555', 300000, 'PAID', 'qr_ai_std_002', '2025-09-22 14:15:00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Vé đã mua cho sự kiện Triển lãm Nghệ thuật Đương đại (2 vé)
  ('b9999999-9999-9999-9999-999999999999', 'ART-STD-001', '44444444-aaaa-bbbb-cccc-dddddddddddd', 'dddddddd-1111-2222-3333-444444444444', 'a7777777-7777-7777-7777-777777777777', 100000, 'PAID', 'qr_art_std_001', '2025-10-10 09:30:00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('c1111111-1111-1111-1111-111111111111', 'ART-STD-002', '55555555-aaaa-bbbb-cccc-dddddddddddd', 'dddddddd-1111-2222-3333-444444444444', 'a7777777-7777-7777-7777-777777777777', 100000, 'PAID', 'qr_art_std_002', '2025-10-12 11:20:00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Vé đã đặt chỗ (RESERVED) cho sự kiện Tech Summit 2025 (2 vé)
  ('c2222222-2222-2222-2222-222222222222', 'TECH-VIP-001', '33333333-aaaa-bbbb-cccc-dddddddddddd', 'eeeeeeee-1111-2222-3333-444444444444', 'a9999999-9999-9999-9999-999999999999', 1200000, 'RESERVED', NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('c3333333-3333-3333-3333-333333333333', 'TECH-STD-001', '55555555-aaaa-bbbb-cccc-dddddddddddd', 'eeeeeeee-1111-2222-3333-444444444444', 'a8888888-8888-8888-8888-888888888888', 500000, 'RESERVED', NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Vé đã hủy (CANCELLED) (1 vé)
  ('c4444444-4444-4444-4444-444444444444', 'TCS-STD-002', '55555555-aaaa-bbbb-cccc-dddddddddddd', 'aaaaaaaa-1111-2222-3333-444444444444', 'a1111111-1111-1111-1111-111111111111', 200000, 'CANCELLED', NULL, '2025-07-26 10:30:00', NULL, '2025-07-28 15:45:00', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  
  -- Vé đã check-in (CHECKED_IN) (1 vé)
  ('c5555555-5555-5555-5555-555555555555', 'AI-STD-003', '44444444-aaaa-bbbb-cccc-dddddddddddd', 'cccccccc-1111-2222-3333-444444444444', 'a5555555-5555-5555-5555-555555555555', 300000, 'CHECKED_IN', 'qr_ai_std_003', '2025-09-25 08:20:00', '2025-10-10 09:15:00', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
