// Re-export the http instance
export { http } from "./http";

// Re-export auth API
export * from "./modules/auth";

// Re-export events API
export * from "./modules/events";

// Re-export generated client (excluding conflicts)
export {
  // Types
  type EventDto,
  type UserDto,
  type CategoryDto,
  type LocationDto,
  type TicketDto,
  type TicketTypeDto,
  type CommentDto,
  type CommentResponse,
  type RatingResponse,
  type NotificationResponse,
  type PaymentResponseDto,
  type UserAuthResponseDto,
  type UserCreateDto,
  type LoginRequestDto,
  type ApiResponseEventDto,
  type ApiResponseUserDto,
  type ApiResponseCategoryDto,
  type ApiResponseLocationDto,
  type ApiResponseTicketDto,
  type ApiResponseTicketTypeDto,
  type ApiResponseNotificationResponse,
  type ApiResponsePaymentResponseDto,
  type ApiResponseUserAuthResponseDto,
  type PageEventDto,
  type PagedResponseUserDto,
  type PageCategoryDto,
  type PageLocationDto,
  type PageTicketDto,
  type PageTicketTypeDto,
  type PageNotificationResponse,
  type Pageable,
  type SortObject,
  type PageableObject,
  // Enums
  UserAuthResponseDtoRole,
  UserCreateDtoRole,
  UserDtoRole,
  EventDtoStatus,
  TicketDtoStatus,
  CommentDtoStatus,
  RatingResponseStatus,
  NotificationResponseNotificationType,
  PaymentResponseDtoStatus,
  DeviceTokenResponseDeviceType,
  DeviceTokenRequestDeviceType,
  CommentStatusUpdateRequestStatus,
  RatingStatusUpdateRequestStatus,
  TestNotificationRequestNotificationType,
  GetMyTicketsStatus,
} from "./generated/client";
