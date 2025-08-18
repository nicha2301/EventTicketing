import { useQuery } from "@tanstack/react-query";
import { PaymentResponseDto, PaymentResponseDtoStatus, getCurrentUserPayments } from "@/lib/api/generated/client";
import { useInitialization } from "@/hooks/useInitialization";

interface PaymentHistoryItem extends PaymentResponseDto {
  event?: {
    imageUrl?: string;
    startTime: string;
  };
}

interface PaymentHistoryResponse {
  content: PaymentHistoryItem[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Hook để lấy lịch sử thanh toán với filter
export function usePaymentHistory(status?: PaymentResponseDtoStatus, page = 0, size = 20) {
  const { isAuthenticated } = useInitialization();
  
  return useQuery({
    queryKey: ["payment-history", status, page, size],
    queryFn: async ({ signal }): Promise<PaymentHistoryResponse> => {
      const response = await getCurrentUserPayments(signal);
      const allPayments = (response.data || []) as PaymentHistoryItem[];

      const filteredData = status
        ? allPayments.filter((p) => p.status === status)
        : allPayments;

      const startIndex = page * size;
      const endIndex = startIndex + size;
      const paginatedData = filteredData.slice(startIndex, endIndex);

      return {
        content: paginatedData,
        totalElements: filteredData.length,
        totalPages: Math.ceil(filteredData.length / size) || 1,
        size,
        number: page,
      };
    },
    placeholderData: (previousData) => previousData,
    enabled: isAuthenticated,
    retry: 1,
  });
}
