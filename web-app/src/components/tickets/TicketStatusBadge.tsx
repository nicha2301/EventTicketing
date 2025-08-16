import { type GetMyTicketsStatus } from "@/lib/api/generated/client";
import { getTicketStatusConfig } from "@/lib/utils/ticket";

interface TicketStatusBadgeProps {
  status: GetMyTicketsStatus;
  size?: "sm" | "md" | "lg";
  showIcon?: boolean;
}

export function TicketStatusBadge({ 
  status, 
  size = "md",
  showIcon = true 
}: TicketStatusBadgeProps) {
  const config = getTicketStatusConfig(status);
  
  const sizeClasses = {
    sm: "text-xs px-2 py-0.5",
    md: "text-sm px-2.5 py-1", 
    lg: "text-base px-3 py-1.5"
  };
  
  const iconSizes = {
    sm: "text-xs",
    md: "text-sm",
    lg: "text-base"
  };
  
  return (
    <span 
      className={`
        inline-flex items-center gap-1 rounded-full border font-medium
        ${config.color} 
        ${sizeClasses[size]}
      `}
      title={config.description}
    >
      {config.label}
    </span>
  );
}
