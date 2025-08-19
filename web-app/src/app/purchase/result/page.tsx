"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
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
import { generateTicketPDF, printTicket } from "@/lib/utils/pdf";
import { usePurchaseInfo } from "@/hooks/usePurchase";

interface PaymentResult {
  success: boolean;
  orderId?: string;
  paymentId?: string;
  amount?: number;
  eventTitle?: string;
  ticketCount?: number;
  message?: string;
  tickets?: any[];
  eventId?: string;
  buyerInfo?: any;
  paymentMethod?: string;
  purchaseTime?: string;
  paymentTime?: string;
}

function ResultContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isProcessing, setIsProcessing] = useState(true);
  const [result, setResult] = useState<PaymentResult | null>(null);
  const { successfulPurchaseInfo, clearPurchaseInfo } = usePurchaseInfo();

  const orderId = searchParams.get("orderId");
  const resultCode = searchParams.get("resultCode");
  const message = searchParams.get("message");
  const amount = searchParams.get("amount");
  const paymentMethod = searchParams.get("paymentMethod");
  const eventTitle = searchParams.get("eventTitle");
  const ticketCount = searchParams.get("ticketCount");
  const purchaseTime = searchParams.get("purchaseTime");

  useEffect(() => {
    const processPaymentResult = async () => {
      try {
        setIsProcessing(true);

        if (!orderId || !resultCode) {
          setResult({
            success: false,
            message: "Thông tin thanh toán không hợp lệ"
          });
          return;
        }

        if (resultCode === "0") {
          let ticketData = successfulPurchaseInfo;
          
          if (!ticketData) {
            const stored = sessionStorage.getItem('successfulPurchaseInfo');
            if (stored) {
              try {
                ticketData = JSON.parse(stored);
              } catch (error) {
                console.error('Error parsing successful purchase info:', error);
              }
            }
          }

          const tickets = ticketData?.tickets?.map((ticket: any) => ({
            id: ticket.id,
            ticketNumber: ticket.ticketNumber,
            ticketTypeName: ticket.ticketTypeName,
            price: ticket.price,
            status: 'PAID',
            eventTitle: ticketData.eventTitle,
            eventId: ticketData.eventId,
            eventStartDate: ticketData.purchaseTime,
            eventLocation: ticketData.eventLocation || 'Địa điểm sự kiện',
            userId: ticketData.buyerInfo?.id || 'current-user-id', 
            eventEndDate: ticketData.eventEndDate || ticketData.purchaseTime,
            eventAddress: ticketData.eventAddress || 'Địa chỉ sự kiện',
            eventDescription: ticketData.eventDescription || 'Mô tả sự kiện',
            buyerName: ticketData.buyerInfo?.name || 'Người mua',
            buyerEmail: ticketData.buyerInfo?.email || 'email@example.com',
            buyerPhone: ticketData.buyerInfo?.phone || 'Số điện thoại',
            paymentMethod: ticketData.paymentMethod || 'MOMO',
            paymentTime: ticketData.paymentTime || new Date().toISOString(),
            orderId: ticketData.orderId
          })) || [];

          setResult({
            success: true,
            orderId,
            paymentId: ticketData?.paymentId,
            amount: amount ? parseInt(amount) : ticketData?.totalAmount || 0,
            eventTitle: eventTitle || ticketData?.eventTitle || "Sự kiện",
            ticketCount: ticketCount ? parseInt(ticketCount) : ticketData?.ticketCount || 0,
            message: "Thanh toán thành công",
            tickets,
            eventId: ticketData?.eventId,
            buyerInfo: ticketData?.buyerInfo,
            paymentMethod: paymentMethod || ticketData?.paymentMethod,
            purchaseTime: purchaseTime || ticketData?.purchaseTime,
            paymentTime: ticketData?.paymentTime
          });
          
          toast.success("Thanh toán thành công! Vé đã được gửi về email của bạn.");
        } else {
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
  }, [orderId, resultCode, message, amount, paymentMethod, eventTitle, ticketCount, purchaseTime, successfulPurchaseInfo]);

  const [isDownloading, setIsDownloading] = useState(false);

  const handleDownloadTickets = async () => {
    if (!result?.tickets || result.tickets.length === 0) {
      toast.error("Không có vé để tải xuống");
      return;
    }

    setIsDownloading(true);
    try {
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

  const handleViewAllTickets = () => {
    clearPurchaseInfo();
    router.push("/tickets");
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
                        <div key={ticket.id || index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                          <div>
                            <p className="font-medium text-gray-900">
                              {ticket.ticketTypeName || 'Vé tham dự'}
                            </p>
                            <p className="text-sm text-gray-600">
                              Mã vé: {ticket.ticketNumber || ticket.id || 'N/A'}
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
                    onClick={handleViewAllTickets}
                    variant="outline"
                    className="flex-1"
                  >
                    <Ticket className="h-4 w-4 mr-2" />
                    Xem tất cả vé
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

export default function PaymentResultPage() {
  return (
    <Suspense fallback={<div className="min-h-screen flex items-center justify-center">Đang tải...</div>}>
      <ResultContent />
    </Suspense>
  );
}
