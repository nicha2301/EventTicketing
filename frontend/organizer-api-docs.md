# API Documentation for Event Organizers

This document provides detailed information about the APIs available for event organizers in the Event Ticketing system.

## Authentication

### Login

```
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "organizer@example.com",
  "password": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "id": "22222222-aaaa-bbbb-cccc-dddddddddddd",
    "email": "organizer@example.com",
    "fullName": "Organizer User",
    "role": "ORGANIZER",
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJvcmdhbml6ZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTI2NTM3NDAsImV4cCI6MTc1Mjc0MDE0MCwidHlwZSI6ImFjY2VzcyIsInVzZXJJZCI6IjIyMjIyMjIyLWFhYWEtYmJiYi1jY2NjLWRkZGRkZGRkZGRkZCIsInJvbGUiOiJPUkdBTklaRVIifQ.rk9OXeVFnqD70mQwoSP07cnyml1Krl9VhBtTGq4nbuPOC-SKXMlJSAXoNy__k8bB3MExuIK-M04K6sB-xgwL5A",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJvcmdhbml6ZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTI2NTM3NDAsImV4cCI6MTc1MzI1ODU0MCwidHlwZSI6InJlZnJlc2giLCJ1c2VySWQiOiIyMjIyMjIyMi1hYWFhLWJiYmItY2NjYy1kZGRkZGRkZGRkZGQiLCJyb2xlIjoiT1JHQU5JWkVSIn0.MN-olp5oKCx5VTTx8if9xtXxD2bh3Eg9v0y7KvSVN5agD_dG0x8E7q16M31WlOEhY4dhv7TopIcumJUvWPvHJQ",
    "profilePictureUrl": "organizer.jpg"
  }
}
```

Use the returned token for subsequent API calls in the `Authorization` header: `Bearer {token}`

## Event Management

### List Organizer Events

```
GET /api/events/organizer/{organizerId}
```

**Path Parameters:**
- `organizerId` - The ID of the organizer

