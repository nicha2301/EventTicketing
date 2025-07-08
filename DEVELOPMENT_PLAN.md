# KẾ HOẠCH PHÁT TRIỂN DỰ ÁN EVENT TICKETING

## TỔNG QUAN DỰ ÁN

### Mục tiêu chính
- Xây dựng ứng dụng Android cho phép người dùng khám phá, đăng ký, mua vé và check-in sự kiện
- Cung cấp hệ thống quản lý cho ban tổ chức và quản trị viên
- Hỗ trợ thanh toán trực tuyến qua VNPay/Stripe
- Áp dụng tính năng AI để nâng cao trải nghiệm người dùng

### Các vai trò trong hệ thống
- **Người dùng**: Tìm kiếm sự kiện, mua vé, nhận vé QR, check-in, chia sẻ mạng xã hội, đánh giá
- **Ban tổ chức**: Tạo và quản lý sự kiện, quét QR để xác nhận vé, theo dõi lượt đăng ký
- **Quản trị viên**: Kiểm duyệt nội dung, theo dõi hệ thống, báo cáo, doanh thu
- **Hệ thống/AI**: Gợi ý sự kiện, gửi thông báo đẩy, phân tích cảm xúc người dùng

### Timeline tổng thể
- **Q1 (3 tháng đầu)**: Hoàn thiện backend core và frontend cơ bản
- **Q2 (3 tháng tiếp)**: Triển khai các tính năng nâng cao và social features
- **Q3 (3 tháng tiếp)**: Tối ưu hóa, analytics và bắt đầu tích hợp AI
- **Q4 (3 tháng cuối)**: Mở rộng các tính năng AI và chuẩn bị ra mắt

## BACKEND (Spring Boot + Kotlin)

### Phase 1: Thiết lập Dự án & Cấu trúc Cơ bản (2 tuần)
- [x] Tạo dự án Spring Boot với Kotlin
- [x] Cấu hình Gradle và dependencies cơ bản
- [x] Thiết lập cấu trúc thư mục theo module (controller, service, repository, entity, dto)
- [x] Cấu hình application.yml với các profile (dev, test, prod)
- [x] Thiết lập Liquibase cho database migration
- [x] Tạo schema cơ sở dữ liệu ban đầu (001-initial-schema.xml)
- [x] Tạo các entity (User, Event, TicketType, Ticket, Payment)
- [x] Tạo các repository interfaces với custom queries
- [x] Tạo các DTO objects và mappers
- [x] Thiết lập xử lý ngoại lệ toàn cục (GlobalExceptionHandler)
- [x] Cấu hình Security (JWT + OAuth2 Resource Server)
- [x] Cấu hình OpenAPI/Swagger với JWT authentication

### Phase 2: Phát triển Services & Controllers (4 tuần)
- [ ] User Module
  - [ ] UserService implementation
    - [ ] Đăng ký người dùng mới với email verification
    - [ ] Đăng nhập/Xác thực bằng JWT
    - [ ] CRUD operations cho User
    - [ ] Phân quyền theo vai trò (USER, ORGANIZER, ADMIN)
    - [ ] Cơ chế đổi mật khẩu và reset mật khẩu
    - [ ] Quản lý thông tin hồ sơ cá nhân
  - [ ] UserController với endpoints
    - [ ] POST /api/auth/register
    - [ ] POST /api/auth/login
    - [ ] GET /api/users/me
    - [ ] PUT /api/users/me
    - [ ] POST /api/auth/password/change
    - [ ] POST /api/auth/password/reset

- [ ] Event Module
  - [ ] EventService implementation
    - [ ] Tạo sự kiện mới với validation
    - [ ] Cập nhật thông tin sự kiện
    - [ ] Tìm kiếm sự kiện (theo vị trí, thể loại, thời gian, từ khóa)
    - [ ] Geolocational search (tìm theo khoảng cách)
    - [ ] Quản lý trạng thái sự kiện (draft, published, cancelled)
    - [ ] Xử lý upload ảnh cho sự kiện
  - [ ] EventController với endpoints
    - [ ] POST /api/events
    - [ ] PUT /api/events/{id}
    - [ ] GET /api/events
    - [ ] GET /api/events/{id}
    - [ ] GET /api/events/search
    - [ ] DELETE /api/events/{id}
    - [ ] POST /api/events/{id}/publish
    - [ ] POST /api/events/{id}/cancel

