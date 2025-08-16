"use client";

import { formatPriceVND } from "@/lib/utils";
import { Ticket, Tag, Calculator } from "lucide-react";
import { TicketTypeDto } from "@/lib/api/generated/client";

interface SelectedTicket {
  ticketTypeId: string;
  quantity: number;
}

interface PurchaseSummaryProps {
  selectedTickets: SelectedTicket[];
  ticketTypes: TicketTypeDto[];
  subtotal: number;
  discount: number;
  total: number;
  promoCode?: string;
  promoDiscount: number;
}

export default function PurchaseSummary({
  selectedTickets,
  ticketTypes,
  subtotal,
  discount,
  total,
  promoCode,
  promoDiscount,
}: PurchaseSummaryProps) {
  const totalTickets = selectedTickets.reduce((sum, ticket) => sum + ticket.quantity, 0);

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
        <Calculator className="h-5 w-5 mr-2 text-blue-600" />
        Tóm tắt đơn hàng
      </h3>

      {selectedTickets.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <Ticket className="h-12 w-12 mx-auto mb-3 text-gray-300" />
          <p>Chưa có vé nào được chọn</p>
        </div>
      ) : (
        <div className="space-y-4">
          {/* Ticket Items */}
          <div className="space-y-3">
            {selectedTickets.map((ticket, index) => {
              const ticketType = ticketTypes.find(t => t.id === ticket.ticketTypeId);
              if (!ticketType) return null;

              const itemTotal = ticketType.price * ticket.quantity;

              return (
                <div key={index} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                  <div className="flex-1">
                    <div className="flex items-center space-x-2">
                      <Ticket className="h-4 w-4 text-blue-600" />
                      <span className="text-sm font-medium text-gray-900">
                        {ticketType.name}
                      </span>
                    </div>
                    <div className="text-xs text-gray-600 mt-1">
                      {formatPriceVND(ticketType.price)} × {ticket.quantity}
                    </div>
                  </div>
                  <div className="text-sm font-semibold text-gray-900">
                    {formatPriceVND(itemTotal)}
                  </div>
                </div>
              );
            })}
          </div>

          {/* Summary Calculations */}
          <div className="border-t border-gray-200 pt-4 space-y-2">
            {/* Subtotal */}
            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-600">
                Tạm tính ({totalTickets} vé)
              </span>
              <span className="font-medium text-gray-900">
                {formatPriceVND(subtotal)}
              </span>
            </div>

            {/* Promo Discount */}
            {promoDiscount > 0 && (
              <div className="flex items-center justify-between text-sm">
                <div className="flex items-center space-x-1 text-green-600">
                  <Tag className="h-3 w-3" />
                  <span>
                    Giảm giá {promoCode && `(${promoCode})`}
                  </span>
                </div>
                <span className="font-medium text-green-600">
                  -{formatPriceVND(discount)}
                </span>
              </div>
            )}

            {/* Total */}
            <div className="flex items-center justify-between pt-2 border-t border-gray-200">
              <span className="text-base font-semibold text-gray-900">
                Tổng cộng
              </span>
              <span className="text-lg font-bold text-blue-600">
                {formatPriceVND(total)}
              </span>
            </div>

            {/* Savings Display */}
            {discount > 0 && (
              <div className="text-xs text-center text-green-600 bg-green-50 py-2 px-3 rounded-lg">
                🎉 Bạn đã tiết kiệm được {formatPriceVND(discount)}!
              </div>
            )}
          </div>

          {/* Additional Info */}
          <div className="border-t border-gray-100 pt-4 space-y-2 text-xs text-gray-500">
            <div className="flex items-center justify-between">
              <span>Phí dịch vụ</span>
              <span>Miễn phí</span>
            </div>
            <div className="flex items-center justify-between">
              <span>Thuế VAT</span>
              <span>Đã bao gồm</span>
            </div>
          </div>

          {/* Terms Notice */}
          <div className="border-t border-gray-100 pt-4">
            <div className="text-xs text-gray-500 leading-relaxed">
              <p className="mb-1">
                • Vé đã mua không thể hoàn trả trừ khi sự kiện bị hủy
              </p>
              <p className="mb-1">
                • Vé sẽ được gửi qua email sau khi thanh toán thành công
              </p>
              <p>
                • Vui lòng mang theo vé (in ra hoặc trên điện thoại) khi tham dự sự kiện
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
