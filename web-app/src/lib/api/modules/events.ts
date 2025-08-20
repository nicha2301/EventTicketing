import { http } from "../http";
import type {
  EventDto,
  ApiResponsePageEventDto,
  EventCancelDto,
  EventUpdateDto,
  UploadEventImageBody,
  UploadEventImageParams,
  CloudinaryImageRequest,
  EventCreateDto,
  CreateEventWithImagesBody,
  CreateEventWithImagesParams,
} from "../generated/client";
import {
  publishEvent as publishEventApi,
  cancelEvent as cancelEventApi,
  updateEvent as updateEventApi,
  uploadEventImage as uploadEventImageApi,
  saveCloudinaryImage as saveCloudinaryImageApi,
  createEvent as createEventApi,
  createEventWithImages as createEventWithImagesApi,
  getKPIDashboard as getKPIDashboardApi,
  getEventPerformance as getEventPerformanceApi,
  getDailyRevenue as getDailyRevenueApi,
  getTicketSalesByType as getTicketSalesByTypeApi,
  getPaymentMethodsAnalysis as getPaymentMethodsAnalysisApi,
  getAttendeeAnalytics as getAttendeeAnalyticsApi,
  getRegistrationTimeline as getRegistrationTimelineApi,
  generateSalesReport as generateSalesReportApi,
  generateRevenueReport as generateRevenueReportApi,
  generateAttendanceReport as generateAttendanceReportApi,
  exportReportToPdf as exportReportToPdfApi,
  exportReportToExcel as exportReportToExcelApi,
  type ReportRequest,
} from "../generated/client";

