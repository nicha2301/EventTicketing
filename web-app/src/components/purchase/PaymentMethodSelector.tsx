"use client";

import { CreditCard, Smartphone, Banknote, Shield } from "lucide-react";

interface PaymentMethod {
  id: string;
  name: string;
  description: string;
  icon: React.ComponentType<any>;
  enabled: boolean;
  comingSoon?: boolean;
}

interface PaymentMethodSelectorProps {
  selectedMethod: string;
  onMethodChange: (method: string) => void;
}

const paymentMethods: PaymentMethod[] = [
  {
    id: "MOMO",
    name: "MoMo E-Wallet",
    description: "Thanh toán nhanh chóng và an toàn qua ví điện tử MoMo",
    icon: Smartphone,
    enabled: true,
  },
  {
    id: "VNPAY",
    name: "VNPay",
    description: "Thanh toán qua thẻ ATM, thẻ tín dụng, ví điện tử",
    icon: CreditCard,
    enabled: false,
    comingSoon: true,
  },
  {
    id: "ZALOPAY",
    name: "ZaloPay",
    description: "Thanh toán qua ví điện tử ZaloPay",
    icon: Smartphone,
    enabled: false,
    comingSoon: true,
  },
  {
    id: "BANK_TRANSFER",
    name: "Chuyển khoản ngân hàng",
    description: "Chuyển khoản trực tiếp qua ngân hàng",
    icon: Banknote,
    enabled: false,
    comingSoon: true,
  },
];

export default function PaymentMethodSelector({
  selectedMethod,
  onMethodChange,
}: PaymentMethodSelectorProps) {
  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
        <Shield className="h-5 w-5 mr-2 text-green-600" />
        Phương thức thanh toán
      </h3>

      <div className="space-y-3">
        {paymentMethods.map((method) => {
          const IconComponent = method.icon;
          const isSelected = selectedMethod === method.id;
          
          return (
            <div
              key={method.id}
              className={`
                relative border rounded-lg p-4 cursor-pointer transition-all duration-200
                ${isSelected
                  ? "border-blue-500 bg-blue-50 ring-2 ring-blue-500 ring-opacity-20"
                  : "border-gray-200 hover:border-gray-300"
                }
                ${!method.enabled ? "opacity-60 cursor-not-allowed bg-gray-50" : ""}
              `}
              onClick={() => method.enabled && onMethodChange(method.id)}
            >
              <div className="flex items-center space-x-3">
                {/* Radio Button */}
                <div className="flex-shrink-0">
                  <div
                    className={`
                      w-4 h-4 rounded-full border-2 flex items-center justify-center
                      ${isSelected && method.enabled
                        ? "border-blue-500 bg-blue-500"
                        : "border-gray-300"
                      }
                    `}
                  >
                    {isSelected && method.enabled && (
                      <div className="w-1.5 h-1.5 rounded-full bg-white"></div>
                    )}
                  </div>
                </div>

                {/* Icon */}
                <div className="flex-shrink-0">
                  <div
                    className={`
                      w-10 h-10 rounded-lg flex items-center justify-center
                      ${method.id === "MOMO" ? "bg-pink-100 text-pink-600" : ""}
                      ${method.id === "VNPAY" ? "bg-blue-100 text-blue-600" : ""}
                      ${method.id === "ZALOPAY" ? "bg-blue-100 text-blue-600" : ""}
                      ${method.id === "BANK_TRANSFER" ? "bg-green-100 text-green-600" : ""}
                    `}
                  >
                    <IconComponent className="h-5 w-5" />
                  </div>
                </div>

                {/* Content */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center space-x-2">
                    <h4 className="text-sm font-medium text-gray-900">
                      {method.name}
                    </h4>
                    {method.comingSoon && (
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800">
                        Sắp có
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-gray-600 mt-1">
                    {method.description}
                  </p>
                </div>

                {/* Status Indicator */}
                {isSelected && method.enabled && (
                  <div className="flex-shrink-0">
                    <div className="w-2 h-2 rounded-full bg-blue-500"></div>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Security Notice */}
      <div className="mt-4 p-3 bg-green-50 border border-green-200 rounded-lg">
        <div className="flex items-start space-x-2">
          <Shield className="h-4 w-4 text-green-600 mt-0.5 flex-shrink-0" />
          <div className="text-sm text-green-800">
            <p className="font-medium">Thanh toán an toàn</p>
            <p className="text-green-700">
              Tất cả giao dịch được mã hóa SSL 256-bit và tuân thủ các tiêu chuẩn bảo mật quốc tế.
            </p>
          </div>
        </div>
      </div>

      {/* MoMo Instructions */}
      {selectedMethod === "MOMO" && (
        <div className="mt-4 p-3 bg-pink-50 border border-pink-200 rounded-lg">
          <h5 className="text-sm font-medium text-pink-900 mb-2">
            Hướng dẫn thanh toán MoMo:
          </h5>
          <ol className="text-sm text-pink-800 space-y-1 list-decimal list-inside">
            <li>Bấm "Thanh toán" để được chuyển đến trang MoMo</li>
            <li>Đăng nhập tài khoản MoMo của bạn</li>
            <li>Xác nhận thông tin và hoàn tất thanh toán</li>
            <li>Vé sẽ được gửi về email sau khi thanh toán thành công</li>
          </ol>
        </div>
      )}
    </div>
  );
}
