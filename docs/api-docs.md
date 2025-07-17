# Event Ticketing API Documentation

Tài liệu này cung cấp thông tin chi tiết về các API của hệ thống Event Ticketing.

## Mục lục
1. [Giới thiệu](#introduction)
2. [Xác thực](#authentication)
3. [API cho User](#user-apis)
4. [API cho Organizer](#organizer-apis)
5. [API cho Admin](#admin-apis)

<a name="introduction"></a>
## 1. Giới thiệu

Hệ thống Event Ticketing cung cấp các API cho 3 nhóm người dùng chính:
- **User**: Người dùng thông thường, có thể xem, tìm kiếm và mua vé sự kiện
- **Organizer**: Người tổ chức sự kiện, có thể tạo và quản lý sự kiện, xem báo cáo doanh thu
- **Admin**: Quản trị viên hệ thống, có quyền quản lý tất cả các khía cạnh của hệ thống

<a name="authentication"></a>
## 2. Xác thực

Hầu hết các API yêu cầu xác thực bằng JSON Web Token (JWT).

**Đăng nhập để lấy token:**
```
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

**Sử dụng token trong các request:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

<a name="user-apis"></a>
## 3. API cho User

*Các API dành cho người dùng thông thường sẽ được thêm vào sau khi test hoàn tất.*

<a name="organizer-apis"></a>
## 4. API cho Organizer

### 4.1 Quản lý sự kiện

#### 4.1.1 Xem danh sách sự kiện của Organizer

Lấy danh sách tất cả sự kiện do người tổ chức tạo ra.

**Endpoint:**
```
GET /api/events/organizer/{organizerId}
```

**Các tham số:**
- `organizerId`: ID của người tổ chức (UUID)
- `pageable`: (Tùy chọn) Thông tin phân trang (size, page, sort)

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Đã lấy danh sách sự kiện của người tổ chức thành công",
  "data": {
    "content": [
      {
        "id": "bbbbbbbb-1111-2222-3333-444444444444",
        "title": "Giải bóng đá giao hữu",
        "description": "Giải bóng đá giao hữu giữa các đội tuyển doanh nghiệp",
        "shortDescription": "Giải bóng đá giao hữu doanh nghiệp",
        "organizerId": "22222222-aaaa-bbbb-cccc-dddddddddddd",
        "organizerName": "Organizer User",
        "categoryId": "22222222-2222-2222-2222-222222222222",
        "categoryName": "Thể thao",
        "locationId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
        "locationName": "Sân vận động Mỹ Đình",
        "address": "Đường Lê Đức Thọ, Mỹ Đình",
        "city": "Hà Nội",
        "latitude": 21.0305,
        "longitude": 105.7638,
        "status": "PUBLISHED",
        "maxAttendees": 1000,
        "currentAttendees": 0,
        "featuredImageUrl": "football.jpg",
        "imageUrls": ["football_1.jpg"],
        "minTicketPrice": 0.00,
        "maxTicketPrice": 0.00,
        "startDate": "2025-09-20 08:00:00",
        "endDate": "2025-09-20 17:00:00",
        "createdAt": "2025-07-14 17:55:56",
        "updatedAt": "2025-07-14 17:55:56",
        "isPrivate": false,
        "isFeatured": false,
        "isFree": true,
        "ticketTypes": [
          {
            "id": "a4444444-4444-4444-4444-444444444444",
            "name": "Vé Miễn Phí",
            "description": "Vé tham dự miễn phí",
            "price": 0.00,
            "quantity": 1000,
            "availableQuantity": 1000,
            "quantitySold": 0,
            "eventId": "bbbbbbbb-1111-2222-3333-444444444444",
            "salesStartDate": "2025-08-20T00:00:00",
            "salesEndDate": "2025-09-19T23:59:59",
            "maxTicketsPerCustomer": 2,
            "minTicketsPerOrder": 1,
            "isEarlyBird": false,
            "isVIP": false,
            "isActive": true
          }
        ]
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "last": true,
    "totalElements": 5,
    "totalPages": 1,
    "size": 20,
    "number": 0,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "first": true,
    "numberOfElements": 5,
    "empty": false
  }
}
```

**Quyền truy cập:**
- ORGANIZER (chỉ xem sự kiện của chính mình)
- ADMIN (xem sự kiện của bất kỳ người tổ chức nào)

<a name="admin-apis"></a>
## 5. API cho Admin

*Các API dành cho quản trị viên sẽ được thêm vào sau khi test hoàn tất.* 