import { 
  getTicketById,
  getMyTickets,
  getMyPendingTickets,
  cancelTicket as cancelTicketAPI,
  type TicketDto,
  type GetMyTicketsStatus,
  type ApiResponseTicketDto,
  type ApiResponsePageTicketDto,
  type Pageable
} from '../generated/client';

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
    const pageable: Pageable = {
      page,
      size,
      sort: []
    };
    
    const response = await getMyTickets({
      status,
      pageable
    }, signal);
    
    return response;
  } catch (error) {
    console.error('Error fetching user tickets:', error);
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
