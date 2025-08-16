"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { 
  Clock, 
  ArrowLeft, 
  CheckCircle2, 
  RefreshCw,
  Smartphone,
  AlertCircle
} from "lucide-react";
import { usePaymentStatus, useCheckPaymentStatus } from "@/hooks/usePaymentStatus";
import { toast } from "sonner";

export default function PaymentPendingPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [timeElapsed, setTimeElapsed] = useState(0);
  const [checkingManually, setCheckingManually] = useState(false);
  
  const orderId = searchParams.get("orderId");
  const paymentId = searchParams.get("paymentId");

  const [paymentInfo, setPaymentInfo] = useState<any>(null);
  const [ticketInfo, setTicketInfo] = useState<any>(null);
  
  useEffect(() => {
    const storedPayment = sessionStorage.getItem('pendingPaymentInfo');
    const storedTicket = sessionStorage.getItem('purchaseTicketInfo');
    
    if (storedPayment) {
      try {
        setPaymentInfo(JSON.parse(storedPayment));
      } catch (error) {
        console.error('Error parsing payment info:', error);
      }
    }
    
    if (storedTicket) {
      try {
        setTicketInfo(JSON.parse(storedTicket));
      } catch (error) {
        console.error('Error parsing ticket info:', error);
      }
    }
  }, []);

  const ticketId = ticketInfo?.ticketId || paymentId;

  const { data: ticketResult, error, refetch, isLoading } = usePaymentStatus(
    ticketId, 
    true 
  );
  
  const checkPaymentStatus = useCheckPaymentStatus();

  useEffect(() => {
    const interval = setInterval(() => {
      setTimeElapsed(prev => prev + 1);
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (ticketResult) {
      if (ticketResult.status === 'PAID') {
        sessionStorage.removeItem('pendingPaymentInfo');
        sessionStorage.removeItem('purchaseTicketInfo');
        const params = new URLSearchParams({
          orderId: orderId || ticketInfo?.orderId || '',
          resultCode: '0',
          amount: ticketResult.price?.toString() || '',
          paymentMethod: ticketResult.paymentStatus || 'MOMO',
          message: 'Thanh toán thành công'
        });
        router.push(`/purchase/result?${params.toString()}`);
      } else if (ticketResult.status === 'CANCELLED' || ticketResult.status === 'EXPIRED') {
        sessionStorage.removeItem('pendingPaymentInfo');
        sessionStorage.removeItem('purchaseTicketInfo');
        const params = new URLSearchParams({
          orderId: orderId || ticketInfo?.orderId || '',
          resultCode: '1',
          message: ticketResult.status === 'CANCELLED' ? 'Thanh toán đã bị hủy' : 'Thanh toán đã hết hạn'
        });
        router.push(`/purchase/result?${params.toString()}`);
      }
    }
  }, [ticketResult, router, orderId, ticketInfo]);

  // Timeout after 10 minutes
  useEffect(() => {
    if (timeElapsed >= 600) { // 10 minutes
      sessionStorage.removeItem('pendingPaymentInfo');
      sessionStorage.removeItem('purchaseTicketInfo');
      router.push('/purchase/result?resultCode=timeout&message=Hết thời gian chờ');
    }
  }, [timeElapsed, router]);

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handleCancel = () => {
    sessionStorage.removeItem('pendingPaymentInfo');
    sessionStorage.removeItem('purchaseTicketInfo');
    router.push('/events');
  };

  const handleCheckResult = async () => {
    if (!ticketId) {
      toast.error("Không tìm thấy thông tin vé");
      return;
    }

    setCheckingManually(true);
    try {
      const result = await checkPaymentStatus(ticketId);
      
      if (result) {
        if (result.status === 'PAID') {
          toast.success("Thanh toán thành công! Đang chuyển trang...");
        } else if (result.status === 'CANCELLED' || result.status === 'EXPIRED') {
          toast.error(`Thanh toán ${result.status === 'CANCELLED' ? 'đã bị hủy' : 'đã hết hạn'}!`);
        } else {
          toast.info(`Trạng thái hiện tại: ${result.status}. Vui lòng đợi thêm.`);
        }
      } else {
        toast.info("Chưa nhận được kết quả thanh toán. Vui lòng đợi thêm hoặc kiểm tra lại sau.");
      }
      
      refetch();
      
    } catch (error) {
      console.error("Error checking payment:", error);
      toast.error("Lỗi khi kiểm tra kết quả thanh toán từ API");
    } finally {
      setCheckingManually(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-xl shadow-lg p-8 text-center">
        {/* Header */}
        <div className="mb-8">
          <div className="w-20 h-20 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Clock className="w-10 h-10 text-blue-600 animate-pulse" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">
            Đang xử lý thanh toán
          </h1>
          <p className="text-gray-600">
            Vui lòng hoàn tất thanh toán trên MoMo và đợi kết quả
          </p>
        </div>

        {/* Payment Info */}
        {orderId && (
          <div className="bg-gray-50 rounded-lg p-4 mb-6">
            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-600">Mã đơn hàng:</span>
              <span className="font-mono font-medium">{orderId}</span>
            </div>
            {paymentId && (
              <div className="flex items-center justify-between text-sm mt-2">
                <span className="text-gray-600">Mã thanh toán:</span>
                <span className="font-mono font-medium">{paymentId}</span>
              </div>
            )}
          </div>
        )}

        {/* Progress Indicators */}
        <div className="space-y-4 mb-8">
          <div className="flex items-center text-sm">
            <CheckCircle2 className="w-5 h-5 text-green-500 mr-3" />
            <span className="text-gray-700">Yêu cầu thanh toán đã được tạo</span>
          </div>
          
          <div className="flex items-center text-sm">
            <CheckCircle2 className="w-5 h-5 text-green-500 mr-3" />
            <span className="text-gray-700">Đã chuyển hướng đến MoMo</span>
          </div>
          
          <div className="flex items-center text-sm">
            <div className="w-5 h-5 border-2 border-blue-500 border-t-transparent rounded-full animate-spin mr-3"></div>
            <span className="text-gray-700">Đang kiểm tra kết quả thanh toán...</span>
          </div>
        </div>

        {/* Time Elapsed */}
        <div className="bg-blue-50 rounded-lg p-4 mb-6">
          <div className="flex items-center justify-center text-blue-700">
            <Clock className="w-4 h-4 mr-2" />
            <span className="text-sm">
              Thời gian chờ: <span className="font-mono font-medium">{formatTime(timeElapsed)}</span>
            </span>
          </div>
          <div className="mt-2">
            <div className="w-full bg-blue-200 rounded-full h-2">
              <div 
                className="bg-blue-500 h-2 rounded-full transition-all duration-1000"
                style={{ width: `${Math.min((timeElapsed / 600) * 100, 100)}%` }}
              ></div>
            </div>
          </div>
        </div>

        {/* Instructions */}
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
          <div className="flex items-start">
            <Smartphone className="w-5 h-5 text-yellow-600 mr-3 mt-0.5 flex-shrink-0" />
            <div className="text-left">
              <p className="text-sm text-yellow-800 font-medium mb-1">
                Hướng dẫn thanh toán:
              </p>
              <ul className="text-xs text-yellow-700 space-y-1">
                <li>1. Hoàn tất thanh toán trên ứng dụng MoMo</li>
                <li>2. Quay lại trang này sau khi thanh toán</li>
                <li>3. Hệ thống sẽ tự động kiểm tra kết quả từ API</li>
                <li>4. <strong>Chỉ khi thanh toán thật sự thành công mới chuyển trang</strong></li>
              </ul>
            </div>
          </div>
        </div>
        
        {/* API Status */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 mb-6">
          <div className="flex items-center text-sm text-blue-800">
            <AlertCircle className="w-4 h-4 mr-2" />
            <span>
              {isLoading ? "Đang gọi API kiểm tra..." : 
               error ? "API kiểm tra gặp lỗi" : 
               ticketResult ? `Trạng thái vé: ${ticketResult.status}` : 
               "Chờ gọi API..."}
            </span>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="space-y-3">
          <Button 
            onClick={handleCheckResult}
            disabled={checkingManually || isLoading}
            className="w-full"
            variant="outline"
          >
            {checkingManually || isLoading ? (
              <div className="flex items-center">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mr-2"></div>
                Đang kiểm tra API...
              </div>
            ) : (
              <>
                <RefreshCw className="w-4 h-4 mr-2" />
                Kiểm tra kết quả ngay
              </>
            )}
          </Button>
          
          <Button 
            onClick={handleCancel}
            variant="ghost"
            className="w-full text-gray-600"
            disabled={checkingManually}
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Hủy và quay lại
          </Button>
        </div>

        {/* Help Link */}
        <div className="mt-6 pt-4 border-t border-gray-200">
          <p className="text-xs text-gray-500">
            Cần hỗ trợ? Liên hệ với chúng tôi
          </p>
        </div>
      </div>
    </div>
  );
}
