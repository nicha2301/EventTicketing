import { 
  getTicketById,
  getMyTickets,
  getMyPendingTickets,
  cancelTicket as cancelTicketAPI,
  type TicketDto,
  type GetMyTicketsStatus,
  type ApiResponseTicketDto,
  type ApiResponsePageTicketDto,
  type Pageable,
  ApiResponsePageTicketTypeDto
} from '../generated/client';
import { http } from "../http";

export async function getTicketDetail(ticketId: string, signal?: AbortSignal): Promise<TicketDto> {
  try {
    const response = await getTicketById(ticketId, signal);
    
    if (response.data?.success && response.data?.data) {
      return response.data.data;
    }
    
    throw new Error('Invalid ticket data received');
  } catch (error) {
    console.error('Error fetching ticket detail:', error);
    throw error;
  }
}

export async function getUserTickets(
  status?: GetMyTicketsStatus,
  page = 0,
  size = 20,
  signal?: AbortSignal
) {
  try {
    const params = new URLSearchParams();
    params.set('page', page.toString());
    params.set('size', size.toString());
    if (status) {
      params.set('status', status);
    }
    
    const response = await http<ApiResponsePageTicketDto>({
      url: `/api/tickets/my-tickets?${params.toString()}`,
      method: 'GET',
      signal
    });
    
    return response.data;
  } catch (error) {
    throw error;
  }
}

export async function getUserPendingTickets(signal?: AbortSignal) {
  try {
    const response = await getMyPendingTickets(signal);
    return response;
  } catch (error) {
    console.error('Error fetching pending tickets:', error);
    throw error;
  }
}

export async function cancelUserTicket(ticketId: string) {
  try {
    const response = await cancelTicketAPI(ticketId);
    return response;
  } catch (error) {
    console.error('Error canceling ticket:', error);
    throw error;
  }
}

export async function getTicketTypesByEvent(eventId: string, signal?: AbortSignal) {
  try {
    const response = await http<ApiResponsePageTicketTypeDto>({
      url: `/api/events/${eventId}/ticket-types`,
      method: "GET",
      params: {
        page: 0,
        size: 50
      },
      signal
    });
    console.log('Fetched ticket types for event:', response);
    return response.data?.data?.content || [];
  } catch (error) {
    console.error("Error fetching ticket types by event:", error);
    throw error;
  }
}