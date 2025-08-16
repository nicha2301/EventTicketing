"use client";

import { Search, Database, Wifi } from "lucide-react";

interface EmptySearchStateProps {
  type: 'no-results' | 'api-unavailable' | 'initial';
  searchQuery?: string;
}

export function EmptySearchState({ type, searchQuery }: EmptySearchStateProps) {
  if (type === 'api-unavailable') {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <div className="mb-4 rounded-full bg-red-100 p-3">
          <Wifi className="h-8 w-8 text-red-600" />
        </div>
        <h3 className="mb-2 text-xl font-semibold text-gray-900">
          Không thể kết nối API
        </h3>
        <p className="mb-4 max-w-md text-gray-600">
          Hiện tại không thể kết nối đến máy chủ. Vui lòng kiểm tra:
        </p>
        <ul className="mb-6 text-left text-sm text-gray-600">
          <li>• Backend server đã khởi động tại localhost:8080</li>
          <li>• Kết nối mạng ổn định</li>
          <li>• API endpoints hoạt động bình thường</li>
        </ul>
        <p className="text-sm text-gray-500">
          Tự động thử lại khi API khả dụng
        </p>
      </div>
    );
  }

  if (type === 'no-results') {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <div className="mb-4 rounded-full bg-gray-100 p-3">
          <Search className="h-8 w-8 text-gray-400" />
        </div>
        <h3 className="mb-2 text-xl font-semibold text-gray-900">
          Không tìm thấy sự kiện
        </h3>
        <p className="mb-4 max-w-md text-gray-600">
          {searchQuery 
            ? `Không có sự kiện nào phù hợp với "${searchQuery}"` 
            : 'Không có sự kiện nào phù hợp với bộ lọc hiện tại'
          }
        </p>
        <p className="text-sm text-gray-500">
          Thử điều chỉnh từ khóa tìm kiếm hoặc bộ lọc
        </p>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="mb-4 rounded-full bg-blue-100 p-3">
        <Database className="h-8 w-8 text-blue-600" />
      </div>
      <h3 className="mb-2 text-xl font-semibold text-gray-900">
        Tìm kiếm sự kiện
      </h3>
      <p className="text-gray-600">
        Nhập từ khóa hoặc chọn bộ lọc để bắt đầu tìm kiếm
      </p>
    </div>
  );
}
