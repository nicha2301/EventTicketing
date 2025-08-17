"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Ticket, Search, Filter, Calendar, MapPin, Clock } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { getUserTickets } from "@/lib/api/modules/tickets";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { getTicketStatusConfig, TICKET_FILTER_TABS } from "@/lib/utils/ticket";
import { formatCurrency } from "@/lib/utils/currency";
import { formatDate, formatTime } from "@/lib/utils/date";

export default function MyTicketsPage() {
  const [activeTab, setActiveTab] = useState("all");
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const router = useRouter();

  const currentStatus = TICKET_FILTER_TABS.find(tab => tab.key === activeTab)?.status;

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["my-tickets", currentStatus, currentPage],
    queryFn: ({ signal }) => getUserTickets(currentStatus, currentPage, 10, signal),
    retry: 2,
  });

  const tickets = data?.data?.content || [];
  const totalPages = data?.data?.totalPages || 0;

  const filteredTickets = tickets.filter((ticket: any) =>
    ticket.eventTitle?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    ticket.ticketNumber?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
            <p className="text-gray-600">Đang tải danh sách vé...</p>
          </div>
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <div className="text-red-500 mb-4">
              <Ticket className="w-16 h-16 mx-auto mb-2" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              Không thể tải danh sách vé
            </h3>
            <p className="text-gray-600 mb-4">
              {error?.message || "Đã xảy ra lỗi khi tải dữ liệu"}
            </p>
            <Button onClick={() => refetch()}>
              Thử lại
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Vé của tôi</h1>
        <p className="text-gray-600">Quản lý và theo dõi tất cả vé sự kiện của bạn</p>
      </div>

      {/* Search and Filters */}
      <div className="mb-6 space-y-4">
        {/* Search Bar */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <Input
            type="text"
            placeholder="Tìm kiếm theo tên sự kiện hoặc mã vé..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10 pr-4 py-2 w-full"
          />
        </div>

        {/* Filter Tabs */}
        <div className="flex flex-wrap gap-2">
          {TICKET_FILTER_TABS.map((tab) => (
            <Button
              key={tab.key}
              variant={activeTab === tab.key ? "default" : "outline"}
              size="sm"
              onClick={() => {
                setActiveTab(tab.key);
                setCurrentPage(0);
                refetch();
              }}
              className="text-sm"
            >
              <Filter className="w-4 h-4 mr-2" />
              {tab.label}
            </Button>
          ))}
        </div>
      </div>

      {/* Tickets List */}
      {filteredTickets.length === 0 ? (
        <div className="text-center py-12">
          <Ticket className="w-16 h-16 mx-auto text-gray-400 mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">
            {searchQuery ? "Không tìm thấy vé" : "Chưa có vé nào"}
          </h3>
          <p className="text-gray-600 mb-6">
            {searchQuery 
              ? "Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc"
              : "Vé đã mua sẽ xuất hiện ở đây"
            }
          </p>
          {!searchQuery && (
            <Button onClick={() => window.location.href = "/search"}>
              Khám phá sự kiện
            </Button>
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {filteredTickets.map((ticket: any) => {
            const statusConfig = getTicketStatusConfig(ticket.status);
            return (
              <div
                key={ticket.id}
                className="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-lg transition-shadow cursor-pointer"
                onClick={() => router.push(`/tickets/${ticket.id}`)}
              >
                <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                  {/* Ticket Info */}
                  <div className="flex-1">
                    <div className="flex items-start justify-between mb-3">
                      <h3 className="text-lg font-semibold text-gray-900 mb-1">
                        {ticket.eventTitle}
                      </h3>
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${statusConfig.color}`}>
                        {statusConfig.label}
                      </span>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-sm text-gray-600">
                      <div className="flex items-center">
                        <Calendar className="w-4 h-4 mr-2" />
                        <span>{formatDate(ticket.eventStartDate)}</span>
                      </div>
                      <div className="flex items-center">
                        <Clock className="w-4 h-4 mr-2" />
                        <span>{formatTime(ticket.eventStartDate)}</span>
                      </div>
                      <div className="flex items-center">
                        <MapPin className="w-4 h-4 mr-2" />
                        <span>{ticket.eventLocation || ticket.eventAddress}</span>
                      </div>
                    </div>
                    <div className="mt-3 pt-3 border-t border-gray-100">
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-gray-500">Mã vé: {ticket.ticketNumber}</span>
                        <span className="font-semibold text-gray-900">
                          {formatCurrency(ticket.price)}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center mt-8">
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={currentPage === 0}
              onClick={() => {
                const newPage = Math.max(0, currentPage - 1);
                setCurrentPage(newPage);
                refetch();
              }}
            >
              Trước
            </Button>
            
            <span className="flex items-center px-4 py-2 text-sm text-gray-600">
              Trang {currentPage + 1} / {totalPages}
            </span>
            
            <Button
              variant="outline"
              size="sm"
              disabled={currentPage >= totalPages - 1}
              onClick={() => {
                const newPage = currentPage + 1;
                setCurrentPage(newPage);
                refetch();
              }}
            >
              Sau
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
