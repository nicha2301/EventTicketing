import { GetMyTicketsStatus } from "@/lib/api/generated/client";

// Ticket status configurations với colors và labels
export const TICKET_STATUS_CONFIG = {
  [GetMyTicketsStatus.RESERVED]: {
    label: "Chờ thanh toán",
    color: "bg-yellow-100 text-yellow-800 border-yellow-200",
    description: "Vé đã được đặt, cần thanh toán để hoàn tất"
  },
  [GetMyTicketsStatus.PAID]: {
    label: "Đã thanh toán",
    color: "bg-green-100 text-green-800 border-green-200", 
    description: "Vé đã được thanh toán thành công"
  },
  [GetMyTicketsStatus.CHECKED_IN]: {
    label: "Đã check-in",
    color: "bg-blue-100 text-blue-800 border-blue-200",
    description: "Đã sử dụng vé tại sự kiện"
  },
  [GetMyTicketsStatus.CANCELLED]: {
    label: "Đã hủy",
    color: "bg-red-100 text-red-800 border-red-200",
    description: "Vé đã được hủy"
  },
  [GetMyTicketsStatus.EXPIRED]: {
    label: "Hết hạn", 
    color: "bg-gray-100 text-gray-800 border-gray-200",
    description: "Vé đã hết hạn sử dụng"
  }
} as const;

export const TICKET_FILTER_TABS = [
  { key: "all", label: "Tất cả", status: undefined },
  { key: "pending", label: "Chờ thanh toán", status: GetMyTicketsStatus.RESERVED },
  { key: "paid", label: "Đã thanh toán", status: GetMyTicketsStatus.PAID },
  { key: "used", label: "Đã sử dụng", status: GetMyTicketsStatus.CHECKED_IN },
  { key: "cancelled", label: "Đã hủy", status: GetMyTicketsStatus.CANCELLED },
  { key: "expired", label: "Hết hạn", status: GetMyTicketsStatus.EXPIRED }
] as const;

export function getTicketStatusConfig(status: GetMyTicketsStatus) {
  return TICKET_STATUS_CONFIG[status] || TICKET_STATUS_CONFIG[GetMyTicketsStatus.RESERVED];
}

export function canCancelTicket(status: GetMyTicketsStatus): boolean {
  return status === GetMyTicketsStatus.RESERVED || status === GetMyTicketsStatus.PAID;
}

export function canDownloadTicket(status: GetMyTicketsStatus): boolean {
  return status === GetMyTicketsStatus.PAID || status === GetMyTicketsStatus.CHECKED_IN;
}

export function formatTicketId(ticketId: string): string {
  if (ticketId.includes('-') && ticketId.length > 10) {
    const parts = ticketId.split('-');
    if (parts.length >= 2) {
      const id = parts[1];
      return `${parts[0]}-${id.slice(0, 3)}-${id.slice(3, 6)}-${id.slice(6)}`;
    }
  }
  return ticketId;
}