- [ ] TicketType Module
  - [ ] TicketTypeService implementation
    - [ ] Tạo và quản lý loại vé cho sự kiện
    - [ ] Cập nhật thông tin và số lượng vé
    - [ ] Kiểm tra tình trạng bán vé
    - [ ] Phân tích doanh thu theo loại vé
  - [ ] TicketTypeController với endpoints
    - [ ] POST /api/events/{eventId}/ticket-types
    - [ ] PUT /api/ticket-types/{id}
    - [ ] GET /api/events/{eventId}/ticket-types
    - [ ] DELETE /api/ticket-types/{id}

- [ ] Ticket Module
  - [ ] TicketService implementation
    - [ ] Xử lý mua vé với kiểm tra số lượng còn lại
    - [ ] Tạo QR code cho vé (sử dụng ZXing)
    - [ ] Xác thực và check-in vé (quét QR)
    - [ ] Quản lý vé đã mua của người dùng
    - [ ] Hủy vé và hoàn tiền (nếu phù hợp)
  - [ ] TicketController với endpoints
    - [ ] POST /api/tickets/purchase
    - [ ] GET /api/users/me/tickets
    - [ ] POST /api/tickets/check-in
    - [ ] GET /api/tickets/{id}/qr-code
    - [ ] POST /api/tickets/{id}/cancel

- [ ] Payment Module
  - [ ] PaymentService implementation
    - [ ] Tích hợp VNPay payment gateway
    - [ ] Tích hợp Stripe payment gateway (backup)
    - [ ] Xử lý webhook từ payment gateway
    - [ ] Quản lý trạng thái thanh toán
    - [ ] Xử lý hoàn tiền
  - [ ] PaymentController với endpoints
    - [ ] POST /api/payments/create
    - [ ] POST /api/payments/vnpay-return
    - [ ] POST /api/payments/vnpay-ipn
    - [ ] POST /api/payments/stripe-webhook
    - [ ] GET /api/users/me/payments
    - [ ] POST /api/payments/{id}/refund

### Phase 3: Tính năng Nâng cao & Tối ưu (6 tuần)
- [ ] Community Module
  - [ ] CommentService và CommentController
    - [ ] Thêm/sửa/xóa bình luận trên sự kiện
    - [ ] Phản hồi bình luận
    - [ ] Quản lý bình luận (phê duyệt/từ chối)
  - [ ] RatingService và RatingController
    - [ ] Đánh giá sự kiện (1-5 sao)
    - [ ] Hiển thị đánh giá trung bình
    - [ ] Báo cáo đánh giá không phù hợp

- [ ] Notification System
  - [ ] Tích hợp SendGrid/Mailgun cho email
    - [ ] Email xác nhận đăng ký với mã kích hoạt
    - [ ] Email xác nhận mua vé với QR code
    - [ ] Email nhắc nhở sự kiện trước 24h
    - [ ] Email marketing (tùy chọn opt-in)
  - [ ] Tích hợp Firebase Cloud Messaging
    - [ ] Thiết lập notification channels
    - [ ] Thông báo đẩy cho người dùng khi có sự kiện phù hợp
    - [ ] Thông báo cho ban tổ chức khi có người đăng ký
    - [ ] Thông báo nhắc nhở sự kiện sắp diễn ra

- [ ] Reporting & Analytics
  - [ ] ReportService và ReportController
    - [ ] Thống kê doanh thu theo ngày/tuần/tháng
    - [ ] Báo cáo số lượng vé đã bán
    - [ ] Báo cáo check-in (có mặt/vắng mặt)
    - [ ] Dashboard tổng hợp cho ban tổ chức
    - [ ] Xuất báo cáo dạng PDF/Excel

- [ ] Hiệu suất & Quy mô
  - [ ] Caching
    - [ ] Cài đặt Redis cho distributed caching
    - [ ] Cache events, tickets data với TTL
    - [ ] Cache response từ các API phổ biến
  - [ ] Tối ưu hiệu năng
    - [ ] Query optimization với indexes
    - [ ] Connection pooling tối ưu
    - [ ] JPA/Hibernate tuning (batch operations)
    - [ ] Pagination cho large result sets
  - [ ] File Storage
    - [ ] Tích hợp Amazon S3/Google Cloud Storage
    - [ ] Xử lý và tối ưu hình ảnh
    - [ ] Tạo signed URLs cho file access

### Phase 4: Testing & Deployment (4 tuần)
- [ ] Testing Strategy
  - [ ] Unit Testing
    - [ ] Service layer tests với Mockito
    - [ ] Repository tests với TestContainers
    - [ ] Controller tests với WebMvcTest
    - [ ] Đạt coverage ít nhất 80%
  - [ ] Integration Testing
    - [ ] API endpoint tests (end-to-end)
    - [ ] Security tests (authentication/authorization)
    - [ ] Database integration tests
    - [ ] Payment gateway integration tests