**Response Example:**
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
        "imageUrls": [
          "football_1.jpg"
        ],
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
    "totalElements": 5,
    "totalPages": 1,
    "size": 20,
    "number": 0,
    "first": true,
    "numberOfElements": 5,
    "empty": false
  }
}
```

### Create Event

```
POST /api/events
```

**Request Body:**
```json
{
  "title": "Workshop Lap trinh Python",
  "description": "Workshop day lap trinh Python co ban den nang cao",
  "shortDescription": "Workshop Python cho nguoi moi bat dau",
  "categoryId": "33333333-3333-3333-3333-333333333333",
  "locationId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
  "address": "Pham Hung, Cau Giay",
  "city": "Ha Noi",
  "latitude": 21.0048,
  "longitude": 105.7877,
  "maxAttendees": 50,
  "startDate": "2025-12-25 09:00:00",
  "endDate": "2025-12-25 17:00:00",
  "isPrivate": false,
  "isDraft": true,
  "isFree": false
}
```

**Response Example:**
```json
{
  "success": true,
  "message": "Đã tạo sự kiện thành công",
  "data": {
    "id": "bda50b49-e882-40a2-b795-cc6e9be3db0c",
    "title": "Workshop Lap trinh Python",
    "description": "Workshop day lap trinh Python co ban den nang cao",
    "shortDescription": "Workshop Python cho nguoi moi bat dau",
    "organizerId": "22222222-aaaa-bbbb-cccc-dddddddddddd",
    "organizerName": "Organizer User",
    "categoryId": "33333333-3333-3333-3333-333333333333",
    "categoryName": "Giáo dục",
    "locationId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
    "locationName": "Trung tâm Hội nghị Quốc gia",
    "address": "Pham Hung, Cau Giay",
    "city": "Ha Noi",
    "latitude": 21.0048,
    "longitude": 105.7877,
    "status": "DRAFT",
    "maxAttendees": 50,
    "currentAttendees": 0,
    "featuredImageUrl": null,
    "imageUrls": [],
    "minTicketPrice": null,
    "maxTicketPrice": null,
    "startDate": "2025-12-25 09:00:00",
    "endDate": "2025-12-25 17:00:00",
    "createdAt": "2025-07-16 15:17:56",
    "updatedAt": "2025-07-16 15:17:56",
    "isPrivate": false,
    "isFeatured": false,
    "isFree": false,
    "ticketTypes": []
  }
}
```

### Update Event

```
PUT /api/events/{id}
```

**Path Parameters:**
- `id` - The ID of the event to update

**Request Body:** Same structure as Create Event
```json
{
  "title": "Workshop Test Event Updated",
  "description": "Workshop test event description updated",
  "shortDescription": "Workshop test updated",
  "categoryId": "33333333-3333-3333-3333-333333333333",
  "locationId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
  "address": "Test Address Updated",
  "city": "Ha Noi Updated",
  "latitude": 21.0048,
  "longitude": 105.7877,
  "maxAttendees": 100,
  "startDate": "2025-12-26 09:00:00",
  "endDate": "2025-12-26 17:00:00",
  "isPrivate": false,
  "isDraft": true,
  "isFree": false
}
```

**Response Example:**
```json
{
  "success": true,
  "message": "Đã cập nhật sự kiện thành công",
  "data": {
    "id": "ed7d48fa-aa28-48a4-ab57-13d96b03babc",
    "title": "Workshop Test Event Updated",
    "description": "Workshop test event description updated",
    "shortDescription": "Workshop test updated",
    "organizerId": "22222222-aaaa-bbbb-cccc-dddddddddddd",
    "organizerName": "Organizer User",
    "categoryId": "33333333-3333-3333-3333-333333333333",
    "categoryName": "Giáo dục",
    "locationId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
    "locationName": "Trung tâm Hội nghị Quốc gia",
    "address": "Test Address Updated",
    "city": "Ha Noi Updated",
    "latitude": 21.0048,
    "longitude": 105.7877,
    "status": "DRAFT",
    "maxAttendees": 100,
    "currentAttendees": 0,
    "featuredImageUrl": null,
    "imageUrls": [],
    "minTicketPrice": null,
    "maxTicketPrice": null,
    "startDate": "2025-12-26 09:00:00",
    "endDate": "2025-12-26 17:00:00",
    "createdAt": "2025-07-16 17:57:21",
    "updatedAt": "2025-07-16 17:57:21",
    "isPrivate": false,
    "isFeatured": false,
    "isFree": false,
    "ticketTypes": []
  }
}
```

### Delete Event

```
DELETE /api/events/{id}
```

**Path Parameters:**
- `id` - The ID of the event to delete

**Response Example:**
```json
{
  "success": true,
  "message": "Đã xóa sự kiện thành công",
  "data": true
}
```

### Publish Event

```
PUT /api/events/{id}/publish
```

> **Note:** This API requires at least one ticket type to be created for the event before publishing. Otherwise, it will return a 400 Bad Request error.

**Response Example:**
```json
{
  "success": true,
  "message": "Đã công bố sự kiện thành công",
  "data": {
    "id": "ff41232e-c106-4082-bf8b-146bcf810089",
    "title": "Test Ticket Event",
    "description": "Test ticket event description",
    "shortDescription": "Test ticket",
    "organizerId": "22222222-aaaa-bbbb-cccc-dddddddddddd",
    "organizerName": "Organizer User",
    "categoryId": "33333333-3333-3333-3333-333333333333",
    "categoryName": "Giáo dục",
    "locationId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
    "locationName": "Trung tâm Hội nghị Quốc gia",
    "address": "Test Address",
    "city": "Ha Noi",
    "latitude": 21.0048,
    "longitude": 105.7877,
    "status": "PUBLISHED",
    "maxAttendees": 50,
    "currentAttendees": 0,
    "featuredImageUrl": null,
    "imageUrls": [],
    "minTicketPrice": 500000.00,
    "maxTicketPrice": 500000.00,
    "startDate": "2025-12-25 09:00:00",
    "endDate": "2025-12-25 17:00:00",
    "createdAt": "2025-07-16 18:18:34",
    "updatedAt": "2025-07-16 18:18:34",
    "isPrivate": false,
    "isFeatured": false,
    "isFree": false,
    "ticketTypes": [
      {
        "id": "04029ea3-9d95-499e-a464-6e60506978a4",
        "name": "Standard Ticket",
        "description": "Regular entry ticket",
        "price": 500000.00,
        "quantity": 30,
        "availableQuantity": 30,
        "quantitySold": 0,
        "eventId": "ff41232e-c106-4082-bf8b-146bcf810089",
        "salesStartDate": "2025-11-25T00:00:00",
        "salesEndDate": "2025-12-24T23:59:59",
        "maxTicketsPerCustomer": 2,
        "minTicketsPerOrder": 1,
        "isEarlyBird": false,
        "isVIP": false,
        "isActive": true
      }
    ]
  }
}
```

### Cancel Event

```
PUT /api/events/{id}/cancel
```

**Request Body:**
```json
{
  "reason": "Workshop postponed due to scheduling conflicts"
}
```

**Response Example:**
```json
{
  "success": true,
  "message": "Đã hủy sự kiện thành công",
  "data": {
    "id": "bda50b49-e882-40a2-b795-cc6e9be3db0c",
    "title": "Workshop Lap trinh Python",
    "description": "Workshop day lap trinh Python co ban den nang cao",
    "shortDescription": "Workshop Python cho nguoi moi bat dau",
    "organizerId": "22222222-aaaa-bbbb-cccc-dddddddddddd",
    "organizerName": "Organizer User",
    "categoryId": "33333333-3333-3333-3333-333333333333",
    "categoryName": "Giáo dục",
    "locationId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
    "locationName": "Trung tâm Hội nghị Quốc gia",
    "address": "Pham Hung, Cau Giay",
    "city": "Ha Noi",
    "latitude": 21.0048,
    "longitude": 105.7877,
    "status": "CANCELLED",
    "maxAttendees": 50,
    "currentAttendees": 0,
    "featuredImageUrl": null,
    "imageUrls": [],
    "minTicketPrice": 500000.00,
    "maxTicketPrice": 500000.00,
    "startDate": "2025-12-25 09:00:00",
    "endDate": "2025-12-25 17:00:00",
    "createdAt": "2025-07-16 15:17:56",
    "updatedAt": "2025-07-16 15:17:56",
    "isPrivate": false,
    "isFeatured": false,
    "isFree": false
  }
}
```

## Event Image Management

### Upload Event Image

```
POST /api/events/{id}/images
```

**Path Parameters:**
- `id` - The ID of the event to upload images for

**Request Format:**
This API requires a multipart/form-data request with the following fields:
- `image`: Image file (required) - The image file to upload
- `isPrimary`: Boolean (optional, default false) - Whether this image should be set as the primary/featured image for the event

**Response Example:**
```json
{
  "success": true,
  "message": "Đã tải lên hình ảnh sự kiện thành công",
  "data": {
    "id": "d1ac4027-4d92-44f4-9616-67427089e07b",
    "url": "events/a5655ec3-712a-4341-ac19-457af7c93654/9652b0d2-b539-4203-a498-2278ab4a8c14.png",
    "eventId": "a5655ec3-712a-4341-ac19-457af7c93654",
    "isPrimary": true,
    "createdAt": "2025-07-16 21:20:21"
  }
}
```

**Lưu ý về truy cập hình ảnh:**
Đường dẫn để truy cập hình ảnh sẽ có dạng:
```
/api/files/{url}
```

Ví dụ, nếu `url` trong phản hồi là `events/a5655ec3-712a-4341-ac19-457af7c93654/9652b0d2-b539-4203-a498-2278ab4a8c14.png`, thì đường dẫn đầy đủ để truy cập hình ảnh sẽ là:
```
/api/files/events/a5655ec3-712a-4341-ac19-457af7c93654/9652b0d2-b539-4203-a498-2278ab4a8c14.png
```

### Delete Event Image

```
DELETE /api/events/{eventId}/images/{imageId}
```

**Path Parameters:**
- `eventId` - The ID of the event
- `imageId` - The ID of the image to delete

**Response Example:**
```json
{
  "success": true,
  "message": "Đã xóa hình ảnh sự kiện thành công",
  "data": true
}
```

### Get Event Images

```
GET /api/events/{eventId}/images
```

**Path Parameters:**
- `eventId` - The ID of the event to get images for

**Response Example:**
```json
{
  "success": true,
  "message": "Đã lấy danh sách hình ảnh sự kiện thành công",
  "data": [
    {
      "id": "d1ac4027-4d92-44f4-9616-67427089e07b",
      "url": "events/a5655ec3-712a-4341-ac19-457af7c93654/9652b0d2-b539-4203-a498-2278ab4a8c14.png",
      "eventId": "a5655ec3-712a-4341-ac19-457af7c93654",
      "isPrimary": true,
      "createdAt": "2025-07-16 21:20:21"
    }
  ]
}
```

## Ticket Type Management

### Create Ticket Type

```
POST /api/events/{eventId}/ticket-types
```

**Path Parameters:**
- `eventId` - The ID of the event to add ticket types to

**Request Body:**
```json
{
  "name": "Standard Ticket",
  "description": "Regular entry ticket",
  "price": 500000,
  "quantity": 30,
  "eventId": "ff41232e-c106-4082-bf8b-146bcf810089",
  "salesStartDate": "2025-11-25T00:00:00",
  "salesEndDate": "2025-12-24T23:59:59",
  "maxTicketsPerCustomer": 2,
  "minTicketsPerOrder": 1,
  "isEarlyBird": false,
  "isVIP": false,
  "isActive": true
}
```

**Response Example:**
```json
{
  "success": true,
  "message": "Đã tạo loại vé thành công",
  "data": {
    "id": "04029ea3-9d95-499e-a464-6e60506978a4",
    "name": "Standard Ticket",
    "description": "Regular entry ticket",
    "price": 500000,
    "quantity": 30,
    "availableQuantity": 30,
    "quantitySold": 0,
    "eventId": "ff41232e-c106-4082-bf8b-146bcf810089",
    "salesStartDate": "2025-11-25T00:00:00",
    "salesEndDate": "2025-12-24T23:59:59",
    "maxTicketsPerCustomer": 2,
    "minTicketsPerOrder": 1,
    "isEarlyBird": false,
    "isVIP": false,
    "isActive": true,
    "createdAt": "2025-07-16T18:22:07.3243382",
    "updatedAt": "2025-07-16T18:22:07.3243382"
  }
}
```

### Get Ticket Types for Event

```
GET /api/events/{eventId}/ticket-types
```

**Path Parameters:**
- `eventId` - The ID of the event to get ticket types for

**Response Example:**
```json
{
  "success": true,
  "message": "Đã lấy danh sách loại vé thành công",
  "data": [
    {
      "id": "04029ea3-9d95-499e-a464-6e60506978a4",
      "name": "Standard Ticket",
      "description": "Regular entry ticket",
      "price": 500000.00,
      "quantity": 30,
      "availableQuantity": 30,
      "quantitySold": 0,
      "eventId": "ff41232e-c106-4082-bf8b-146bcf810089",
      "salesStartDate": "2025-11-25T00:00:00",
      "salesEndDate": "2025-12-24T23:59:59",
      "maxTicketsPerCustomer": 2,
      "minTicketsPerOrder": 1,
      "isEarlyBird": false,
      "isVIP": false,
      "isActive": true,
      "createdAt": "2025-07-16T18:22:07.3243382",
      "updatedAt": "2025-07-16T18:22:07.3243382"
    }
  ]
}
```

### Update Ticket Type

```
PUT /api/ticket-types/{id}
```

**Path Parameters:**
- `id` - The ID of the ticket type to update

**Request Body:** Same structure as Create Ticket Type
```json
{
  "name": "Standard Ticket Updated",
  "description": "Regular entry ticket updated",
  "price": 550000,
  "quantity": 25,
  "eventId": "ff41232e-c106-4082-bf8b-146bcf810089",
  "salesStartDate": "2025-11-25T00:00:00",
  "salesEndDate": "2025-12-24T23:59:59",
  "maxTicketsPerCustomer": 3,
  "minTicketsPerOrder": 1,
  "isEarlyBird": true,
  "isVIP": false,
  "isActive": true
}
```

### Delete Ticket Type

```
DELETE /api/ticket-types/{id}
```

**Path Parameters:**
- `id` - The ID of the ticket type to delete

**Response Example:**
```json
{
  "success": true,
  "message": "Đã xóa loại vé thành công",
  "data": true
}
```

## Workflow for Publishing an Event

1. Create an event (POST /api/events) - This will create the event in DRAFT status
2. Create at least one ticket type for the event (POST /api/events/{eventId}/ticket-types)
3. Publish the event (PUT /api/events/{id}/publish) - This will change the status to PUBLISHED 

<!-- 
## Report Management

### Generate Revenue Report

```
POST /api/reports/revenue
```

**Request Body:**
```json
{
  "eventId": "a5655ec3-712a-4341-ac19-457af7c93654",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "includeRefunds": true,
  "groupBy": "MONTH"
}
```

**Response Example:**
```json
{
  "id": 1,
  "title": "Revenue Report",
  "description": "Revenue report for event Workshop Test Event",
  "type": "REVENUE",
  "format": "JSON",
  "data": {
    "totalRevenue": 1500000.00,
    "totalRefunds": 0.00,
    "netRevenue": 1500000.00,
    "currency": "VND",
    "byPeriod": [
      {
        "period": "2025-07",
        "revenue": 1500000.00,
        "refunds": 0.00,
        "netRevenue": 1500000.00
      }
    ]
  },
  "createdAt": "2025-07-16T21:45:30",
  "userId": "22222222-aaaa-bbbb-cccc-dddddddddddd",
  "eventId": "a5655ec3-712a-4341-ac19-457af7c93654"
}
```

### Generate Sales Report

```
POST /api/reports/sales
```

**Request Body:**
```json
{
  "eventId": "a5655ec3-712a-4341-ac19-457af7c93654",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "includeRefunds": true,
  "groupBy": "MONTH"
}
```

**Response Example:**
```json
{
  "id": 2,
  "title": "Sales Report",
  "description": "Sales report for event Workshop Test Event",
  "type": "SALES",
  "format": "JSON",
  "data": {
    "totalSales": 3,
    "totalRefunds": 0,
    "netSales": 3,
    "byTicketType": [
      {
        "ticketTypeId": "04029ea3-9d95-499e-a464-6e60506978a4",
        "ticketTypeName": "Standard Ticket",
        "sales": 3,
        "refunds": 0,
        "netSales": 3,
        "revenue": 1500000.00
      }
    ],
    "byPeriod": [
      {
        "period": "2025-07",
        "sales": 3,
        "refunds": 0,
        "netSales": 3
      }
    ]
  },
  "createdAt": "2025-07-16T21:46:15",
  "userId": "22222222-aaaa-bbbb-cccc-dddddddddddd",
  "eventId": "a5655ec3-712a-4341-ac19-457af7c93654"
}
```

### Generate Attendance Report

```
POST /api/reports/attendance
```

**Request Body:**
```json
{
  "eventId": "a5655ec3-712a-4341-ac19-457af7c93654"
}
```

**Response Example:**
```json
{
  "id": 3,
  "title": "Attendance Report",
  "description": "Attendance report for event Workshop Test Event",
  "type": "ATTENDANCE",
  "format": "JSON",
  "data": {
    "totalTickets": 3,
    "totalAttendees": 2,
    "attendanceRate": 66.67,
    "byTicketType": [
      {
        "ticketTypeId": "04029ea3-9d95-499e-a464-6e60506978a4",
        "ticketTypeName": "Standard Ticket",
        "tickets": 3,
        "attendees": 2,
        "attendanceRate": 66.67
      }
    ]
  },
  "createdAt": "2025-07-16T21:47:00",
  "userId": "22222222-aaaa-bbbb-cccc-dddddddddddd",
  "eventId": "a5655ec3-712a-4341-ac19-457af7c93654"
}
```

### Get Report By ID

```
GET /api/reports/{reportId}
```

**Path Parameters:**
- `reportId` - The ID of the report to retrieve

**Response Example:**
```json
{
  "id": 1,
  "title": "Revenue Report",
  "description": "Revenue report for event Workshop Test Event",
  "type": "REVENUE",
  "format": "JSON",
  "data": {
    "totalRevenue": 1500000.00,
    "totalRefunds": 0.00,
    "netRevenue": 1500000.00,
    "currency": "VND",
    "byPeriod": [
      {
        "period": "2025-07",
        "revenue": 1500000.00,
        "refunds": 0.00,
        "netRevenue": 1500000.00
      }
    ]
  },
  "createdAt": "2025-07-16T21:45:30",
  "userId": "22222222-aaaa-bbbb-cccc-dddddddddddd",
  "eventId": "a5655ec3-712a-4341-ac19-457af7c93654"
}
```

### Get Reports by Event

```
GET /api/reports/event/{eventId}
```

**Path Parameters:**
- `eventId` - The ID of the event to get reports for

**Response Example:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Revenue Report",
      "description": "Revenue report for event Workshop Test Event",
      "type": "REVENUE",
      "format": "JSON",
      "createdAt": "2025-07-16T21:45:30",
      "userId": "22222222-aaaa-bbbb-cccc-dddddddddddd",
      "eventId": "a5655ec3-712a-4341-ac19-457af7c93654"
    },
    {
      "id": 2,
      "title": "Sales Report",
      "description": "Sales report for event Workshop Test Event",
      "type": "SALES",
      "format": "JSON",
      "createdAt": "2025-07-16T21:46:15",
      "userId": "22222222-aaaa-bbbb-cccc-dddddddddddd",
      "eventId": "a5655ec3-712a-4341-ac19-457af7c93654"
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
  "totalElements": 2,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "numberOfElements": 2,
  "empty": false
}
```

