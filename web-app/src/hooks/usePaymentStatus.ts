import { useQuery } from "@tanstack/react-query";
import { TicketDto, getTicketById } from "@/lib/api/generated/client";

// Hook check trạng thái thanh toán thông qua ticket
export function usePaymentStatus(ticketId: string | null, enabled: boolean = true) {
  return useQuery({
    queryKey: ['payment-status', ticketId],
    queryFn: async (): Promise<TicketDto | null> => {
      if (!ticketId) return null;
      
      try {
        const response = await getTicketById(ticketId);
        
        if (response.data?.success && response.data?.data) {
          return response.data.data;
        }
        
        return null;
      } catch (error) {
        console.error('Error checking payment status:', error);
        return null;
      }
    },
    enabled: enabled && !!ticketId,
    refetchInterval: 5000,
    retry: false,
  });
}

// Hook check trạng thái thanh toán thủ công
export function useCheckPaymentStatus() {
  return async (ticketId: string): Promise<TicketDto | null> => {
    if (!ticketId) return null;
    
    try {
      const response = await getTicketById(ticketId);
      
      if (response.data?.success && response.data?.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      console.error('Error checking payment status:', error);
      throw error;
    }
  };
}