- [ ] Logging & Monitoring
  - [ ] Cấu hình Logback với rotating file
  - [ ] Tích hợp ELK Stack (Elasticsearch, Logstash, Kibana)
  - [ ] Cấu hình Actuator endpoints
  - [ ] Cài đặt Prometheus metrics
  - [ ] Dashboard giám sát với Grafana

- [ ] Deployment Pipeline
  - [ ] Dockerize Application
    - [ ] Tạo Dockerfile tối ưu (multi-stage build)
    - [ ] docker-compose.yml cho development
    - [ ] Kubernetes manifests cho production
  - [ ] CI/CD với GitHub Actions
    - [ ] Automated testing mỗi khi push code
    - [ ] Automatic build Docker images
    - [ ] Automatic deployment to staging/production
    - [ ] Semantic versioning

## FRONTEND (Kotlin Android)

### Phase 1: Thiết lập Dự án & Cấu trúc Cơ bản (2 tuần)
- [ ] Project Setup
  - [ ] Tạo dự án Android với Kotlin và Gradle 8.x
  - [ ] Thiết lập các dependencies chính (Retrofit, Room, Hilt, Jetpack Compose)
  - [ ] Cấu trúc dự án theo module chức năng
  - [ ] Thiết lập CI/CD với GitHub Actions

- [ ] Architectural Foundation
  - [ ] Áp dụng MVVM + Clean Architecture
  - [ ] Thiết lập base classes (BaseViewModel, BaseRepository)
  - [ ] Cấu hình Dependency Injection với Hilt
  - [ ] Repository Pattern kết hợp Single Source of Truth

- [ ] Core Infrastructure
  - [ ] Network layer với Retrofit và interceptors
  - [ ] Local storage với Room Database
  - [ ] SharedPreferences wrapper
  - [ ] Utilities (extensions, helpers)

- [ ] Design System
  - [ ] Thiết lập Material 3 Theme
  - [ ] Typography & Color systems
  - [ ] Common UI components
  - [ ] Animation systems

### Phase 2: User Authentication & Profile (3 tuần)
- [ ] Authentication Flow
  - [ ] Onboarding screens
  - [ ] Đăng ký với email verification
  - [ ] Đăng nhập với email/password
  - [ ] Social login (Google)
  - [ ] Xử lý JWT token và refresh
  - [ ] Forgot password flow

- [ ] User Profile
  - [ ] Profile screen với thông tin cá nhân
  - [ ] Edit profile (name, avatar, phone)
  - [ ] Change password
  - [ ] Notification preferences
  - [ ] Privacy settings

- [ ] Biometric Authentication
  - [ ] Fingerprint/Face ID integration
  - [ ] Secure credential storage
  - [ ] Biometric prompt cho sensitive actions

### Phase 3: Event Discovery & Details (4 tuần)
- [ ] Home & Discovery
  - [ ] Home screen với carousel sự kiện nổi bật
  - [ ] Category browsing
  - [ ] Tìm kiếm với filters (date, location, price)
  - [ ] Pull-to-refresh và infinite scrolling
  - [ ] Saved/Favorite events

- [ ] Event Details
  - [ ] Event info screen với hình ảnh, mô tả
  - [ ] Google Maps integration để xem địa điểm
  - [ ] Danh sách loại vé và giá
  - [ ] Share event functionality
  - [ ] Add to calendar

- [ ] Location Services
  - [ ] Xin quyền truy cập vị trí
  - [ ] Nearby events dựa trên GPS
  - [ ] Map view của các sự kiện
  - [ ] Directions to event venue

### Phase 4: Ticket Purchase & Management (3 tuần)
- [ ] Ticket Purchase Flow
  - [ ] Ticket selection UI
  - [ ] Checkout process
  - [ ] Payment method selection
  - [ ] VNPay SDK integration
  - [ ] Order confirmation
  - [ ] Purchase receipt

- [ ] Ticket Wallet
  - [ ] My tickets screen
  - [ ] QR code display
  - [ ] Ticket details view
  - [ ] Add to Google Wallet/Apple Wallet
  - [ ] Offline access to tickets

- [ ] Check-in Experience
  - [ ] QR code scanner cho ban tổ chức
  - [ ] Check-in confirmation
  - [ ] Error handling (invalid ticket, already used)

### Phase 5: Organizer Experience (3 tuần)
- [ ] Event Management
  - [ ] Create event form
  - [ ] Edit event details
  - [ ] Manage ticket types
  - [ ] Upload images
  - [ ] Publish/Unpublish controls

