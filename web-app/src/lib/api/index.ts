// Manual API modules
export * from './modules/auth';
export * from './modules/events';
export * from './modules/tickets';
export * from './modules/notifications';
export * from './modules/categories';
export * from './modules/locations';

// Generated client types only (to avoid conflicts)
export type {
  EventDto,
  UserDto,
  CategoryDto,
  LocationDto,
  TicketDto,
  TicketTypeDto,
  CommentDto,
  CommentResponse,
  RatingResponse,
  NotificationResponse,
  PaymentResponseDto,
  UserAuthResponseDto,
  UserCreateDto,
  LoginRequestDto,
  ApiResponseEventDto,
  ApiResponseUserDto,
  ApiResponseCategoryDto,
  ApiResponseLocationDto,
  ApiResponseTicketDto,
  ApiResponsePageTicketDto,
  ApiResponseTicketTypeDto,
  ApiResponsePageTicketTypeDto,
  ApiResponseNotificationResponse,
  ApiResponsePaymentResponseDto,
  ApiResponseUserAuthResponseDto,
  EventCreateDto,
  EventUpdateDto,
  EventCancelDto,
  UploadEventImageBody,
  UploadEventImageParams,
  CloudinaryImageRequest,
  CreateEventWithImagesBody,
  CreateEventWithImagesParams,
  ReportRequest,
  PageEventDto,
  PagedResponseUserDto,
  PageCategoryDto,
  PageLocationDto,
  PageTicketDto,
  PageTicketTypeDto,
  PageNotificationResponse,
  Pageable,
  SortObject,
  PageableObject,
  TicketCheckInRequestDto,
} from './generated/client';

// Generated client enums
export {
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
} from './generated/client';