export interface EventsListResponse {
  events: EventDto[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
}

export interface SearchEventsParams {
  keyword?: string;
  categoryId?: string;
  locationId?: string;
  minPrice?: number;
  maxPrice?: number;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const listEvents = async (params?: {
  page?: number;
  size?: number;
  categoryId?: string;
  search?: string;
}): Promise<EventsListResponse> => {
  const response = await http<ApiResponsePageEventDto>({
    url: "/api/events",
    method: "GET",
    params: {
      page: params?.page || 0,
      size: params?.size || 10,
      categoryId: params?.categoryId,
      search: params?.search,
    },
  });

  const { data } = response.data;
  if (!data) {
    throw new Error("Failed to fetch events");
  }

  return {
    events: data.content || [],
    totalPages: data.totalPages || 0,
    totalElements: data.totalElements || 0,
    currentPage: data.number || 0,
  };
};

export const searchEvents = async (params: SearchEventsParams): Promise<EventsListResponse> => {
  const response = await http<ApiResponsePageEventDto>({
    url: "/api/events/search",
    method: "GET",
    params: {
      keyword: params.keyword,
      categoryId: params.categoryId,
      locationId: params.locationId,
      minPrice: params.minPrice,
      maxPrice: params.maxPrice,
      startDate: params.startDate,
      endDate: params.endDate,
      page: params.page || 0,
      size: params.size || 12,
      sort: params.sort,
    },
  });

  const { data } = response.data;
  if (!data) {
    throw new Error("Failed to search events");
  }

  return {
    events: data.content || [],
    totalPages: data.totalPages || 0,
    totalElements: data.totalElements || 0,
    currentPage: data.number || 0,
  };
};

export const getEventById = async (id: string): Promise<EventDto> => {
  const response = await http<{ data: EventDto }>({
    url: `/api/events/${id}`,
    method: "GET",
  });

  const { data } = response.data;
  if (!data) {
    throw new Error("Event not found");
  }

  return data;
};

export const getFeaturedEvents = async (limit: number = 6): Promise<EventDto[]> => {
  const response = await http<{ data: EventDto[] }>({
    url: "/api/events/featured",
    method: "GET",
    params: { limit },
  });

  const { data } = response.data;
  return data || [];
};

export const getUpcomingEvents = async (limit: number = 6): Promise<EventDto[]> => {
  const response = await http<{ data: EventDto[] }>({
    url: "/api/events/upcoming",
    method: "GET",
    params: { limit },
  });

  const { data } = response.data;
  return data || [];
};

export const getOrganizerEvents = async (
  organizerId: string,
  page = 0,
  size = 10,
  signal?: AbortSignal
) => {
  const params = new URLSearchParams();
  params.set('page', String(page));
  params.set('size', String(size));
  const response = await http<ApiResponsePageEventDto>({
    url: `/api/events/organizer/${organizerId}?${params.toString()}`,
    method: 'GET',
    signal,
  });
  return response.data;
};

export const publishEvent = async (id: string) => {
  return publishEventApi(id);
};

export const cancelEvent = async (id: string, dto: EventCancelDto) => {
  return cancelEventApi(id, dto);
};

export const updateEvent = async (id: string, dto: EventUpdateDto) => {
  return updateEventApi(id, dto);
};

export const uploadEventImage = async (
  id: string,
  body: UploadEventImageBody,
  params?: UploadEventImageParams,
) => {
  return uploadEventImageApi(id, body, params);
};

export const saveCloudinaryImage = async (
  id: string,
  req: CloudinaryImageRequest,
) => {
  return saveCloudinaryImageApi(id, req);
};

export const createEvent = async (dto: EventCreateDto) => {
  return createEventApi(dto);
};

export const createEventWithImages = async (
  body: CreateEventWithImagesBody,
  params: CreateEventWithImagesParams,
) => {
  return createEventWithImagesApi(body, params);
};

export const getKPIDashboard = async (eventId: string, signal?: AbortSignal) => getKPIDashboardApi(eventId, signal);
export const getEventPerformance = async (eventId: string, signal?: AbortSignal) => getEventPerformanceApi(eventId, signal);
export const getDailyRevenue = async (
  params: { eventId: string; startDate?: string; endDate?: string },
  signal?: AbortSignal
) => {
  let { startDate, endDate } = params;

  const extractDatePart = (dateTimeStr: string | undefined): string | undefined => {
    if (!dateTimeStr) return undefined;
    return dateTimeStr.split(' ')[0].split('T')[0];
  };

  try {
    if (!startDate || !endDate) {
      const event = await getEventById(params.eventId);
      const eventSalesStart = (event as any)?.salesStartDate as string | undefined;
      const eventSalesEnd = (event as any)?.salesEndDate as string | undefined;
      const eventStart = (event as any)?.startDate as string | undefined;
      const eventEnd = (event as any)?.endDate as string | undefined;

      startDate = startDate ?? extractDatePart(eventSalesStart) ?? extractDatePart(eventStart);
      endDate = endDate ?? extractDatePart(eventSalesEnd) ?? extractDatePart(eventEnd);
    }
  } catch {
  }

  // Final fallback: last 30 days
  if (!startDate || !endDate) {
    const fallbackEnd = new Date();
    const fallbackStart = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
    endDate = endDate ?? fallbackEnd.toISOString().split('T')[0];
    startDate = startDate ?? fallbackStart.toISOString().split('T')[0];
  }

  startDate = extractDatePart(startDate);
  endDate = extractDatePart(endDate);

  return getDailyRevenueApi(
    {
      eventId: params.eventId,
      startDate: startDate!,
      endDate: endDate!,
    },
    signal,
  );
};
export const getTicketSalesByType = async (eventId: string, signal?: AbortSignal) => getTicketSalesByTypeApi(eventId, signal);
export const getPaymentMethodsAnalysis = async (eventId: string, signal?: AbortSignal) => getPaymentMethodsAnalysisApi(eventId, signal);
export const getAttendeeAnalytics = async (eventId: string, signal?: AbortSignal) => getAttendeeAnalyticsApi(eventId, signal);
export const getRegistrationTimeline = async (eventId: string, signal?: AbortSignal) => getRegistrationTimelineApi(eventId, signal);

export const generateSalesReport = async (req: ReportRequest) => generateSalesReportApi(req);
export const generateRevenueReport = async (req: ReportRequest) => generateRevenueReportApi(req);
export const generateAttendanceReport = async (req: ReportRequest) => generateAttendanceReportApi(req);
export const exportReportToPdf = async (reportId: number) => exportReportToPdfApi(reportId);
export const exportReportToExcel = async (reportId: number) => exportReportToExcelApi(reportId);

export const getEventImages = async (
  id: string,
  signal?: AbortSignal
) => {
  const response = await http<{ data: any[] }>({
    url: `/api/events/${id}/images`,
    method: 'GET',
    signal,
  });
  return response.data?.data ?? [];
};

import { deleteEvent as deleteEventApi, deleteEventImage as deleteEventImageApi } from "../generated/client";

export const deleteEvent = async (id: string) => deleteEventApi(id);
export const deleteEventImage = async (id: string, imageId: string) => deleteEventImageApi(id, imageId);
