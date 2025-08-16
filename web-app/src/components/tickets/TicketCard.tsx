"use client";

import { Calendar, MapPin } from "lucide-react";
import { useRouter } from "next/navigation";
import { EventImage } from "@/components/ui/event-image";
import { TicketStatusBadge } from "./TicketStatusBadge";
import { formatPriceVND } from "@/lib/utils";
import { formatTicketId } from "@/lib/utils/ticket";
import type { TicketDto } from "@/lib/api/generated/client";

interface TicketCardProps {
  ticket: TicketDto;
  onViewDetail?: (ticketId: string) => void;
}

export function TicketCard({ ticket }: TicketCardProps) {
  const router = useRouter();

  const handleCardClick = () => {
    router.push(`/tickets/${ticket.id}`);
  };
  
  const eventStartDate = ticket.eventStartDate 
    ? new Date(ticket.eventStartDate)
    : null;
    
  const formattedDate = eventStartDate
    ? eventStartDate.toLocaleString("vi-VN", { 
        dateStyle: "medium", 
        timeStyle: "short" 
      })
    : "Chưa có thời gian";
  
  return (
    <div 
      className="bg-white rounded-xl border border-gray-200 overflow-hidden hover:shadow-lg transition-all duration-200 cursor-pointer"
      onClick={handleCardClick}
    >
      {/* Header với event image */}
      <div className="flex gap-4 p-4">
        <div className="relative w-20 h-20 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0">
          <EventImage
            src={ticket.eventImageUrl || ""}
            alt={ticket.eventTitle || "Event"}
            fill
            className="object-cover"
          />
        </div>
        
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <div className="flex-1 min-w-0">
              <h3 className="font-semibold text-gray-900 truncate text-base">
                {ticket.eventTitle || "Sự kiện"}
              </h3>
              <p className="text-sm text-gray-500 mt-0.5">
                Mã vé: {formatTicketId(ticket.id || "")}
              </p>
            </div>
            
            <div className="flex items-center gap-2">
              <TicketStatusBadge status={ticket.status!} size="sm" />
            </div>
          </div>
          
          {/* Event details */}
          <div className="mt-3 space-y-1">
            <div className="flex items-center text-sm text-gray-600">
              <Calendar className="h-4 w-4 mr-2" />
              <span>{formattedDate}</span>
            </div>
            <div className="flex items-center text-sm text-gray-600">
              <MapPin className="h-4 w-4 mr-2" />
              <span className="truncate">{ticket.eventLocation || "Chưa có địa điểm"}</span>
            </div>
          </div>
        </div>
      </div>
      
      {/* Footer với thông tin vé */}
      <div className="px-4 py-3 bg-gray-50 border-t border-gray-100">
        <div className="flex items-center justify-between">
          <div>
            <span className="text-sm text-gray-600">Loại vé: </span>
            <span className="text-sm font-medium text-gray-900">
              {ticket.ticketTypeName || "Vé thường"}
            </span>
          </div>
          <div className="text-right">
            <div className="text-sm font-bold text-green-600">
              {formatPriceVND(ticket.price || 0)}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
