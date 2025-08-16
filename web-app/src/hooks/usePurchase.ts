import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { 
  purchaseTickets,
  createPayment,
  getTicketTypesByEventId,
  checkTicketAvailability,
  type TicketPurchaseDto,
  type PaymentCreateDto,
  type TicketItemDto,
  type TicketPurchaseResponseDto,
  type PaymentResponseDto,
  type TicketTypeDto,
  type Pageable 
} from "@/lib/api/generated/client";
import { useAuthStore } from "@/store/auth";
import { toast } from "sonner";

// Hook lấy ticket types của event
export function useTicketTypes(eventId: string) {
  return useQuery({
    queryKey: ["ticket-types", eventId],
    queryFn: async () => {
      const response = await getTicketTypesByEventId(eventId, { 
        pageable: { page: 0, size: 50 } as Pageable 
      });
      
      if (response.data?.success && response.data?.data) {
        return response.data.data;
      }
      
      throw new Error("Failed to fetch ticket types");
    },
    enabled: !!eventId,
  });
}

// Hook check ticket availability
export function useTicketAvailability(ticketTypeId: string, quantity: number) {
  return useQuery({
    queryKey: ["ticket-availability", ticketTypeId, quantity],
    queryFn: async () => {
      const response = await checkTicketAvailability(ticketTypeId, { quantity });
      
      if (response.data?.success) {
        return response.data.data;
      }
      
      throw new Error("Failed to check ticket availability");
    },
    enabled: !!ticketTypeId && quantity > 0,
    refetchInterval: 30000, 
  });
}

// Hook purchase tickets
export function usePurchaseTickets() {
  const queryClient = useQueryClient();
  const { currentUser } = useAuthStore();

  return useMutation({
    mutationFn: async (purchaseData: TicketPurchaseDto): Promise<TicketPurchaseResponseDto> => {
      const response = await purchaseTickets(purchaseData);
      
      if (response.data?.success && response.data?.data) {
        return response.data.data;
      }
      
      throw new Error(response.data?.message || "Failed to purchase tickets");
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["my-tickets"] });
      queryClient.invalidateQueries({ queryKey: ["my-pending-tickets"] });
      queryClient.invalidateQueries({ queryKey: ["ticket-types", data.eventId] });
      
      toast.success("Đặt vé thành công! Vui lòng thanh toán để hoàn tất.");
    },
    onError: (error: any) => {
      console.error("Purchase tickets error:", error);
      const message = error?.response?.data?.message || error?.message || "Không thể đặt vé";
      toast.error(message);
    },
  });
}

// Hook create payment
export function useCreatePayment() {
  return useMutation({
    mutationFn: async (paymentData: PaymentCreateDto): Promise<PaymentResponseDto> => {
      const response = await createPayment(paymentData);
      return response.data;
    },
    onSuccess: (data) => {
      if (data.paymentUrl) {
        sessionStorage.setItem('pendingPaymentInfo', JSON.stringify({
          orderId: data.id,
          paymentId: data.id,
          amount: data.amount,
          eventTitle: data.eventTitle
        }));
        
        window.location.href = data.paymentUrl;
      } else {
        toast.success("Tạo thanh toán thành công!");
      }
    },
    onError: (error: any) => {
      console.error("Create payment error:", error);
      const message = error?.response?.data?.message || error?.message || "Không thể tạo thanh toán";
      toast.error(message);
    },
  });
}

export function usePurchaseFlow() {
  const purchaseTicketsMutation = usePurchaseTickets();
  const createPaymentMutation = useCreatePayment();
  const { currentUser } = useAuthStore();

  const completePurchase = async (
    eventId: string,
    selectedTickets: TicketItemDto[],
    buyerInfo: {
      name: string;
      email: string;
      phone?: string;
    },
    paymentMethod: string = "MOMO",
    promoCode?: string
  ) => {
    try {
      // Step 1: Purchase tickets (reserve them)
      const purchaseData: TicketPurchaseDto = {
        eventId,
        tickets: selectedTickets,
        buyerName: buyerInfo.name,
        buyerEmail: buyerInfo.email,
        buyerPhone: buyerInfo.phone,
        paymentMethod,
        promoCode,
      };

      const purchaseResult = await purchaseTicketsMutation.mutateAsync(purchaseData);
      
      sessionStorage.setItem('purchaseTicketInfo', JSON.stringify({
        orderId: purchaseResult.orderId,
        ticketId: purchaseResult.tickets?.[0]?.id,
        tickets: purchaseResult.tickets,
        eventTitle: purchaseResult.eventTitle,
        totalAmount: purchaseResult.totalAmount
      }));
      
      // Step 2: Create payment for the purchased tickets
      if (purchaseResult.tickets && purchaseResult.tickets.length > 0) {
        const firstTicket = purchaseResult.tickets[0];
        
        const paymentData: PaymentCreateDto = {
          ticketId: firstTicket.id!,
          amount: purchaseResult.totalAmount,
          paymentMethod: paymentMethod.toLowerCase(),
          returnUrl: `${window.location.origin}/payment/pending?orderId=${purchaseResult.orderId}&paymentId=${firstTicket.id}`,
          description: `Thanh toán vé ${purchaseResult.eventTitle}`,
          metadata: {
            orderId: purchaseResult.orderId,
            eventId: purchaseResult.eventId,
            ticketCount: selectedTickets.reduce((sum, item) => sum + item.quantity, 0).toString(),
          },
        };

        await createPaymentMutation.mutateAsync(paymentData);
      }

      return purchaseResult;
    } catch (error) {
      throw error;
    }
  };

  return {
    completePurchase,
    isPurchasing: purchaseTicketsMutation.isPending,
    isCreatingPayment: createPaymentMutation.isPending,
    isLoading: purchaseTicketsMutation.isPending || createPaymentMutation.isPending,
  };
}

// Validation helpers
export function validatePurchaseData(
  selectedTickets: TicketItemDto[],
  buyerInfo: { name: string; email: string; phone?: string }
): string[] {
  const errors: string[] = [];

  if (!selectedTickets || selectedTickets.length === 0) {
    errors.push("Vui lòng chọn ít nhất một loại vé");
  }

  selectedTickets.forEach((ticket, index) => {
    if (!ticket.ticketTypeId) {
      errors.push(`Vé thứ ${index + 1}: Vui lòng chọn loại vé`);
    }
    if (!ticket.quantity || ticket.quantity < 1) {
      errors.push(`Vé thứ ${index + 1}: Số lượng phải lớn hơn 0`);
    }
  });

  if (!buyerInfo.name || buyerInfo.name.trim().length < 2) {
    errors.push("Tên người mua phải có ít nhất 2 ký tự");
  }

  if (!buyerInfo.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(buyerInfo.email)) {
    errors.push("Email không hợp lệ");
  }

  if (buyerInfo.phone && !/^[0-9]{10,11}$/.test(buyerInfo.phone.replace(/\s/g, ""))) {
    errors.push("Số điện thoại không hợp lệ");
  }

  return errors;
}

export function calculateTotalAmount(
  selectedTickets: TicketItemDto[],
  ticketTypes: TicketTypeDto[],
  promoDiscount: number = 0
): {
  subtotal: number;
  discount: number;
  total: number;
} {
  const subtotal = selectedTickets.reduce((sum, item) => {
    const ticketType = ticketTypes.find(t => t.id === item.ticketTypeId);
    return sum + (ticketType?.price || 0) * item.quantity;
  }, 0);

  const discount = Math.round(subtotal * (promoDiscount / 100));
  const total = subtotal - discount;

  return { subtotal, discount, total };
}
