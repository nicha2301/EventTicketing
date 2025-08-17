'use client'

import { useState } from 'react'
import { AlertTriangle, X, Ticket, CreditCard, AlertCircle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { formatCurrency } from '@/lib/utils/currency'
import { formatDate, formatTime } from '@/lib/utils/date'

interface CancelTicketModalProps {
  isOpen: boolean
  onClose: () => void
  onConfirm: () => void
  ticket: any
  isCancelling: boolean
}

export function CancelTicketModal({ 
  isOpen, 
  onClose, 
  onConfirm, 
  ticket, 
  isCancelling 
}: CancelTicketModalProps) {
  const [isConfirmed, setIsConfirmed] = useState(false)

  const handleConfirm = () => {
    if (isConfirmed) {
      onConfirm()
    }
  }

  const handleClose = () => {
    setIsConfirmed(false)
    onClose()
  }

  const isPaidTicket = ticket?.status === 'PAID' || ticket?.status === 'CONFIRMED'
  const hasRefundPolicy = ticket?.event?.refundPolicy || false

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="max-w-md bg-white border-2 border-gray-200 shadow-xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-red-600">
            <AlertTriangle className="w-5 h-5" />
            Xác nhận hủy vé
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          {/* Ticket Info */}
          <div className="bg-gray-50 rounded-lg p-4">
            <div className="flex items-center gap-3 mb-3">
              <Ticket className="w-5 h-5 text-blue-600" />
              <h4 className="font-semibold text-gray-900">{ticket?.eventTitle}</h4>
            </div>
            <div className="space-y-2 text-sm text-gray-600">
              <div>Mã vé: {ticket?.ticketNumber}</div>
              <div>Ngày: {formatDate(ticket?.eventStartDate)}</div>
              <div>Giờ: {formatTime(ticket?.eventStartDate)}</div>
              <div className="flex items-center gap-2">
                <CreditCard className="w-4 h-4" />
                <span className="font-medium text-gray-900">
                  {formatCurrency(ticket?.price)}
                </span>
              </div>
            </div>
          </div>

          {/* Warning Messages */}
          <div className="space-y-3">
            {isPaidTicket && (
              <div className="bg-red-50 border-2 border-red-200 rounded-lg p-3">
                <div className="flex items-start gap-2">
                  <AlertCircle className="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0" />
                  <div className="text-sm">
                    <p className="font-medium text-red-800 mb-1">
                      ⚠️ Cảnh báo: Vé đã thanh toán
                    </p>
                    <p className="text-red-700">
                      {hasRefundPolicy 
                        ? 'Vé này có thể được hoàn tiền theo chính sách của sự kiện.'
                        : 'Vé này KHÔNG được hoàn tiền. Bạn sẽ mất toàn bộ số tiền đã thanh toán.'
                      }
                    </p>
                  </div>
                </div>
              </div>
            )}

            <div className="bg-yellow-50 border-2 border-yellow-200 rounded-lg p-3">
              <div className="flex items-start gap-2">
                <AlertTriangle className="w-5 h-5 text-yellow-600 mt-0.5 flex-shrink-0" />
                <div className="text-sm">
                  <p className="font-medium text-yellow-800 mb-1">
                    Hành động không thể hoàn tác
                  </p>
                  <p className="text-yellow-700">
                    Sau khi hủy, vé sẽ không thể khôi phục lại.
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Confirmation Checkbox */}
          <div className="flex items-start gap-3">
            <input
              type="checkbox"
              id="confirm-cancel"
              checked={isConfirmed}
              onChange={(e) => setIsConfirmed(e.target.checked)}
              className="mt-1 w-4 h-4 text-red-600 border-gray-300 rounded focus:ring-red-500"
            />
            <label htmlFor="confirm-cancel" className="text-sm text-gray-700">
              Tôi hiểu rõ rằng việc hủy vé này là không thể hoàn tác
              {isPaidTicket && !hasRefundPolicy && ' và sẽ không được hoàn tiền'}
            </label>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex gap-3 pt-4">
          <Button
            variant="outline"
            onClick={handleClose}
            disabled={isCancelling}
            className="flex-1"
          >
            <X className="w-4 h-4 mr-2" />
            Hủy bỏ
          </Button>
          <Button
            variant="destructive"
            onClick={handleConfirm}
            disabled={!isConfirmed || isCancelling}
            className="flex-1"
          >
            {isCancelling ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                Đang hủy...
              </>
            ) : (
              <>
                <AlertTriangle className="w-4 h-4 mr-2" />
                Xác nhận hủy
              </>
            )}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  )
}
