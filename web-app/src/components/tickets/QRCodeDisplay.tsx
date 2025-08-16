"use client";

import { useState, useEffect, useRef } from "react";
import { Download, Maximize2, X, Copy, Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { generateTicketQRCode, generateQRCodeCanvas } from "@/lib/utils/qrcode";
import LoadingSpinner from "@/components/ui/LoadingSpinner";

interface QRCodeDisplayProps {
  ticketId: string;
  eventId: string;
  userId: string;
  ticketNumber?: string;
  eventTitle?: string;
  size?: "sm" | "md" | "lg" | "xl";
  showActions?: boolean;
  className?: string;
}

const QR_SIZES = {
  sm: 120,
  md: 200,
  lg: 300,
  xl: 400,
};

export function QRCodeDisplay({
  ticketId,
  eventId,
  userId,
  ticketNumber,
  eventTitle,
  size = "md",
  showActions = true,
  className = "",
}: QRCodeDisplayProps) {
  const [qrCodeUrl, setQrCodeUrl] = useState<string>("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>("");
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [copied, setCopied] = useState(false);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  const qrSize = QR_SIZES[size];

  useEffect(() => {
    generateQRCode();
  }, [ticketId, eventId, userId, size]);

  const generateQRCode = async () => {
    try {
      setIsLoading(true);
      setError("");
      
      const dataUrl = await generateTicketQRCode(ticketId, eventId, userId, {
        width: qrSize,
        margin: 2,
        errorCorrectionLevel: 'M',
      });
      
      setQrCodeUrl(dataUrl);
    } catch (err) {
      setError("Không thể tạo mã QR");
      console.error("QR Code generation error:", err);
    } finally {
      setIsLoading(false);
    }
  };

  const downloadQRCode = () => {
    if (!qrCodeUrl) return;
    
    const link = document.createElement('a');
    link.download = `ticket-qr-${ticketNumber || ticketId}.png`;
    link.href = qrCodeUrl;
    link.click();
  };

  const copyQRData = async () => {
    const qrData = `TICKET:${ticketId}:${eventId}:${userId}`;
    
    try {
      await navigator.clipboard.writeText(qrData);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error("Failed to copy QR data:", err);
    }
  };

  const openFullscreen = () => {
    setIsFullscreen(true);
  };

  if (isLoading) {
    return (
      <div className={`flex items-center justify-center ${className}`} style={{ width: qrSize, height: qrSize }}>
        <LoadingSpinner size="md" />
      </div>
    );
  }

  if (error) {
    return (
      <div className={`flex flex-col items-center justify-center border-2 border-red-200 rounded-lg ${className}`} style={{ width: qrSize, height: qrSize }}>
        <div className="text-red-500 text-sm text-center p-4">
          <p>{error}</p>
          <Button 
            variant="outline" 
            size="sm" 
            onClick={generateQRCode}
            className="mt-2"
          >
            Thử lại
          </Button>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className={`relative group ${className}`}>
        <div className="border rounded-lg p-4 bg-white">
          {qrCodeUrl && (
            <img 
              src={qrCodeUrl} 
              alt={`QR Code cho vé ${ticketNumber || ticketId}`}
              width={qrSize}
              height={qrSize}
              className="mx-auto"
            />
          )}
          
          {showActions && (
            <div className="flex justify-center gap-2 mt-3">
              <Button
                variant="outline"
                size="sm"
                onClick={downloadQRCode}
                className="h-8"
              >
                <Download className="h-3 w-3 mr-1" />
                Tải về
              </Button>
              
              <Button
                variant="outline"
                size="sm"
                onClick={openFullscreen}
                className="h-8"
              >
                <Maximize2 className="h-3 w-3 mr-1" />
                Toàn màn hình
              </Button>
              
              <Button
                variant="outline"
                size="sm"
                onClick={copyQRData}
                className="h-8"
              >
                {copied ? (
                  <Check className="h-3 w-3 mr-1 text-green-600" />
                ) : (
                  <Copy className="h-3 w-3 mr-1" />
                )}
                {copied ? "Đã sao chép" : "Sao chép"}
              </Button>
            </div>
          )}
        </div>
      </div>

      {/* Fullscreen Modal */}
      {isFullscreen && (
        <div className="fixed inset-0 bg-black bg-opacity-90 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold">Mã QR Check-in</h3>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setIsFullscreen(false)}
              >
                <X className="h-4 w-4" />
              </Button>
            </div>
            
            <div className="text-center">
              {eventTitle && (
                <p className="text-sm text-gray-600 mb-2">{eventTitle}</p>
              )}
              {ticketNumber && (
                <p className="text-xs text-gray-500 mb-4">Vé: {ticketNumber}</p>
              )}
              
              {qrCodeUrl && (
                <img 
                  src={qrCodeUrl} 
                  alt="QR Code"
                  width={300}
                  height={300}
                  className="mx-auto mb-4"
                />
              )}
              
              <p className="text-xs text-gray-500 mb-4">
                Xuất trình mã QR này để check-in tại sự kiện
              </p>
              
              <div className="flex justify-center gap-2">
                <Button
                  variant="outline"
                  onClick={downloadQRCode}
                  className="flex-1"
                >
                  <Download className="h-4 w-4 mr-2" />
                  Tải về
                </Button>
                
                <Button
                  variant="outline"
                  onClick={copyQRData}
                  className="flex-1"
                >
                  {copied ? (
                    <Check className="h-4 w-4 mr-2 text-green-600" />
                  ) : (
                    <Copy className="h-4 w-4 mr-2" />
                  )}
                  {copied ? "Đã sao chép" : "Sao chép link"}
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
