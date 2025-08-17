"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { getEventById, type EventDto } from "@/lib/api/generated/client";
import { useTicketTypes, usePurchaseFlow, validatePurchaseData, calculateTotalAmount } from "@/hooks/usePurchase";
import { useAuthStore } from "@/store/auth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "sonner";
import { formatPriceVND } from "@/lib/utils";
import { ArrowLeft, Shield, CreditCard, Clock, MapPin, Calendar } from "lucide-react";
import Link from "next/link";
import TicketSelector from "@/components/purchase/TicketSelector";
import PaymentMethodSelector from "@/components/purchase/PaymentMethodSelector";
import PurchaseSummary from "@/components/purchase/PurchaseSummary";
import { sanitizeEventImageUrl } from "@/lib/utils/image";

interface SelectedTicket {
  ticketTypeId: string;
  quantity: number;
}

export default function PurchasePage() {
  const params = useParams();
  const router = useRouter();
  const eventId = params.id as string;
  const { currentUser, isAuthenticated, isHydrated } = useAuthStore();
  
  // State
  const [selectedTickets, setSelectedTickets] = useState<SelectedTicket[]>([]);
  const [buyerInfo, setBuyerInfo] = useState({
    name: currentUser?.fullName || "",
    email: currentUser?.email || "",
    phone: currentUser?.phoneNumber || "",
  });
  const [promoCode, setPromoCode] = useState("");
  const [promoDiscount, setPromoDiscount] = useState(0);
  const [paymentMethod, setPaymentMethod] = useState("MOMO");
  const [agreeTerms, setAgreeTerms] = useState(false);

  // Queries and mutations
  const { data: event, isLoading: eventLoading } = useQuery({
    queryKey: ["event", eventId],
    queryFn: async () => {
      const response = await getEventById(eventId);
      if (response.data?.success && response.data?.data) {
        return response.data.data;
      }
      throw new Error("Event not found");
    },
    enabled: !!eventId,
  });

  const { data: ticketTypesData, isLoading: ticketTypesLoading } = useTicketTypes(eventId);
  const { completePurchase, isLoading: purchaseLoading } = usePurchaseFlow();

  const ticketTypes = ticketTypesData?.content || [];

  useEffect(() => {
    if (currentUser && !buyerInfo.name) {
      setBuyerInfo(prev => ({
        ...prev,
        name: currentUser.fullName,
        email: currentUser.email,
        phone: currentUser.phoneNumber || "",
      }));
    }
  }, [currentUser, buyerInfo.name]);

  useEffect(() => {
    if (isHydrated && !isAuthenticated) {
      toast.error("Vui lòng đăng nhập để mua vé");
      router.push(`/login?redirect=/events/${eventId}/purchase`);
    }
  }, [isHydrated, isAuthenticated, router, eventId]);

  const { subtotal, discount, total } = calculateTotalAmount(
    selectedTickets.map(st => ({ 
      ticketTypeId: st.ticketTypeId, 
      quantity: st.quantity 
    })),
    ticketTypes,
    promoDiscount
  );

  const handleAddTicket = () => {
    if (ticketTypes.length > 0) {
      const firstAvailableType = ticketTypes.find(t => t.isActive);
      if (firstAvailableType?.id) {
        setSelectedTickets(prev => [...prev, {
          ticketTypeId: firstAvailableType.id!,
          quantity: 1,
        }]);
      }
    }
  };

  const handleRemoveTicket = (index: number) => {
    setSelectedTickets(prev => prev.filter((_, i) => i !== index));
  };

  const handleTicketChange = (index: number, ticketTypeId: string, quantity: number) => {
    setSelectedTickets(prev => {
      // If index is beyond current array, add new ticket
      if (index >= prev.length) {
        return [...prev, { ticketTypeId, quantity }];
      }
      // Otherwise update existing ticket
      return prev.map((ticket, i) => 
        i === index ? { ticketTypeId, quantity } : ticket
      );
    });
  };

  const handleApplyPromo = () => {
    // Mock promo code validation
    if (promoCode.toLowerCase() === "welcome10") {
      setPromoDiscount(10);
      toast.success("Áp dụng mã giảm giá thành công! Giảm 10%");
    } else if (promoCode.toLowerCase() === "vip20") {
      setPromoDiscount(20);
      toast.success("Áp dụng mã giảm giá thành công! Giảm 20%");
    } else if (promoCode.trim()) {
      toast.error("Mã giảm giá không hợp lệ");
    } else {
      setPromoDiscount(0);
    }
  };

  const handlePurchase = async () => {
    // Validation
    const errors = validatePurchaseData(
      selectedTickets.map(st => ({ 
        ticketTypeId: st.ticketTypeId, 
        quantity: st.quantity 
      })),
      buyerInfo
    );

    if (!agreeTerms) {
      errors.push("Vui lòng đồng ý với điều khoản và điều kiện");
    }

    if (errors.length > 0) {
      errors.forEach(error => toast.error(error));
      return;
    }

    try {
      await completePurchase(
        eventId,
        selectedTickets.map(st => ({ 
          ticketTypeId: st.ticketTypeId, 
          quantity: st.quantity 
        })),
        buyerInfo,
        paymentMethod,
        promoCode || undefined
      );
    } catch (error) {
      console.error("Purchase error:", error);
    }
  };

  if (eventLoading || ticketTypesLoading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
          <div className="space-y-6">
            <Skeleton className="h-8 w-64" />
            <div className="grid grid-cols-1 gap-8 lg:grid-cols-2">
              <div className="space-y-6">
                <Skeleton className="h-48 w-full" />
                <Skeleton className="h-32 w-full" />
              </div>
              <div className="space-y-6">
                <Skeleton className="h-64 w-full" />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-gray-900">Sự kiện không tồn tại</h1>
            <p className="mt-2 text-gray-600">Sự kiện bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
            <Link href="/" className="mt-4 inline-block text-blue-600 hover:text-blue-800">
              Quay về trang chủ
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <Link 
            href={`/events/${eventId}`}
            className="inline-flex items-center text-sm text-gray-600 hover:text-gray-900 mb-4"
          >
            <ArrowLeft className="h-4 w-4 mr-1" />
            Quay lại sự kiện
          </Link>
          
          <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
            <div className="flex items-start space-x-4">
              {(event.featuredImageUrl || (event.imageUrls && event.imageUrls.length > 0)) && (
                <img
                  src={sanitizeEventImageUrl(event.featuredImageUrl, event.imageUrls)}
                  alt={event.title}
                  className="h-20 w-20 rounded-lg object-cover"
                />
              )}
              <div className="flex-1">
                <h1 className="text-2xl font-bold text-gray-900 mb-2">{event.title}</h1>
                <div className="flex flex-wrap gap-4 text-sm text-gray-600">
                  <div className="flex items-center">
                    <Calendar className="h-4 w-4 mr-1" />
                    {new Date(event.startDate).toLocaleString("vi-VN", {
                      dateStyle: "full",
                      timeStyle: "short"
                    })}
                  </div>
                  <div className="flex items-center">
                    <MapPin className="h-4 w-4 mr-1" />
                    {event.city}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 gap-8 lg:grid-cols-2">
          {/* Left Column - Ticket Selection & Buyer Info */}
          <div className="space-y-6">
            {/* Ticket Selection */}
            <TicketSelector
              ticketTypes={ticketTypes}
              selectedTickets={selectedTickets}
              onAddTicket={handleAddTicket}
              onRemoveTicket={handleRemoveTicket}
              onTicketChange={handleTicketChange}
            />

            {/* Buyer Information */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Thông tin người mua</h3>
              
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Họ và tên *
                  </label>
                  <Input
                    value={buyerInfo.name}
                    onChange={(e) => setBuyerInfo(prev => ({ ...prev, name: e.target.value }))}
                    placeholder="Nhập họ và tên"
                    required
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Email *
                  </label>
                  <Input
                    type="email"
                    value={buyerInfo.email}
                    onChange={(e) => setBuyerInfo(prev => ({ ...prev, email: e.target.value }))}
                    placeholder="Nhập email"
                    required
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Số điện thoại
                  </label>
                  <Input
                    type="tel"
                    value={buyerInfo.phone}
                    onChange={(e) => setBuyerInfo(prev => ({ ...prev, phone: e.target.value }))}
                    placeholder="Nhập số điện thoại"
                  />
                </div>
              </div>
            </div>

            {/* Promo Code */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Mã giảm giá</h3>
              
              <div className="flex space-x-2">
                <Input
                  value={promoCode}
                  onChange={(e) => setPromoCode(e.target.value)}
                  placeholder="Nhập mã giảm giá"
                  className="flex-1"
                />
                <Button onClick={handleApplyPromo} variant="outline">
                  Áp dụng
                </Button>
              </div>
              
              {promoDiscount > 0 && (
                <div className="mt-2 text-sm text-green-600">
                  ✓ Giảm giá {promoDiscount}% đã được áp dụng
                </div>
              )}
            </div>
          </div>

          {/* Right Column - Payment & Summary */}
          <div className="space-y-6">
            {/* Payment Method */}
            <PaymentMethodSelector
              selectedMethod={paymentMethod}
              onMethodChange={setPaymentMethod}
            />

            {/* Purchase Summary */}
            <PurchaseSummary
              selectedTickets={selectedTickets}
              ticketTypes={ticketTypes}
              subtotal={subtotal}
              discount={discount}
              total={total}
              promoCode={promoCode}
              promoDiscount={promoDiscount}
            />

            {/* Terms and Purchase */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
              <div className="flex items-start space-x-3 mb-6">
                <input
                  type="checkbox"
                  id="terms"
                  checked={agreeTerms}
                  onChange={(e) => setAgreeTerms(e.target.checked)}
                  className="mt-1 h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                />
                <label htmlFor="terms" className="text-sm text-gray-700">
                  Tôi đồng ý với{" "}
                  <Link href="/terms" className="text-blue-600 hover:text-blue-800">
                    điều khoản và điều kiện
                  </Link>{" "}
                  và{" "}
                  <Link href="/privacy" className="text-blue-600 hover:text-blue-800">
                    chính sách bảo mật
                  </Link>
                </label>
              </div>

              <Button
                onClick={handlePurchase}
                disabled={!agreeTerms || selectedTickets.length === 0 || purchaseLoading}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3"
                size="lg"
              >
                {purchaseLoading ? (
                  <div className="flex items-center">
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    Đang xử lý...
                  </div>
                ) : (
                  <div className="flex items-center">
                    <CreditCard className="h-5 w-5 mr-2" />
                    Thanh toán {formatPriceVND(total)}
                  </div>
                )}
              </Button>

              <div className="flex items-center justify-center space-x-2 mt-4 text-xs text-gray-500">
                <Shield className="h-4 w-4" />
                <span>Thanh toán an toàn và bảo mật</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
