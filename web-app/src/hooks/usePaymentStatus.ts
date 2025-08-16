import { useQuery } from "@tanstack/react-query";
import { ApiResponsePaymentResponseDto, PaymentResponseDto } from "@/lib/api/generated/client";
import { http } from "@/lib/api/http";

// Hook check trạng thái thanh toán
export function usePaymentStatus(orderId: string | null, enabled: boolean = true) {
  return useQuery({
    queryKey: ['payment-status', orderId],
    queryFn: async (): Promise<PaymentResponseDto | null> => {
      if (!orderId) return null;
      
      try {
        const response = await http<ApiResponsePaymentResponseDto>({
          url: `/api/payments/status/${orderId}`,
          method: 'GET'
        });
        
        return response.data?.data || null;
      } catch (error) {
        console.error('Error checking payment status:', error);
        return {
          id: orderId,
          status: 'PENDING' as any,
          amount: 0,
          paymentMethod: 'MOMO',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          eventId: '',
          eventTitle: '',
          ticketId: '',
          ticketTypeName: '',
          userId: '',
          userName: ''
        };
      }
    },
    enabled: enabled && !!orderId,
    refetchInterval: 5000, // Check every 5 seconds
    retry: false,
  });
}

// Hook check trạng thái thanh toán thủ công
export function useCheckPaymentStatus() {
  return async (orderId: string): Promise<PaymentResponseDto | null> => {
    if (!orderId) return null;
    
    try {
      const response = await http<ApiResponsePaymentResponseDto>({
        url: `/api/payments/status/${orderId}`,
        method: 'GET'
      });
      
      return response.data?.data || null;
    } catch (error) {
      console.error('Error checking payment status:', error);
      throw error;
    }
  };
}
