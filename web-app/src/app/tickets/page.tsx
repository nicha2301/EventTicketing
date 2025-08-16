"use client";

import { useState } from "react";
import { Ticket, Search, Filter } from "lucide-react";
import { useMyTickets } from "@/hooks/useMyTickets";
import { useAuthHydration } from "@/hooks/useAuthHydration";
import { TicketCard } from "@/components/tickets/TicketCard";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { PageLoading, ErrorState, EmptyState } from "@/components/ui/LoadingSpinner";
import { TICKET_FILTER_TABS } from "@/lib/utils/ticket";
import type { GetMyTicketsStatus } from "@/lib/api/generated/client";

export default function MyTicketsPage() {
  const [activeTab, setActiveTab] = useState("all");
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedTicket, setSelectedTicket] = useState<string | null>(null);
  
  const isHydrated = useAuthHydration();
  const currentStatus = TICKET_FILTER_TABS.find(tab => tab.key === activeTab)?.status;
  const { data, isLoading, isError, refetch } = useMyTickets(currentStatus, 0, 20);
  
  // Show loading while hydrating
  if (!isHydrated) {
    return <PageLoading message="Đang khởi tạo..." />;
  }
  
  const tickets = data?.data?.data?.content || [];
  
  // Filter tickets by search query
  const filteredTickets = tickets.filter((ticket: any) =>
    ticket.eventTitle?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    ticket.ticketNumber?.toLowerCase().includes(searchQuery.toLowerCase())
  );
  
  const handleViewDetail = (ticketId: string) => {
    setSelectedTicket(ticketId);
    // TODO: Implement ticket detail modal
    console.log("View ticket detail:", ticketId);
  };

  // Early return for auth not hydrated yet
  if (!isHydrated) {
    return <PageLoading message="Đang khởi tạo..." />;
  }

  // Early return for loading state
  if (isLoading) {
    return <PageLoading message="Đang tải danh sách vé..." />;
  }

  // Early return for error state  
  if (isError) {
    return (
      <ErrorState
        title="Không thể tải danh sách vé"
        message="Vui lòng thử lại sau hoặc kiểm tra kết nối mạng."
        onRetry={() => refetch()}
      />
    );
  }
  
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container-page py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-blue-100 rounded-lg">
              <Ticket className="h-6 w-6 text-blue-600" />
            </div>
            <h1 className="text-3xl font-bold text-gray-900">Vé của tôi</h1>
          </div>
          <p className="text-gray-600">Quản lý tất cả vé sự kiện của bạn</p>
        </div>
        
        {/* Search and Filter */}
        <div className="bg-white rounded-xl border border-gray-200 p-6 mb-6">
          <div className="flex flex-col sm:flex-row gap-4 mb-6">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <Input
                placeholder="Tìm vé theo tên sự kiện hoặc mã vé..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
              />
            </div>
            <Button variant="outline" className="sm:w-auto">
              <Filter className="h-4 w-4 mr-2" />
              Lọc nâng cao
            </Button>
          </div>
          
          {/* Status Tabs */}
          <div className="flex flex-wrap gap-2">
            {TICKET_FILTER_TABS.map((tab) => (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key)}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  activeTab === tab.key
                    ? "bg-blue-100 text-blue-700 border border-blue-200"
                    : "text-gray-600 hover:text-gray-900 hover:bg-gray-100"
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>
        
        {/* Tickets List */}
        {filteredTickets.length === 0 ? (
          <EmptyState
            title={searchQuery ? "Không tìm thấy vé" : "Chưa có vé nào"}
            description={
              searchQuery 
                ? "Thử tìm kiếm với từ khóa khác hoặc xóa bộ lọc"
                : "Khi bạn mua vé sự kiện, chúng sẽ hiển thị ở đây"
            }
            icon={<Ticket className="h-12 w-12 text-gray-400" />}
            action={
              !searchQuery ? (
                <Button onClick={() => window.location.href = "/"}>
                  Khám phá sự kiện
                </Button>
              ) : undefined
            }
          />
        ) : (
          <div className="space-y-4">
            {filteredTickets.map((ticket: any) => (
              <TicketCard
                key={ticket.id}
                ticket={ticket}
                onViewDetail={handleViewDetail}
              />
            ))}
          </div>
        )}
        
        {/* TODO: Add pagination if needed */}
        {filteredTickets.length > 0 && (
          <div className="mt-8 text-center">
            <p className="text-sm text-gray-500">
              Hiển thị {filteredTickets.length} vé
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
