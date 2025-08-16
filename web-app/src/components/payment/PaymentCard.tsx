"use client";

import { PaymentResponseDto, PaymentResponseDtoStatus } from "@/lib/api/generated/client";
import EventImage from "@/components/EventImage";

interface PaymentHistoryItem extends PaymentResponseDto {
  event?: {
    imageUrl?: string;
    startTime: string;
  };
}

interface PaymentCardProps {
  payment: PaymentHistoryItem;
}

function PaymentStatusBadge({ status }: { status: PaymentResponseDtoStatus }) {
  const getStatusConfig = (status: PaymentResponseDtoStatus) => {
    switch (status) {
      case PaymentResponseDtoStatus.COMPLETED:
        return {
          text: "Thành công",
          className: "bg-green-100 text-green-800 border-green-200"
        };
      case PaymentResponseDtoStatus.PENDING:
        return {
          text: "Đang xử lý",
          className: "bg-yellow-100 text-yellow-800 border-yellow-200"
        };
      case PaymentResponseDtoStatus.FAILED:
        return {
          text: "Thất bại",
          className: "bg-red-100 text-red-800 border-red-200"
        };
      case PaymentResponseDtoStatus.CANCELLED:
        return {
          text: "Đã hủy",
          className: "bg-gray-100 text-gray-800 border-gray-200"
        };
      default:
        return {
          text: "Không xác định",
          className: "bg-gray-100 text-gray-800 border-gray-200"
        };
    }
  };

  const config = getStatusConfig(status);

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${config.className}`}>
      {config.text}
    </span>
  );
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount);
}

function formatDateTime(dateString: string): string {
  return new Intl.DateTimeFormat('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(dateString));
}

export function PaymentCard({ payment }: PaymentCardProps) {
  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4 hover:shadow-md transition-shadow">
      <div className="flex items-center space-x-3">
        {/* Event Image - Smaller */}
        <div className="flex-shrink-0">
          <EventImage
            src={payment.event?.imageUrl}
            alt={payment.eventTitle}
            className="w-12 h-12 rounded-lg object-cover"
          />
        </div>

        {/* Payment Info - Compact */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between">
            <div className="flex-1 min-w-0">
              <h3 className="text-base font-semibold text-gray-900 truncate">
                {payment.eventTitle}
              </h3>
              <div className="flex items-center space-x-3 text-sm text-gray-600 mt-1">
                <span>{payment.ticketTypeName}</span>
                <span>•</span>
                <span>{payment.paymentMethod}</span>
                <span>•</span>
                <span>{formatDateTime(payment.createdAt)}</span>
              </div>
            </div>
            
            <div className="flex items-center space-x-4">
              <PaymentStatusBadge status={payment.status} />
              <div className="text-right">
                <p className="text-base font-semibold text-gray-900">
                  {formatCurrency(payment.amount)}
                </p>
                {payment.refundedAmount && payment.refundedAmount > 0 && (
                  <p className="text-xs text-red-600">
                    Hoàn: {formatCurrency(payment.refundedAmount)}
                  </p>
                )}
              </div>
            </div>
          </div>

          {/* Action Buttons - Inline */}
          {(payment.status === PaymentResponseDtoStatus.COMPLETED || 
            payment.status === PaymentResponseDtoStatus.FAILED) && (
            <div className="mt-3 flex justify-end space-x-3">
              {payment.status === PaymentResponseDtoStatus.COMPLETED && (
                <button className="text-blue-600 hover:text-blue-800 text-sm font-medium">
                  Xem vé
                </button>
              )}
              {payment.status === PaymentResponseDtoStatus.FAILED && payment.paymentUrl && (
                <button className="text-blue-600 hover:text-blue-800 text-sm font-medium">
                  Thử lại
                </button>
              )}
              <button className="text-gray-600 hover:text-gray-800 text-sm font-medium">
                Chi tiết
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
