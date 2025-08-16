"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { formatPriceVND } from "@/lib/utils";
import { Plus, Minus, Trash2, Ticket } from "lucide-react";
import { useTicketAvailability } from "@/hooks/usePurchase";
import { TicketTypeDto } from "@/lib/api/generated/client";

interface SelectedTicket {
  ticketTypeId: string;
  quantity: number;
}

interface TicketSelectorProps {
  ticketTypes: TicketTypeDto[];
  selectedTickets: SelectedTicket[];
  onAddTicket: () => void;
  onRemoveTicket: (index: number) => void;
  onTicketChange: (index: number, ticketTypeId: string, quantity: number) => void;
}

function TicketRow({ 
  ticket, 
  index, 
  ticketTypes, 
  onRemove, 
  onChange 
}: {
  ticket: SelectedTicket;
  index: number;
  ticketTypes: TicketTypeDto[];
  onRemove: () => void;
  onChange: (ticketTypeId: string, quantity: number) => void;
}) {
  const selectedTicketType = ticketTypes.find(t => t.id === ticket.ticketTypeId);
  
  // Check availability for current selection
  const { data: availability } = useTicketAvailability(ticket.ticketTypeId, ticket.quantity);
  const isAvailable = availability !== false;
  
  const maxQuantity = Math.min(
    selectedTicketType?.maxTicketsPerCustomer || 10,
    selectedTicketType?.availableQuantity || 10
  );

  const handleQuantityChange = (newQuantity: number) => {
    const validQuantity = Math.max(1, Math.min(newQuantity, maxQuantity));
    onChange(ticket.ticketTypeId, validQuantity);
  };

  return (
    <div className="border border-gray-200 rounded-lg p-4 space-y-4">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center space-x-2">
            <Ticket className="h-4 w-4 text-blue-600" />
            <span className="font-medium text-gray-900">Vé #{index + 1}</span>
          </div>
        </div>
        
        <Button
          onClick={onRemove}
          variant="ghost"
          size="sm"
          className="text-red-600 hover:text-red-800 hover:bg-red-50"
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Ticket Type Selection */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Loại vé
          </label>
          <select
            value={ticket.ticketTypeId}
            onChange={(e) => onChange(e.target.value, ticket.quantity)}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500"
          >
            {ticketTypes
              .filter(t => t.isActive)
              .map((type) => (
              <option key={type.id} value={type.id}>
                {type.name} - {formatPriceVND(type.price)}
                {type.availableQuantity !== undefined && 
                  ` (Còn ${type.availableQuantity})`
                }
              </option>
            ))}
          </select>
        </div>

        {/* Quantity Selection */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Số lượng
          </label>
          <div className="flex items-center space-x-2">
            <Button
              onClick={() => handleQuantityChange(ticket.quantity - 1)}
              disabled={ticket.quantity <= 1}
              variant="outline"
              size="sm"
              className="p-2"
            >
              <Minus className="h-3 w-3" />
            </Button>
            
            <Input
              type="number"
              min={1}
              max={maxQuantity}
              value={ticket.quantity}
              onChange={(e) => handleQuantityChange(parseInt(e.target.value) || 1)}
              className="w-20 text-center"
            />
            
            <Button
              onClick={() => handleQuantityChange(ticket.quantity + 1)}
              disabled={ticket.quantity >= maxQuantity}
              variant="outline"
              size="sm"
              className="p-2"
            >
              <Plus className="h-3 w-3" />
            </Button>
          </div>
          
          {maxQuantity < 10 && (
            <p className="text-xs text-gray-500 mt-1">
              Tối đa {maxQuantity} vé
            </p>
          )}
        </div>
      </div>

      {/* Ticket Info */}
      {selectedTicketType && (
        <div className="border-t border-gray-100 pt-3 space-y-2">
          {selectedTicketType.description && (
            <p className="text-sm text-gray-600">
              {selectedTicketType.description}
            </p>
          )}
          
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-600">
              {formatPriceVND(selectedTicketType.price)} × {ticket.quantity}
            </span>
            <span className="font-semibold text-gray-900">
              {formatPriceVND(selectedTicketType.price * ticket.quantity)}
            </span>
          </div>
          
          {!isAvailable && (
            <div className="text-xs text-red-600 bg-red-50 px-2 py-1 rounded">
              ⚠️ Số lượng vé không đủ hoặc đã hết
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default function TicketSelector({
  ticketTypes,
  selectedTickets,
  onAddTicket,
  onRemoveTicket,
  onTicketChange,
}: TicketSelectorProps) {
  const activeTicketTypes = ticketTypes.filter(t => t.isActive);
  
  if (activeTicketTypes.length === 0) {
    return (
      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Chọn vé</h3>
        <div className="text-center py-8 text-gray-500">
          <Ticket className="h-12 w-12 mx-auto mb-3 text-gray-300" />
          <p>Hiện tại chưa có loại vé nào có sẵn</p>
        </div>
      </div>
    );
  }

  // Get quantities for each ticket type
  const getQuantityForTicketType = (ticketTypeId: string) => {
    const selectedTicket = selectedTickets.find(st => st.ticketTypeId === ticketTypeId);
    return selectedTicket?.quantity || 0;
  };

  // Handle quantity change for a ticket type
  const handleQuantityChange = (ticketTypeId: string, newQuantity: number) => {
    const existingIndex = selectedTickets.findIndex(st => st.ticketTypeId === ticketTypeId);
    
    if (newQuantity === 0) {
      // Remove ticket if quantity is 0
      if (existingIndex >= 0) {
        onRemoveTicket(existingIndex);
      }
    } else if (existingIndex >= 0) {
      // Update existing ticket
      onTicketChange(existingIndex, ticketTypeId, newQuantity);
    } else {
      // Add new ticket
      onAddTicket();
      // The new ticket will be added with the first ticket type, so we need to update it
      setTimeout(() => {
        const newIndex = selectedTickets.length;
        onTicketChange(newIndex, ticketTypeId, newQuantity);
      }, 0);
    }
  };

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Chọn vé</h3>

      {/* Direct Ticket Type Selection */}
      <div className="space-y-4">
        {activeTicketTypes.map((type) => {
          const currentQuantity = getQuantityForTicketType(type.id!);
          const maxQuantity = Math.min(
            type.maxTicketsPerCustomer || 10,
            type.availableQuantity || 10
          );
          
          return (
            <div key={type.id} className="border border-gray-200 rounded-lg p-4">
              <div className="flex items-start justify-between mb-3">
                <div className="flex-1">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-medium text-gray-900">{type.name}</h4>
                    <span className="text-lg font-semibold text-blue-600">
                      {formatPriceVND(type.price)}
                    </span>
                  </div>
                  
                  {type.description && (
                    <p className="text-sm text-gray-600 mb-2">{type.description}</p>
                  )}
                  
                  <div className="flex items-center space-x-4 text-xs text-gray-500">
                    {type.availableQuantity !== undefined && (
                      <span>Còn {type.availableQuantity} vé</span>
                    )}
                    {type.maxTicketsPerCustomer && (
                      <span>Tối đa {type.maxTicketsPerCustomer}/lần</span>
                    )}
                  </div>
                </div>
              </div>

              {/* Quantity Selector */}
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <Button
                    onClick={() => handleQuantityChange(type.id!, Math.max(0, currentQuantity - 1))}
                    disabled={currentQuantity <= 0}
                    variant="outline"
                    size="sm"
                    className="p-2"
                  >
                    <Minus className="h-3 w-3" />
                  </Button>
                  
                  <span className="w-12 text-center font-medium">
                    {currentQuantity}
                  </span>
                  
                  <Button
                    onClick={() => handleQuantityChange(type.id!, Math.min(maxQuantity, currentQuantity + 1))}
                    disabled={currentQuantity >= maxQuantity}
                    variant="outline"
                    size="sm"
                    className="p-2"
                  >
                    <Plus className="h-3 w-3" />
                  </Button>
                </div>

                {currentQuantity > 0 && (
                  <div className="text-right">
                    <div className="text-sm text-gray-600">
                      {formatPriceVND(type.price)} × {currentQuantity}
                    </div>
                    <div className="font-semibold text-gray-900">
                      {formatPriceVND(type.price * currentQuantity)}
                    </div>
                  </div>
                )}
              </div>

              {/* Availability Warning */}
              {type.availableQuantity !== undefined && currentQuantity > type.availableQuantity && (
                <div className="mt-2 text-xs text-red-600 bg-red-50 px-2 py-1 rounded">
                  ⚠️ Số lượng vé không đủ (còn {type.availableQuantity} vé)
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Summary */}
      {selectedTickets.length > 0 && (
        <div className="mt-6 border-t border-gray-100 pt-4">
          <div className="text-sm text-gray-600 mb-2">Đã chọn:</div>
          <div className="space-y-1">
            {selectedTickets.map((ticket, index) => {
              const ticketType = activeTicketTypes.find(t => t.id === ticket.ticketTypeId);
              return ticketType ? (
                <div key={index} className="flex justify-between text-sm">
                  <span>{ticketType.name} × {ticket.quantity}</span>
                  <span className="font-medium">
                    {formatPriceVND(ticketType.price * ticket.quantity)}
                  </span>
                </div>
              ) : null;
            })}
          </div>
        </div>
      )}
    </div>
  );
}
