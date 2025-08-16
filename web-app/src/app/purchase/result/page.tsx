"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { formatPriceVND } from "@/lib/utils";
import { 
  CheckCircle, 
  XCircle, 
  Download, 
  ArrowLeft, 
  Mail, 
  Ticket,
  Clock,
  CreditCard,
  AlertCircle,
  Printer 
} from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";
import { getMyTickets, TicketDto } from "@/lib/api/generated/client";
import { generateTicketPDF, printTicket } from "@/lib/utils/pdf";

interface PaymentResult {
  success: boolean;
  orderId?: string;
  paymentId?: string;
  amount?: number;
  eventTitle?: string;
  ticketCount?: number;
  message?: string;
  tickets?: TicketDto[];
}

export default function PaymentResultPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isProcessing, setIsProcessing] = useState(true);
  const [result, setResult] = useState<PaymentResult | null>(null);

  // Extract payment parameters from URL
  const orderId = searchParams.get("orderId");
  const partnerCode = searchParams.get("partnerCode");
  const requestId = searchParams.get("requestId");
  const amount = searchParams.get("amount");
  const orderInfo = searchParams.get("orderInfo");
  const orderType = searchParams.get("orderType");
  const transId = searchParams.get("transId");
  const resultCode = searchParams.get("resultCode");
  const message = searchParams.get("message");
  const payType = searchParams.get("payType");
  const responseTime = searchParams.get("responseTime");
  const extraData = searchParams.get("extraData");
  const signature = searchParams.get("signature");

  // Get recent tickets by orderId to find the purchased tickets
  const { data: ticketsData, isLoading: ticketsLoading, error: ticketsError } = useQuery({
    queryKey: ["recent-tickets", orderId],
    queryFn: async () => {
      if (!orderId) return null;
      
      // Get recent tickets (last 10 minutes) to find tickets from this purchase
      const response = await getMyTickets({
        pageable: { page: 0, size: 50 }, // Get recent tickets
        status: resultCode === "0" ? "PAID" : "RESERVED"
      });
      
      return response.data?.data?.content || [];
    },
    enabled: !!orderId,
  });

  // Process payment result
  useEffect(() => {
    const processPaymentResult = async () => {
      try {
        setIsProcessing(true);

        // Check if we have the required parameters
        if (!orderId || !resultCode) {
          setResult({
            success: false,
            message: "Thông tin thanh toán không hợp lệ"
          });
          return;
        }

        // Wait for tickets data to load
        if (ticketsLoading) return;
        
        if (resultCode === "0") {
          // Payment successful - find tickets from this purchase
          const purchaseTickets = ticketsData?.filter(ticket => 
            // Find tickets purchased recently (within last 10 minutes)
            ticket.purchaseDate && 
            new Date().getTime() - new Date(ticket.purchaseDate).getTime() < 10 * 60 * 1000
          ) || [];

          setResult({
            success: true,
            orderId,
            paymentId: transId || undefined,
            amount: amount ? parseInt(amount) : (purchaseTickets[0]?.price || 0),
            eventTitle: purchaseTickets[0]?.eventTitle || "Sự kiện",
            ticketCount: purchaseTickets.length,
            message: "Thanh toán thành công",
            tickets: purchaseTickets
          });
          
          toast.success("Thanh toán thành công! Vé đã được gửi về email của bạn.");
        } else {
          // Payment failed
          setResult({
            success: false,
            orderId,
            message: message || "Thanh toán thất bại",
          });
          
          toast.error("Thanh toán thất bại. Vui lòng thử lại.");
        }
      } catch (error) {
        console.error("Error processing payment result:", error);
        setResult({
          success: false,
          message: "Có lỗi xảy ra khi xử lý kết quả thanh toán"
        });
      } finally {
        setIsProcessing(false);
      }
    };

    processPaymentResult();
  }, [orderId, resultCode, transId, amount, message, ticketsData, ticketsLoading]);

  const [isDownloading, setIsDownloading] = useState(false);

  const handleDownloadTickets = async () => {
    if (!result?.tickets || result.tickets.length === 0) {
      toast.error("Không có vé để tải xuống");
      return;
    }

    setIsDownloading(true);
    try {
      // Download PDF for each ticket
      for (const ticket of result.tickets) {
        await generateTicketPDF(ticket);
      }
      toast.success(`Đã tải xuống ${result.tickets.length} vé thành công!`);
    } catch (error) {
      console.error("Error downloading tickets:", error);
      toast.error("Lỗi khi tải xuống vé");
    } finally {
      setIsDownloading(false);
    }
  };

  const handlePrintTickets = () => {
    if (!result?.tickets || result.tickets.length === 0) {
      toast.error("Không có vé để in");
      return;
    }

    try {
      // Print each ticket
      result.tickets.forEach(ticket => {
        printTicket(ticket);
      });
      toast.success("Đã gửi lệnh in vé!");
    } catch (error) {
      console.error("Error printing tickets:", error);
      toast.error("Lỗi khi in vé");
    }
  };

  const handleRetryPayment = () => {
    router.back();
  };

  if (isProcessing) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="mx-auto max-w-2xl px-4 sm:px-6 lg:px-8">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8">
            <div className="text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
              <h2 className="text-xl font-semibold text-gray-900 mb-2">
                Đang xử lý thanh toán...
              </h2>
              <p className="text-gray-600">
                Vui lòng đợi trong giây lát, chúng tôi đang xác minh thanh toán của bạn.
              </p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!result) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="mx-auto max-w-2xl px-4 sm:px-6 lg:px-8">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 text-center">
            <AlertCircle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h2 className="text-xl font-semibold text-gray-900 mb-2">
              Không thể xử lý kết quả thanh toán
            </h2>
            <p className="text-gray-600 mb-6">
              Có lỗi xảy ra khi xử lý thông tin thanh toán. Vui lòng liên hệ hỗ trợ.
            </p>
            <Button onClick={() => router.push("/")} variant="outline">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Về trang chủ
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="mx-auto max-w-2xl px-4 sm:px-6 lg:px-8">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
          {/* Header */}
          <div className={`px-8 py-6 ${result.success ? "bg-green-50" : "bg-red-50"}`}>
            <div className="text-center">
              {result.success ? (
                <CheckCircle className="h-16 w-16 text-green-600 mx-auto mb-4" />
              ) : (
                <XCircle className="h-16 w-16 text-red-600 mx-auto mb-4" />
              )}
              
              <h1 className={`text-2xl font-bold mb-2 ${
                result.success ? "text-green-900" : "text-red-900"
              }`}>
                {result.success ? "Thanh toán thành công!" : "Thanh toán thất bại"}
              </h1>
              
              <p className={`${
                result.success ? "text-green-700" : "text-red-700"
              }`}>
                {result.message}
              </p>
            </div>
          </div>

          {/* Content */}
          <div className="px-8 py-6">
            {result.success ? (
              // Success Content
              <div className="space-y-6">
                {/* Order Summary */}
                <div className="border border-gray-200 rounded-lg p-4">
                  <h3 className="font-semibold text-gray-900 mb-3 flex items-center">
                    <CreditCard className="h-5 w-5 mr-2 text-blue-600" />
                    Thông tin đơn hàng
                  </h3>
                  
                  <div className="space-y-2 text-sm">
                    {result.orderId && (
                      <div className="flex justify-between">
                        <span className="text-gray-600">Mã đơn hàng:</span>
                        <span className="font-medium">{result.orderId}</span>
                      </div>
                    )}
                    
                    {result.paymentId && (
                      <div className="flex justify-between">
                        <span className="text-gray-600">Mã giao dịch:</span>
                        <span className="font-medium">{result.paymentId}</span>
                      </div>
                    )}
                    
                    {result.eventTitle && (
                      <div className="flex justify-between">
                        <span className="text-gray-600">Sự kiện:</span>
                        <span className="font-medium">{result.eventTitle}</span>
                      </div>
                    )}
                    
                    {result.ticketCount && (
                      <div className="flex justify-between">
                        <span className="text-gray-600">Số lượng vé:</span>
                        <span className="font-medium">{result.ticketCount} vé</span>
                      </div>
                    )}
                    
                    {result.amount && (
                      <div className="flex justify-between border-t border-gray-200 pt-2 mt-2">
                        <span className="text-gray-600">Tổng tiền:</span>
                        <span className="font-bold text-blue-600">
                          {formatPriceVND(result.amount)}
                        </span>
                      </div>
                    )}
                  </div>
                </div>

                {/* Email Notice */}
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <div className="flex items-start space-x-3">
                    <Mail className="h-5 w-5 text-blue-600 mt-0.5 flex-shrink-0" />
                    <div className="text-sm">
                      <p className="font-medium text-blue-900 mb-1">
                        Vé đã được gửi về email
                      </p>
                      <p className="text-blue-700">
                        Vé điện tử và mã QR check-in đã được gửi về địa chỉ email của bạn. 
                        Vui lòng kiểm tra cả hộp thư spam nếu không thấy email.
                      </p>
                    </div>
                  </div>
                </div>

                {/* Tickets */}
                {result.tickets && result.tickets.length > 0 && (
                  <div className="border border-gray-200 rounded-lg p-4">
                    <h3 className="font-semibold text-gray-900 mb-3 flex items-center">
                      <Ticket className="h-5 w-5 mr-2 text-green-600" />
                      Vé của bạn
                    </h3>
                    
                    <div className="space-y-2">
                      {result.tickets.map((ticket, index) => (
                        <div key={ticket.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                          <div>
                            <p className="font-medium text-gray-900">
                              {ticket.ticketTypeName}
                            </p>
                            <p className="text-sm text-gray-600">
                              Mã vé: {ticket.ticketNumber}
                            </p>
                          </div>
                          <div className="flex items-center space-x-2">
                            <div className="w-8 h-8 bg-white border border-gray-200 rounded flex items-center justify-center">
                              <div className="w-4 h-4 bg-black"></div>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Actions */}
                <div className="flex flex-col sm:flex-row gap-3">
                  <Button 
                    onClick={handleDownloadTickets}
                    disabled={isDownloading}
                    className="flex-1 bg-blue-600 hover:bg-blue-700"
                  >
                    {isDownloading ? (
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    ) : (
                      <Download className="h-4 w-4 mr-2" />
                    )}
                    {isDownloading ? "Đang tải..." : "Tải vé PDF"}
                  </Button>
                  
                  <Button 
                    onClick={handlePrintTickets}
                    variant="outline"
                    className="flex-1"
                  >
                    <Printer className="h-4 w-4 mr-2" />
                    In vé
                  </Button>
                  
                  <Button 
                    asChild
                    variant="outline"
                    className="flex-1"
                  >
                    <Link href="/tickets">
                      <Ticket className="h-4 w-4 mr-2" />
                      Xem tất cả vé
                    </Link>
                  </Button>
                </div>
              </div>
            ) : (
              // Failure Content
              <div className="space-y-6">
                {/* Error Details */}
                <div className="border border-red-200 rounded-lg p-4 bg-red-50">
                  <h3 className="font-semibold text-red-900 mb-2">
                    Chi tiết lỗi
                  </h3>
                  <p className="text-red-700 text-sm">
                    {result.message || "Có lỗi xảy ra trong quá trình thanh toán"}
                  </p>
                  
                  {result.orderId && (
                    <p className="text-red-600 text-xs mt-2">
                      Mã đơn hàng: {result.orderId}
                    </p>
                  )}
                </div>

                {/* Actions */}
                <div className="flex flex-col sm:flex-row gap-3">
                  <Button 
                    onClick={handleRetryPayment}
                    className="flex-1 bg-red-600 hover:bg-red-700"
                  >
                    <CreditCard className="h-4 w-4 mr-2" />
                    Thử lại thanh toán
                  </Button>
                  
                  <Button 
                    asChild
                    variant="outline"
                    className="flex-1"
                  >
                    <Link href="/">
                      <ArrowLeft className="h-4 w-4 mr-2" />
                      Về trang chủ
                    </Link>
                  </Button>
                </div>
              </div>
            )}

            {/* Support */}
            <div className="mt-8 pt-6 border-t border-gray-200 text-center">
              <p className="text-sm text-gray-600">
                Cần hỗ trợ?{" "}
                <Link href="/support" className="text-blue-600 hover:text-blue-800">
                  Liên hệ với chúng tôi
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
