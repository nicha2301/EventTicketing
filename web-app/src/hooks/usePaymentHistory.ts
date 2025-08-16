import { useQuery } from "@tanstack/react-query";
import { PaymentResponseDto, PaymentResponseDtoStatus } from "@/lib/api/generated/client";
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

// Mock data for development
const mockPaymentHistory: PaymentHistoryItem[] = [
  {
    id: "pay-1",
    amount: 500000,
    paymentMethod: "VNPay",
    status: PaymentResponseDtoStatus.COMPLETED,
    createdAt: "2024-12-15T10:30:00Z",
    updatedAt: "2024-12-15T10:32:00Z",
    eventId: "event-1",
    eventTitle: "Lễ hội âm nhạc mùa hè 2024",
    ticketId: "ticket-1",
    ticketTypeName: "VIP",
    userId: "user-1",
    userName: "Nguyễn Văn A",
    paymentUrl: "https://payment.vnpay.vn/...",
    transactionId: "VNP123456789",
    event: {
      imageUrl: "/assets/images/events/music-festival.jpg",
      startTime: "2024-12-25T19:00:00Z"
    }
  },
  {
    id: "pay-2", 
    amount: 300000,
    paymentMethod: "MoMo",
    status: PaymentResponseDtoStatus.PENDING,
    createdAt: "2024-12-10T14:20:00Z",
    updatedAt: "2024-12-10T14:20:00Z",
    eventId: "event-2",
    eventTitle: "Hội thảo công nghệ AI",
    ticketId: "ticket-2",
    ticketTypeName: "Standard",
    userId: "user-1",
    userName: "Nguyễn Văn A",
    paymentUrl: "https://payment.momo.vn/...",
    transactionId: "MOMO987654321",
    event: {
      imageUrl: "/assets/images/events/tech-conference.jpg", 
      startTime: "2024-12-30T09:00:00Z"
    }
  },
  {
    id: "pay-3",
    amount: 750000,
    paymentMethod: "ZaloPay",
    status: PaymentResponseDtoStatus.FAILED,
    createdAt: "2024-12-05T16:45:00Z",
    updatedAt: "2024-12-05T16:47:00Z",
    eventId: "event-3",
    eventTitle: "Triển lãm nghệ thuật đương đại",
    ticketId: "ticket-3", 
    ticketTypeName: "Premium",
    userId: "user-1",
    userName: "Nguyễn Văn A",
    transactionId: "ZALO555666777",
    event: {
      imageUrl: "/assets/images/events/art-exhibition.jpg",
      startTime: "2024-12-20T10:00:00Z"
    }
  }
];

// Hook để lấy lịch sử thanh toán với filter
export function usePaymentHistory(status?: PaymentResponseDtoStatus, page = 0, size = 20) {
  const { isAuthenticated } = useInitialization();
  
  return useQuery({
    queryKey: ["payment-history", status, page, size],
    queryFn: async (): Promise<PaymentHistoryResponse> => {
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      let filteredData = mockPaymentHistory;
      if (status) {
        filteredData = mockPaymentHistory.filter(payment => payment.status === status);
      }
      
      const startIndex = page * size;
      const endIndex = startIndex + size;
      const paginatedData = filteredData.slice(startIndex, endIndex);
      
      return {
        content: paginatedData,
        totalElements: filteredData.length,
        totalPages: Math.ceil(filteredData.length / size),
        size,
        number: page
      };
    },
    placeholderData: (previousData) => previousData,
    enabled: isAuthenticated,
    retry: 1,
  });
}
