"use client";

import { useState } from "react";
import { usePaymentHistory } from "@/hooks/usePaymentHistory";
import { useAuthHydration } from "@/hooks/useAuthHydration";
import { PaymentCard } from "@/components/payment/PaymentCard";
import { PaymentFilter } from "@/components/payment/PaymentFilter";
import { PaymentResponseDto, PaymentResponseDtoStatus } from "@/lib/api/generated/client";
import { PageLoading, ErrorState } from "@/components/ui/LoadingSpinner";

export default function PaymentHistoryPage() {
  type PaymentListItem = PaymentResponseDto & { event?: { imageUrl?: string; startTime: string } };
  const [statusFilter, setStatusFilter] = useState<string>("");
  const [currentPage, setCurrentPage] = useState(0);
  const isHydrated = useAuthHydration();

  const { data, isLoading, error } = usePaymentHistory(
    (statusFilter as PaymentResponseDtoStatus) || undefined,
    currentPage,
    20
  );

  if (!isHydrated) {
    return <PageLoading message="Đang khởi tạo..." />;
  }

  const payments = data?.content || [];

  if (isLoading) {
    return <PageLoading message="Đang tải lịch sử thanh toán..." />;
  }

  if (error) {
    return (
      <ErrorState
        title="Không thể tải lịch sử thanh toán"
        message="Vui lòng thử lại sau hoặc kiểm tra kết nối mạng."
        onRetry={() => window.location.reload()}
      />
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      <div className="mb-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Lịch sử thanh toán</h1>
            <p className="text-sm text-gray-600 mt-1">Theo dõi tất cả giao dịch mua vé của bạn</p>
          </div>

          <div className="flex items-center space-x-4">
            <PaymentFilter
              selectedStatus={statusFilter}
              onStatusChange={setStatusFilter}
            />
          </div>
        </div>
      </div>

      {payments.length === 0 ? (
        <div className="text-center py-12">
          <div className="w-24 h-24 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
            <svg className="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            </svg>
          </div>
          <h3 className="text-xl font-semibold text-gray-900 mb-2">
            {statusFilter ? "Không tìm thấy giao dịch" : "Chưa có giao dịch nào"}
          </h3>
          <p className="text-gray-600">
            {statusFilter 
              ? "Thử thay đổi bộ lọc để xem giao dịch khác" 
              : "Giao dịch thanh toán sẽ hiển thị ở đây sau khi bạn mua vé"
            }
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {(payments as PaymentListItem[]).map((payment) => (
            <PaymentCard key={payment.id} payment={payment} />
          ))}
        </div>
      )}

      {data && data.totalPages > 1 && (
        <div className="mt-8 flex justify-center">
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0}
              className="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Trước
            </button>

            <span className="px-3 py-2 text-sm text-gray-700">
              Trang {currentPage + 1} / {data.totalPages}
            </span>

            <button
              onClick={() => setCurrentPage(Math.min(data.totalPages - 1, currentPage + 1))}
              disabled={currentPage === data.totalPages - 1}
              className="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Sau
            </button>
          </div>
        </div>
      )}
    </div>
  );
}


