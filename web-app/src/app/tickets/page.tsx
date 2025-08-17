"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Ticket, Search, Filter, Calendar, MapPin, Clock, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { getTicketStatusConfig, TICKET_FILTER_TABS } from "@/lib/utils/ticket";
import { formatCurrency } from "@/lib/utils/currency";
import { formatDate, formatTime } from "@/lib/utils/date";
import { useTickets } from "@/hooks/useTickets";
import { useAuthHydration } from "@/hooks/useAuthHydration";
import { PageLoading, ErrorState } from "@/components/ui/LoadingSpinner";
import { CancelTicketModal } from "@/components/tickets/CancelTicketModal";

export default function MyTicketsPage() {
  const [activeTab, setActiveTab] = useState("all");
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const [cancelTicketModal, setCancelTicketModal] = useState<{
    isOpen: boolean;
    ticket: any;
  }>({ isOpen: false, ticket: null });
  const router = useRouter();

  const isHydrated = useAuthHydration()
  const currentStatus = TICKET_FILTER_TABS.find(tab => tab.key === activeTab)?.status;
  const { tickets, totalPages, isLoading, isError, error, refetch, cancelTicketMutation } = useTickets(currentStatus, currentPage);

  if (!isHydrated || isLoading) {
    return <PageLoading message="Đang tải danh sách vé..." />
  }

  if (isError) {
    return (
      <ErrorState
        title="Không thể tải danh sách vé"
        message={error?.message || "Đã xảy ra lỗi khi tải dữ liệu"}
        onRetry={() => refetch()}
      />
    )
  }

  const filteredTickets = tickets.filter((ticket: any) =>
    ticket.eventTitle?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    ticket.ticketNumber?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleCancelTicket = (ticket: any) => {
    setCancelTicketModal({ isOpen: true, ticket });
  };

  const handleConfirmCancel = async () => {
    if (cancelTicketModal.ticket) {
      try {
        await cancelTicketMutation.mutateAsync(cancelTicketModal.ticket.id);
        setCancelTicketModal({ isOpen: false, ticket: null });
      } catch (error) {
      }
    }
  };

  const canCancelTicket = (ticket: any) => {
    return ticket.status === 'PENDING' || ticket.status === 'PAID' || ticket.status === 'CONFIRMED';
  };

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
                className="bg-white rounded-xl border border-gray-200 p-6 hover:shadow-lg transition-all duration-200 hover:border-gray-300"
              >
                <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-6">
                  {/* Ticket Info */}
                  <div 
                    className="flex-1 cursor-pointer"
                    onClick={() => router.push(`/tickets/${ticket.id}`)}
                  >
                    {/* Header with Title and Status */}
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex-1">
                        <h3 className="text-xl font-bold text-gray-900 mb-2 leading-tight">
                          {ticket.eventTitle}
                        </h3>
                        <div className="flex items-center gap-4 text-sm text-gray-500">
                          <span className="flex items-center gap-2">
                            <Ticket className="w-4 h-4 text-blue-500" />
                            Mã vé: <span className="font-mono font-medium text-gray-700">{ticket.ticketNumber}</span>
                          </span>
                          <span className="text-gray-300">|</span>
                          <span className="font-medium text-green-600">{formatCurrency(ticket.price)}</span>
                        </div>
                      </div>
                      <span className={`px-4 py-2 rounded-full text-sm font-semibold ${statusConfig.color} flex-shrink-0`}>
                        {statusConfig.label}
                      </span>
                    </div>

                    {/* Event Details Grid */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                      <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                        <div className="p-2 bg-blue-100 rounded-lg">
                          <Calendar className="w-4 h-4 text-blue-600" />
                        </div>
                        <div>
                          <p className="text-xs text-gray-500 font-medium">Ngày</p>
                          <p className="text-sm text-gray-900 font-semibold">{formatDate(ticket.eventStartDate)}</p>
                        </div>
                      </div>
                      
                      <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                        <div className="p-2 bg-green-100 rounded-lg">
                          <Clock className="w-4 h-4 text-green-600" />
                        </div>
                        <div>
                          <p className="text-xs text-gray-500 font-medium">Giờ</p>
                          <p className="text-sm text-gray-900 font-semibold">{formatTime(ticket.eventStartDate)}</p>
                        </div>
                      </div>
                      
                      <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                        <div className="p-2 bg-purple-100 rounded-lg">
                          <MapPin className="w-4 h-4 text-purple-600" />
                        </div>
                        <div>
                          <p className="text-xs text-gray-500 font-medium">Địa điểm</p>
                          <p className="text-sm text-gray-900 font-semibold truncate">
                            {ticket.eventLocation || ticket.eventAddress || "Chưa cập nhật"}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Action Buttons */}
                  <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:flex-shrink-0">
                    {canCancelTicket(ticket) && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleCancelTicket(ticket);
                        }}
                        className="text-red-600 border-red-200 hover:bg-red-50 hover:border-red-300 hover:text-red-700 transition-colors"
                      >
                        <X className="w-4 h-4 mr-2" />
                        Hủy vé
                      </Button>
                    )}
                    <Button
                      variant="default"
                      size="sm"
                      onClick={() => router.push(`/tickets/${ticket.id}`)}
                      className="bg-blue-600 hover:bg-blue-700 text-white transition-colors"
                    >
                      Xem chi tiết
                    </Button>
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

      {/* Cancel Ticket Modal */}
      <CancelTicketModal
        isOpen={cancelTicketModal.isOpen}
        onClose={() => setCancelTicketModal({ isOpen: false, ticket: null })}
        onConfirm={handleConfirmCancel}
        ticket={cancelTicketModal.ticket}
        isCancelling={cancelTicketMutation.isPending}
      />
    </div>
  );
}
