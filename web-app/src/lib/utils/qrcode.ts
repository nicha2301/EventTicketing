import QRCode from 'qrcode';

export interface QRCodeOptions {
  width?: number;
  margin?: number;
  color?: {
    dark?: string;
    light?: string;
  };
  errorCorrectionLevel?: 'L' | 'M' | 'Q' | 'H';
}

/**
 * Generate QR code data URL for ticket
 */
export async function generateTicketQRCode(
  ticketId: string,
  eventId: string,
  userId: string,
  options: QRCodeOptions = {}
): Promise<string> {
  try {
    const qrData = `TICKET:${ticketId}:${eventId}:${userId}`;
    
    const qrOptions = {
      width: options.width || 300,
      margin: options.margin || 2,
      color: {
        dark: options.color?.dark || '#000000',
        light: options.color?.light || '#FFFFFF',
      },
      errorCorrectionLevel: options.errorCorrectionLevel || 'M' as const,
    };

    const qrCodeDataURL = await QRCode.toDataURL(qrData, qrOptions);
    return qrCodeDataURL;
  } catch (error) {
    console.error('Error generating QR code:', error);
    throw new Error('Failed to generate QR code');
  }
}

/**
 * Generate QR code as Canvas for React components
 */
export async function generateQRCodeCanvas(
  ticketId: string,
  eventId: string,
  userId: string,
  canvasElement: HTMLCanvasElement,
  options: QRCodeOptions = {}
): Promise<void> {
  try {
    const qrData = `TICKET:${ticketId}:${eventId}:${userId}`;
    
    const qrOptions = {
      width: options.width || 300,
      margin: options.margin || 2,
      color: {
        dark: options.color?.dark || '#000000',
        light: options.color?.light || '#FFFFFF',
      },
      errorCorrectionLevel: options.errorCorrectionLevel || 'M' as const,
    };

    await QRCode.toCanvas(canvasElement, qrData, qrOptions);
  } catch (error) {
    console.error('Error generating QR code canvas:', error);
    throw new Error('Failed to generate QR code canvas');
  }
}

/**
 * Parse ticket QR code data
 */
export function parseTicketQRCode(data: string): {
  ticketId: string;
  eventId: string;
  userId: string;
} | null {
  try {
    if (!data.startsWith('TICKET:')) {
      return null;
    }
    
    const parts = data.split(':');
    if (parts.length !== 4) {
      return null;
    }
    
    const [, ticketId, eventId, userId] = parts;
    
    if (!ticketId || !eventId || !userId) {
      return null;
    }
    
    return { ticketId, eventId, userId };
  } catch {
    return null;
  }
}

/**
 * Validate QR code data format
 */
export function isValidTicketQRCode(data: string): boolean {
  return parseTicketQRCode(data) !== null;
}