### Export Report to PDF

```
GET /api/reports/{reportId}/export/pdf
```

**Path Parameters:**
- `reportId` - The ID of the report to export

**Response:**
Binary PDF file with appropriate headers:
- Content-Type: application/pdf
- Content-Disposition: attachment; filename=report-{reportId}.pdf

### Export Report to Excel

```
GET /api/reports/{reportId}/export/excel
```

**Path Parameters:**
- `reportId` - The ID of the report to export

**Response:**
Binary Excel file with appropriate headers:
- Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
- Content-Disposition: attachment; filename=report-{reportId}.xlsx

### Dashboard Data - Daily Revenue

```
GET /api/reports/dashboard/daily-revenue
```

**Query Parameters:**
- `eventId` (optional) - The ID of the event to get data for
- `startDate` (required) - Start date in ISO format (YYYY-MM-DD)
- `endDate` (required) - End date in ISO format (YYYY-MM-DD)

**Response Example:**
```json
{
  "labels": ["2025-07-10", "2025-07-11", "2025-07-12", "2025-07-13", "2025-07-14", "2025-07-15", "2025-07-16"],
  "datasets": [
    {
      "label": "Revenue",
      "data": [0, 0, 0, 0, 500000, 500000, 500000]
    }
  ],
  "total": 1500000.00,
  "currency": "VND"
}
```

### Dashboard Data - Ticket Sales by Type

```
GET /api/reports/dashboard/ticket-sales/{eventId}
```

**Path Parameters:**
- `eventId` - The ID of the event to get data for

**Response Example:**
```json
{
  "labels": ["Standard Ticket", "VIP Ticket"],
  "datasets": [
    {
      "label": "Tickets Sold",
      "data": [3, 0]
    }
  ],
  "total": 3,
  "availableTickets": 27
}
```

### Dashboard Data - Check-in Statistics

```
GET /api/reports/dashboard/check-in-statistics/{eventId}
```

**Path Parameters:**
- `eventId` - The ID of the event to get data for

**Response Example:**
```json
{
  "totalTickets": 3,
  "checkedIn": 2,
  "notCheckedIn": 1,
  "attendanceRate": 66.67,
  "checkInTimeline": {
    "labels": ["09:00", "10:00", "11:00", "12:00", "13:00", "14:00"],
    "data": [0, 1, 1, 0, 0, 0]
  }
}
```  -->