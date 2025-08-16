"use client";

import { PaymentResponseDtoStatus } from "@/lib/api/generated/client";

interface PaymentFilterProps {
  selectedStatus: string;
  onStatusChange: (status: string) => void;
}

export function PaymentFilter({ selectedStatus, onStatusChange }: PaymentFilterProps) {
  const statusOptions = [
    { value: "", label: "Tất cả trạng thái" },
    { value: PaymentResponseDtoStatus.COMPLETED, label: "Thành công" },
    { value: PaymentResponseDtoStatus.PENDING, label: "Đang xử lý" },
    { value: PaymentResponseDtoStatus.FAILED, label: "Thất bại" },
    { value: PaymentResponseDtoStatus.CANCELLED, label: "Đã hủy" },
  ];

  return (
    <div className="flex items-center space-x-2">
      <label htmlFor="status-filter" className="text-sm font-medium text-gray-700 whitespace-nowrap">
        Trạng thái:
      </label>
      <select
        id="status-filter"
        value={selectedStatus}
        onChange={(e) => onStatusChange(e.target.value)}
        className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white text-sm min-w-[160px]"
      >
        {statusOptions.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </div>
  );
}