- [ ] Attendee Management
  - [ ] View registered attendees
  - [ ] Check-in dashboard
  - [ ] Export attendee list
  - [ ] Send notifications to attendees

- [ ] Sales & Analytics
  - [ ] Sales dashboard
  - [ ] Revenue charts
  - [ ] Attendance statistics
  - [ ] Export reports

### Phase 6: Offline-first & Performance (4 tuần)
- [ ] Offline Capabilities
  - [ ] Room DB để lưu data offline
  - [ ] Synchronization strategy
  - [ ] WorkManager cho background tasks
  - [ ] Conflict resolution
  - [ ] Offline-first UI updates

- [ ] Performance Optimization
  - [ ] Image loading & caching với Coil
  - [ ] Pagination với Paging 3
  - [ ] Lazy loading của UI components
  - [ ] Memory optimization
  - [ ] Battery usage optimization

- [ ] Background Processing
  - [ ] WorkManager cho sync tasks
  - [ ] Background notifications
  - [ ] Geofencing cho location-based alerts
  - [ ] Battery-efficient location updates

### Phase 7: Social Features (3 tuần)
- [ ] Community Engagement
  - [ ] Event comments & discussion
  - [ ] User ratings & reviews
  - [ ] Report inappropriate content
  - [ ] Follow organizers

- [ ] Sharing & Social
  - [ ] Share tickets to friends
  - [ ] Social media integration
  - [ ] Deep linking
  - [ ] Dynamic links for referrals
  - [ ] QR code sharing

### Phase 8: Testing & Release Preparation (4 tuần)
- [ ] Automated Testing
  - [ ] Unit tests với JUnit
  - [ ] UI tests với Espresso
  - [ ] ViewModel tests
  - [ ] End-to-end tests

- [ ] User Testing
  - [ ] Beta program setup
  - [ ] Firebase App Distribution
  - [ ] Feedback collection
  - [ ] Crash reporting & analysis

- [ ] Release Preparation
  - [ ] App Store optimization
  - [ ] Screenshots & store listing
  - [ ] Privacy policy & terms
  - [ ] Release management
  - [ ] CI/CD for production releases

## AI INTEGRATION (Phát triển song song với các phase trên)

### Phase 1: Cơ bản (Q2)
- [ ] Recommendation Engine
  - [ ] Collaborative filtering algorithm
  - [ ] Content-based recommendations
  - [ ] Gợi ý sự kiện dựa trên lịch sử xem
  - [ ] API endpoint cho recommendations
  - [ ] A/B testing framework

- [ ] Classification
  - [ ] Phân loại sự kiện tự động
  - [ ] Tag extraction từ mô tả
  - [ ] Content moderation cho bình luận

### Phase 2: Trung cấp (Q3)
- [ ] Sentiment Analysis
  - [ ] Phân tích cảm xúc bình luận
  - [ ] Highlight bình luận tích cực/tiêu cực
  - [ ] Phát hiện spam và nội dung không phù hợp
  - [ ] Báo cáo tổng hợp sentiment cho organizers

- [ ] Contextual Recommendations
  - [ ] Gợi ý sự kiện theo địa điểm hiện tại
  - [ ] Gợi ý theo thời gian (ngày lễ, cuối tuần)
  - [ ] Personalization dựa trên thói quen người dùng
  - [ ] Cross-selling (events people also liked)

### Phase 3: Nâng cao (Q4)
- [ ] Advanced Personalization
  - [ ] Machine learning pipeline
  - [ ] User behavior analysis
  - [ ] Cá nhân hóa dựa trên nhiều yếu tố
  - [ ] Realtime recommendations

- [ ] Predictive Analytics
  - [ ] Dự đoán xu hướng sự kiện hot
  - [ ] Demand forecasting
  - [ ] Tối ưu hóa giá vé dựa trên nhu cầu
  - [ ] Churn prediction & retention

## CROSS-CUTTING CONCERNS

### Security
- [ ] HTTPS và certificate pinning
- [ ] Secure storage của sensitive data
- [ ] OWASP compliance
- [ ] Regular security audits
- [ ] Data encryption (at rest & in transit)

### Privacy
- [ ] GDPR compliance
- [ ] Data minimization
- [ ] Quyền truy cập và xóa dữ liệu cá nhân
- [ ] Transparency in data collection

### Accessibility
- [ ] TalkBack support
- [ ] Content descriptions
- [ ] Scalable typography
- [ ] High contrast themes
- [ ] Keyboard navigation

### Internationalization
- [ ] Multilingual support (Vietnamese & English)
- [ ] Currency formatting
- [ ] Date/time localization
- [ ] RTL language support 