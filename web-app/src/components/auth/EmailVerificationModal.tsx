"use client";

import Modal from "@/components/ui/modal";
import { Button } from "@/components/ui/button";
import { useResendActivationEmail } from "@/hooks/useAuth";
import { Mail, CheckCircle, Clock } from "lucide-react";

interface EmailVerificationModalProps {
  isOpen: boolean;
  onClose: () => void;
  email: string;
}

export default function EmailVerificationModal({ 
  isOpen, 
  onClose, 
  email
}: EmailVerificationModalProps) {
  const resendEmailMutation = useResendActivationEmail();

  const handleResendEmail = () => {
    resendEmailMutation.mutate(email);
  };
  return (
    <Modal isOpen={isOpen} onClose={onClose} className="max-w-lg">
      <div className="text-center">
        {/* Success Icon */}
        <div className="mx-auto w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mb-6">
          <CheckCircle className="w-8 h-8 text-green-600" />
        </div>

        {/* Title */}
        <h2 className="text-2xl font-bold text-gray-900 mb-3">
          Đăng ký thành công!
        </h2>

        {/* Description */}
        <p className="text-gray-600 mb-6 leading-relaxed">
          Chúng tôi đã gửi email xác nhận đến địa chỉ:
        </p>

        {/* Email Display */}
        <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 mb-6 flex items-center gap-3">
          <Mail className="w-5 h-5 text-blue-600 flex-shrink-0" />
          <span className="font-medium text-blue-900 break-all">{email}</span>
        </div>

        {/* Instructions */}
        <div className="text-left bg-gray-50 rounded-xl p-4 mb-6">
          <div className="flex items-start gap-3">
            <Clock className="w-5 h-5 text-gray-500 mt-0.5 flex-shrink-0" />
            <div className="text-sm text-gray-700">
              <p className="font-medium mb-2">Vui lòng làm theo các bước sau:</p>
              <ol className="list-decimal list-inside space-y-1 text-gray-600">
                <li>Kiểm tra hộp thư đến của bạn</li>
                <li>Tìm email từ EventTicketing</li>
                <li>Nhấp vào liên kết xác nhận trong email</li>
                <li>Hoàn tất kích hoạt tài khoản</li>
              </ol>
            </div>
          </div>
        </div>

        {/* Note */}
        <div className="text-sm text-gray-500 mb-6">
          <p>Không thấy email? Kiểm tra thư mục spam hoặc thư rác.</p>
        </div>

        {/* Actions */}
        <div className="flex flex-col gap-3">
          <Button
            variant="outline"
            onClick={handleResendEmail}
            disabled={resendEmailMutation.isPending}
            className="w-full h-12"
          >
            {resendEmailMutation.isPending ? "Đang gửi..." : "Gửi lại email xác nhận"}
          </Button>
          
          <Button
            onClick={onClose}
            className="w-full h-12 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800"
          >
            Tôi đã hiểu
          </Button>
        </div>

        {/* Footer */}
        <p className="text-xs text-gray-400 mt-4">
          Email xác nhận có hiệu lực trong 24 giờ
        </p>
      </div>
    </Modal>
  );
}
