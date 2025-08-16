"use client";

import { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { getTicketById, type TicketDto } from "@/lib/api/generated/client";
import { useAuthStore } from "@/store/auth";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { formatPriceVND } from "@/lib/utils";
import { 
  ArrowLeft,
  Calendar,
  MapPin,
  Clock,
  User,
  Ticket,
  Download,
  Mail,
  Printer,
  QrCode,
  CheckCircle,
  XCircle,
  AlertCircle,
  Share2,
  Star,
  CreditCard
} from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";
import { TicketDtoStatus } from "@/lib/api/generated/client";
import { QRCodeDisplay } from "@/components/tickets/QRCodeDisplay";
import { generateTicketPDF, printTicket } from "@/lib/utils/pdf";

function TicketStatusBadge({ status }: { status: TicketDtoStatus }) {
  const statusConfig = {
    [TicketDtoStatus.RESERVED]: {
      label: "Đã đặt",
      className: "bg-yellow-100 text-yellow-800 border-yellow-300",
      icon: Clock
    },
    [TicketDtoStatus.PAID]: {
      label: "Đã thanh toán",
      className: "bg-green-100 text-green-800 border-green-300", 
      icon: CheckCircle
    },
    [TicketDtoStatus.CHECKED_IN]: {
      label: "Đã check-in",
      className: "bg-blue-100 text-blue-800 border-blue-300",
      icon: CheckCircle
    },
    [TicketDtoStatus.CANCELLED]: {
      label: "Đã hủy",
      className: "bg-red-100 text-red-800 border-red-300",
      icon: XCircle
    },
    [TicketDtoStatus.EXPIRED]: {
      label: "Hết hạn",
      className: "bg-gray-100 text-gray-800 border-gray-300",
      icon: AlertCircle
    },
  };

  const config = statusConfig[status];
  const IconComponent = config.icon;

  return (
    <div className={`inline-flex items-center px-4 py-2 rounded-full text-sm font-medium border ${config.className}`}>
      <IconComponent className="w-4 h-4 mr-2" />
      {config.label}
    </div>
  );
}

function QRCodeSection({ ticket }: { ticket: TicketDto }) {
  if (ticket.status !== TicketDtoStatus.PAID && ticket.status !== TicketDtoStatus.CHECKED_IN) {
    return (
      <div className="bg-gray-50 rounded-2xl p-8 text-center">
        <QrCode className="h-16 w-16 text-gray-300 mx-auto mb-4" />
        <h3 className="text-lg font-semibold text-gray-900 mb-2">Mã QR chưa có sẵn</h3>
        <p className="text-gray-600">
          Mã QR sẽ xuất hiện sau khi vé được thanh toán thành công.
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white border-2 border-dashed border-blue-300 rounded-2xl p-8 text-center">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Mã QR Check-in</h3>
      
      {ticket.qrCodeUrl ? (
        <div className="space-y-4">
          <img
            src={ticket.qrCodeUrl}
            alt="QR Code"
            className="w-48 h-48 mx-auto border border-gray-200 rounded-lg"
          />
          <p className="text-sm text-gray-600">
            Quét mã QR này tại sự kiện để check-in
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="w-48 h-48 mx-auto bg-gray-100 border border-gray-200 rounded-lg flex items-center justify-center">
            <div className="text-center">
              <QrCode className="h-12 w-12 text-gray-400 mx-auto mb-2" />
              <p className="text-sm text-gray-500">Đang tạo mã QR...</p>
            </div>
          </div>
          <p className="text-sm text-gray-600">
            Mã QR sẽ sớm được tạo tự động
          </p>
        </div>
      )}
    </div>
  );
}

export default function TicketDetailPage() {
  const params = useParams();
  const router = useRouter();
  const ticketId = params.id as string;
  const { currentUser, isAuthenticated } = useAuthStore();

  const { data: ticket, isLoading, error } = useQuery({
    queryKey: ["ticket", ticketId],
    queryFn: async () => {
      const response = await getTicketById(ticketId);
      if (response.data?.success && response.data?.data) {
        return response.data.data;
      }
      throw new Error("Ticket not found");
    },
    enabled: !!ticketId && isAuthenticated,
  });

  const [isDownloading, setIsDownloading] = useState(false);

  const handleDownloadPDF = async () => {
    if (!ticket) {
      toast.error("Không tìm thấy thông tin vé");
      return;
    }

    setIsDownloading(true);
    try {
      await generateTicketPDF(ticket);
      toast.success("Tải PDF thành công!");
    } catch (error) {
      console.error("Error downloading PDF:", error);
      toast.error("Lỗi khi tải PDF");
    } finally {
      setIsDownloading(false);
    }
  };

  const handleSendEmail = () => {
    toast.info("Chức năng gửi email đang được phát triển");
  };

  const handlePrint = () => {
    if (!ticket) {
      toast.error("Không tìm thấy thông tin vé");
      return;
    }

    try {
      printTicket(ticket);
      toast.success("Đã gửi lệnh in vé!");
    } catch (error) {
      console.error("Error printing ticket:", error);
      toast.error("Lỗi khi in vé");
    }
  };

  const handleShare = () => {
    if (navigator.share) {
      navigator.share({
        title: `Vé sự kiện: ${ticket?.eventTitle}`,
        text: `Tôi sẽ tham dự sự kiện ${ticket?.eventTitle}`,
        url: window.location.href
      });
    } else {
      navigator.clipboard.writeText(window.location.href);
      toast.success("Đã copy link vé!");
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-gray-900">Vui lòng đăng nhập</h1>
            <p className="mt-2 text-gray-600">Bạn cần đăng nhập để xem chi tiết vé.</p>
            <Link href="/login" className="mt-4 inline-block text-blue-600 hover:text-blue-800">
              Đăng nhập ngay
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
          <div className="space-y-6">
            <Skeleton className="h-8 w-64" />
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div className="space-y-6">
                <Skeleton className="h-64 w-full" />
                <Skeleton className="h-48 w-full" />
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

  if (error || !ticket) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <AlertCircle className="h-12 w-12 text-red-400 mx-auto mb-4" />
            <h1 className="text-2xl font-bold text-gray-900">Không tìm thấy vé</h1>
            <p className="mt-2 text-gray-600">Vé bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
            <div className="mt-4 space-x-4">
              <Button onClick={() => router.back()} variant="outline">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Quay lại
              </Button>
              <Link href="/tickets">
                <Button>Xem tất cả vé</Button>
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const eventDate = new Date(ticket.eventStartDate);
  const eventEndDate = new Date(ticket.eventEndDate);
  const isEventCompleted = eventEndDate < new Date();
  const isEventStarted = eventDate < new Date();

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <Link 
            href="/tickets"
            className="inline-flex items-center text-sm text-gray-600 hover:text-gray-900 mb-4"
          >
            <ArrowLeft className="h-4 w-4 mr-1" />
            Quay lại danh sách vé
          </Link>
          
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Chi tiết vé</h1>
              <p className="text-gray-600">
                Thông tin chi tiết và mã QR check-in
              </p>
            </div>
            
            <Button onClick={handleShare} variant="outline">
              <Share2 className="h-4 w-4 mr-2" />
              Chia sẻ
            </Button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Left Column - Ticket Info */}
          <div className="space-y-6">
            {/* Event Info Card */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
              {ticket.eventImageUrl && (
                <img
                  src={ticket.eventImageUrl}
                  alt={ticket.eventTitle}
                  className="w-full h-48 object-cover"
                />
              )}
              
              <div className="p-6">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex-1">
                    <h2 className="text-xl font-bold text-gray-900 mb-2">
                      {ticket.eventTitle}
                    </h2>
                    <p className="text-lg font-semibold text-blue-600 mb-1">
                      {ticket.ticketTypeName}
                    </p>
                    <p className="text-2xl font-bold text-gray-900">
                      {formatPriceVND(ticket.price)}
                    </p>
                  </div>
                  
                  <TicketStatusBadge status={ticket.status} />
                </div>

                <div className="space-y-3 text-sm">
                  <div className="flex items-center text-gray-600">
                    <Calendar className="h-4 w-4 mr-3 flex-shrink-0" />
                    <div>
                      <div className="font-medium">
                        {eventDate.toLocaleDateString("vi-VN", {
                          weekday: "long",
                          year: "numeric",
                          month: "long",
                          day: "numeric"
                        })}
                      </div>
                      <div className="text-gray-500">
                        {eventDate.toLocaleTimeString("vi-VN", {
                          hour: "2-digit",
                          minute: "2-digit"
                        })} - {eventEndDate.toLocaleTimeString("vi-VN", {
                          hour: "2-digit",
                          minute: "2-digit"
                        })}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-start text-gray-600">
                    <MapPin className="h-4 w-4 mr-3 flex-shrink-0 mt-0.5" />
                    <div>
                      <div className="font-medium">{ticket.eventLocation}</div>
                      <div className="text-gray-500">{ticket.eventAddress}</div>
                    </div>
                  </div>

                  <div className="flex items-center text-gray-600">
                    <User className="h-4 w-4 mr-3 flex-shrink-0" />
                    <span>{ticket.userName}</span>
                  </div>
                </div>

                {/* Event Status */}
                {isEventCompleted ? (
                  <div className="mt-4 p-3 bg-gray-50 border border-gray-200 rounded-lg">
                    <p className="text-sm text-gray-600">
                      🎉 Sự kiện đã kết thúc. Hy vọng bạn đã có những trải nghiệm tuyệt vời!
                    </p>
                  </div>
                ) : isEventStarted ? (
                  <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                    <p className="text-sm text-blue-700">
                      🔥 Sự kiện đang diễn ra! Nhanh chóng check-in để tham gia.
                    </p>
                  </div>
                ) : (
                  <div className="mt-4 p-3 bg-green-50 border border-green-200 rounded-lg">
                    <p className="text-sm text-green-700">
                      ⏰ Sự kiện sắp diễn ra. Hãy chuẩn bị sẵn sàng!
                    </p>
                  </div>
                )}
              </div>
            </div>

            {/* Ticket Details */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                <Ticket className="h-5 w-5 mr-2 text-blue-600" />
                Thông tin vé
              </h3>
              
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="block text-gray-600 mb-1">Mã vé</span>
                  <span className="font-mono font-medium text-gray-900">
                    {ticket.ticketNumber || "Chưa có"}
                  </span>
                </div>
                
                {ticket.purchaseDate && (
                  <div>
                    <span className="block text-gray-600 mb-1">Ngày mua</span>
                    <span className="font-medium text-gray-900">
                      {new Date(ticket.purchaseDate).toLocaleDateString("vi-VN")}
                    </span>
                  </div>
                )}

                {ticket.paymentId && (
                  <div>
                    <span className="block text-gray-600 mb-1">Mã thanh toán</span>
                    <span className="font-mono font-medium text-gray-900">
                      {ticket.paymentId}
                    </span>
                  </div>
                )}

                {ticket.checkedInAt && (
                  <div>
                    <span className="block text-gray-600 mb-1">Thời gian check-in</span>
                    <span className="font-medium text-gray-900">
                      {new Date(ticket.checkedInAt).toLocaleString("vi-VN")}
                    </span>
                  </div>
                )}
              </div>
            </div>

            {/* Actions */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Hành động</h3>
              
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {ticket.status === TicketDtoStatus.PAID && (
                  <>
                    <Button 
                      onClick={handleDownloadPDF} 
                      disabled={isDownloading}
                      variant="outline" 
                      className="w-full"
                    >
                      {isDownloading ? (
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-600 mr-2"></div>
                      ) : (
                        <Download className="h-4 w-4 mr-2" />
                      )}
                      {isDownloading ? "Đang tải..." : "Tải PDF"}
                    </Button>
                    
                    <Button onClick={handleSendEmail} variant="outline" className="w-full">
                      <Mail className="h-4 w-4 mr-2" />
                      Gửi email
                    </Button>
                    
                    <Button onClick={handlePrint} variant="outline" className="w-full">
                      <Printer className="h-4 w-4 mr-2" />
                      In vé
                    </Button>
                  </>
                )}
                
                {ticket.status === TicketDtoStatus.RESERVED && (
                  <Button className="w-full bg-blue-600 hover:bg-blue-700 sm:col-span-2">
                    <CreditCard className="h-4 w-4 mr-2" />
                    Thanh toán ngay
                  </Button>
                )}

                {isEventCompleted && ticket.status === TicketDtoStatus.CHECKED_IN && (
                  <Button asChild variant="outline" className="w-full sm:col-span-2">
                    <Link href={`/events/${ticket.eventId}/rating`}>
                      <Star className="h-4 w-4 mr-2" />
                      Đánh giá sự kiện
                    </Link>
                  </Button>
                )}
              </div>
            </div>
          </div>

          {/* Right Column - QR Code */}
          <div className="space-y-6">
            {/* QR Code Section */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4 text-center">
                Mã QR Check-in
              </h3>
              
              {ticket.status === TicketDtoStatus.PAID || ticket.status === TicketDtoStatus.CHECKED_IN ? (
                <div className="flex flex-col items-center">
                  <QRCodeDisplay
                    ticketId={ticket.id || ''}
                    eventId={ticket.eventId}
                    userId={ticket.userId}
                    ticketNumber={ticket.ticketNumber}
                    size="lg"
                  />
                  <p className="text-sm text-gray-600 mt-4 text-center">
                    Xuất trình mã này tại điểm check-in
                  </p>
                </div>
              ) : (
                <div className="text-center py-8">
                  <QrCode className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                  <p className="text-gray-500">
                    Mã QR sẽ hiển thị sau khi thanh toán
                  </p>
                </div>
              )}
            </div>

            {/* Check-in Instructions */}
            {(ticket.status === TicketDtoStatus.PAID || ticket.status === TicketDtoStatus.CHECKED_IN) && (
              <div className="bg-blue-50 border border-blue-200 rounded-2xl p-6">
                <h3 className="text-lg font-semibold text-blue-900 mb-3">
                  Hướng dẫn check-in
                </h3>
                
                <ol className="space-y-2 text-sm text-blue-800 list-decimal list-inside">
                  <li>Đến điểm check-in tại sự kiện</li>
                  <li>Xuất trình mã QR trên điện thoại hoặc vé in</li>
                  <li>Nhân viên sẽ quét mã để xác nhận</li>
                  <li>Nhận vòng tay/thẻ tham dự (nếu có)</li>
                  <li>Tận hưởng sự kiện!</li>
                </ol>

                <div className="mt-4 p-3 bg-blue-100 rounded-lg">
                  <p className="text-xs text-blue-700">
                    💡 <strong>Lưu ý:</strong> Hãy đảm bảo màn hình điện thoại đủ sáng 
                    và mã QR không bị che khuất để quá trình quét diễn ra thuận lợi.
                  </p>
                </div>
              </div>
            )}

            {/* Support */}
            <div className="bg-gray-50 border border-gray-200 rounded-2xl p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">
                Cần hỗ trợ?
              </h3>
              
              <div className="space-y-3 text-sm text-gray-600">
                <p>
                  Nếu bạn gặp bất kỳ vấn đề nào với vé của mình, 
                  đừng ngần ngại liên hệ với chúng tôi.
                </p>
                
                <div className="flex flex-col space-y-2">
                  <Link 
                    href="/support" 
                    className="text-blue-600 hover:text-blue-800"
                  >
                    📧 Gửi yêu cầu hỗ trợ
                  </Link>
                  <a 
                    href="tel:+84123456789" 
                    className="text-blue-600 hover:text-blue-800"
                  >
                    📞 Hotline: 0123 456 789
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
