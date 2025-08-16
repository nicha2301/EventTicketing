import jsPDF from 'jspdf';
import { generateTicketQRCode } from './qrcode';
import type { TicketDto } from '@/lib/api/generated/client';

export interface TicketPDFOptions {
  format?: 'a4' | 'letter';
  orientation?: 'portrait' | 'landscape';
  margin?: number;
}

/**
 * Convert Vietnamese characters to Latin equivalents for PDF compatibility
 */
function convertVietnameseText(text: string): string {
  if (!text) return '';
  
  const vietnameseMap: { [key: string]: string } = {
    'à': 'a', 'á': 'a', 'ạ': 'a', 'ả': 'a', 'ã': 'a', 'â': 'a', 'ầ': 'a', 'ấ': 'a', 'ậ': 'a', 'ẩ': 'a', 'ẫ': 'a', 'ă': 'a', 'ằ': 'a', 'ắ': 'a', 'ặ': 'a', 'ẳ': 'a', 'ẵ': 'a',
    'è': 'e', 'é': 'e', 'ẹ': 'e', 'ẻ': 'e', 'ẽ': 'e', 'ê': 'e', 'ề': 'e', 'ế': 'e', 'ệ': 'e', 'ể': 'e', 'ễ': 'e',
    'ì': 'i', 'í': 'i', 'ị': 'i', 'ỉ': 'i', 'ĩ': 'i',
    'ò': 'o', 'ó': 'o', 'ọ': 'o', 'ỏ': 'o', 'õ': 'o', 'ô': 'o', 'ồ': 'o', 'ố': 'o', 'ộ': 'o', 'ổ': 'o', 'ỗ': 'o', 'ơ': 'o', 'ờ': 'o', 'ớ': 'o', 'ợ': 'o', 'ở': 'o', 'ỡ': 'o',
    'ù': 'u', 'ú': 'u', 'ụ': 'u', 'ủ': 'u', 'ũ': 'u', 'ư': 'u', 'ừ': 'u', 'ứ': 'u', 'ự': 'u', 'ử': 'u', 'ữ': 'u',
    'ỳ': 'y', 'ý': 'y', 'ỵ': 'y', 'ỷ': 'y', 'ỹ': 'y',
    'đ': 'd',
    'À': 'A', 'Á': 'A', 'Ạ': 'A', 'Ả': 'A', 'Ã': 'A', 'Â': 'A', 'Ầ': 'A', 'Ấ': 'A', 'Ậ': 'A', 'Ẩ': 'A', 'Ẫ': 'A', 'Ă': 'A', 'Ằ': 'A', 'Ắ': 'A', 'Ặ': 'A', 'Ẳ': 'A', 'Ẵ': 'A',
    'È': 'E', 'É': 'E', 'Ẹ': 'E', 'Ẻ': 'E', 'Ẽ': 'E', 'Ê': 'E', 'Ề': 'E', 'Ế': 'E', 'Ệ': 'E', 'Ể': 'E', 'Ễ': 'E',
    'Ì': 'I', 'Í': 'I', 'Ị': 'I', 'Ỉ': 'I', 'Ĩ': 'I',
    'Ò': 'O', 'Ó': 'O', 'Ọ': 'O', 'Ỏ': 'O', 'Õ': 'O', 'Ô': 'O', 'Ồ': 'O', 'Ố': 'O', 'Ộ': 'O', 'Ổ': 'O', 'Ỗ': 'O', 'Ơ': 'O', 'Ờ': 'O', 'Ớ': 'O', 'Ợ': 'O', 'Ở': 'O', 'Ỡ': 'O',
    'Ù': 'U', 'Ú': 'U', 'Ụ': 'U', 'Ủ': 'U', 'Ũ': 'U', 'Ư': 'U', 'Ừ': 'U', 'Ứ': 'U', 'Ự': 'U', 'Ử': 'U', 'Ữ': 'U',
    'Ỳ': 'Y', 'Ý': 'Y', 'Ỵ': 'Y', 'Ỷ': 'Y', 'Ỹ': 'Y',
    'Đ': 'D'
  };
  
  return text.replace(/[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ]/g, (char) => vietnameseMap[char] || char);
}

/**
 * Generate and download ticket as PDF
 */
export async function generateTicketPDF(
  ticket: TicketDto,
  options: TicketPDFOptions = {}
): Promise<void> {
  try {
    const {
      format = 'a4',
      orientation = 'portrait',
      margin = 20
    } = options;

    // Create new PDF document
    const pdf = new jsPDF({
      format,
      orientation,
      unit: 'mm'
    });

    const pageWidth = pdf.internal.pageSize.getWidth();
    const pageHeight = pdf.internal.pageSize.getHeight();
    
    // Use standard font for better compatibility
    pdf.setFont('helvetica', 'normal');
    
    // Title - avoid Vietnamese characters in title
    pdf.setFontSize(24);
    pdf.setTextColor(0, 0, 0);
    pdf.text('TICKET - EVENT PASS', pageWidth / 2, margin + 15, { align: 'center' });
    
    // Event title - convert Vietnamese characters
    pdf.setFontSize(18);
    pdf.setTextColor(60, 60, 60);
    const eventTitle = convertVietnameseText(ticket.eventTitle || 'Event');
    pdf.text(eventTitle, pageWidth / 2, margin + 35, { align: 'center' });
    
    // Ticket number
    pdf.setFontSize(12);
    pdf.setTextColor(100, 100, 100);
    const ticketNumber = ticket.ticketNumber || ticket.id || '';
    pdf.text(`Ticket ID: ${ticketNumber}`, pageWidth / 2, margin + 50, { align: 'center' });
    
    // Create ticket info box - increase width to accommodate QR code
    const boxY = margin + 70;
    const boxHeight = 90; // Increased height
    const qrSize = 35; // QR size in mm
    const contentWidth = pageWidth - 2 * margin - qrSize - 15; // Leave space for QR
    
    // Border
    pdf.setDrawColor(200, 200, 200);
    pdf.setLineWidth(0.5);
    pdf.rect(margin, boxY, pageWidth - 2 * margin, boxHeight);
    
    // Ticket details - left side only
    pdf.setFontSize(12);
    pdf.setTextColor(0, 0, 0);
    let currentY = boxY + 15;
    
    // Event date
    if (ticket.eventStartDate) {
      const eventDate = new Date(ticket.eventStartDate);
      const formattedDate = eventDate.toLocaleString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
      pdf.text(`Date & Time: ${formattedDate}`, margin + 10, currentY);
      currentY += 12;
    }
    
    // Location
    if (ticket.eventLocation) {
      const location = convertVietnameseText(ticket.eventLocation);
      // Split long text if needed
      const maxLength = 35;
      if (location.length > maxLength) {
        pdf.text(`Location: ${location.substring(0, maxLength)}...`, margin + 10, currentY);
      } else {
        pdf.text(`Location: ${location}`, margin + 10, currentY);
      }
      currentY += 12;
    }
    
    // Ticket type
    const ticketType = convertVietnameseText(ticket.ticketTypeName || 'Standard');
    pdf.text(`Type: ${ticketType}`, margin + 10, currentY);
    currentY += 12;
    
    // Price
    const price = ticket.price ? 
      new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(ticket.price) :
      'Free';
    pdf.text(`Price: ${price}`, margin + 10, currentY);
    currentY += 12;
    
    // Status
    const statusText = {
      'RESERVED': 'Reserved',
      'PAID': 'Paid',
      'CHECKED_IN': 'Checked In',
      'CANCELLED': 'Cancelled',
      'EXPIRED': 'Expired'
    }[ticket.status!] || ticket.status;
    
    pdf.text(`Status: ${statusText}`, margin + 10, currentY);
    
    // Generate QR code - positioned on the right side
    if (ticket.id && ticket.eventId && ticket.userId) {
      try {
        const qrCodeDataUrl = await generateTicketQRCode(ticket.id, ticket.eventId, ticket.userId, {
          width: 200,
          margin: 1
        });
        
        // Add QR code - right side of the box
        const qrX = pageWidth - margin - qrSize - 5;
        const qrY = boxY + 15; // Top of the box with some margin
        
        pdf.addImage(qrCodeDataUrl, 'PNG', qrX, qrY, qrSize, qrSize);
        
        // QR code label
        pdf.setFontSize(8);
        pdf.setTextColor(100, 100, 100);
        pdf.text('QR Check-in Code', qrX + qrSize / 2, qrY + qrSize + 5, { align: 'center' });
      } catch (error) {
        console.error('Error generating QR code for PDF:', error);
      }
    }
    
    // Instructions
    const instructionsY = boxY + boxHeight + 20;
    pdf.setFontSize(10);
    pdf.setTextColor(100, 100, 100);
    pdf.text('Instructions:', margin, instructionsY);
    pdf.text('• Bring this ticket to the event', margin, instructionsY + 8);
    pdf.text('• Show QR code for check-in', margin, instructionsY + 16);
    pdf.text('• Contact organizer if you have questions', margin, instructionsY + 24);
    
    // Footer
    const footerY = pageHeight - margin - 10;
    pdf.setFontSize(8);
    pdf.setTextColor(150, 150, 150);
    pdf.text('EventTicketing - Event Booking Platform', pageWidth / 2, footerY, { align: 'center' });
    
    // Download the PDF
    const filename = `ticket-${ticketNumber || 'unknown'}.pdf`;
    pdf.save(filename);
    
  } catch (error) {
    console.error('Error generating ticket PDF:', error);
    throw new Error('Failed to generate ticket PDF');
  }
}

/**
 * Print ticket using browser print with QR code
 */
export async function printTicket(ticket: TicketDto): Promise<void> {
  try {
    // Generate QR code first
    let qrCodeDataUrl = '';
    if (ticket.id && ticket.eventId && ticket.userId) {
      try {
        qrCodeDataUrl = await generateTicketQRCode(ticket.id, ticket.eventId, ticket.userId, {
          width: 200,
          margin: 1
        });
      } catch (error) {
        console.error('Error generating QR code for print:', error);
      }
    }

    // Create a print window with ticket content
    const printWindow = window.open('', '_blank');
    if (!printWindow) {
      alert('Please allow pop-ups to print the ticket');
      return;
    }

    const eventDate = ticket.eventStartDate ? 
      new Date(ticket.eventStartDate).toLocaleString('vi-VN', {
        dateStyle: 'full',
        timeStyle: 'short'
      }) : 'Chưa có thời gian';

    const price = ticket.price ? 
      new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(ticket.price) :
      'Miễn phí';

    const statusText = {
      'RESERVED': 'Chờ thanh toán',
      'PAID': 'Đã thanh toán', 
      'CHECKED_IN': 'Đã check-in',
      'CANCELLED': 'Đã hủy',
      'EXPIRED': 'Hết hạn'
    }[ticket.status!] || ticket.status;

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
        <head>
          <title>Vé - ${ticket.eventTitle}</title>
          <meta charset="UTF-8">
          <style>
            body {
              font-family: Arial, sans-serif;
              max-width: 600px;
              margin: 0 auto;
              padding: 20px;
              line-height: 1.6;
            }
            .header {
              text-align: center;
              border-bottom: 2px solid #333;
              padding-bottom: 20px;
              margin-bottom: 30px;
            }
            .title {
              font-size: 24px;
              font-weight: bold;
              color: #333;
              margin-bottom: 10px;
            }
            .event-title {
              font-size: 18px;
              color: #666;
              margin-bottom: 10px;
            }
            .ticket-number {
              font-size: 12px;
              color: #999;
            }
            .details {
              border: 2px solid #ddd;
              padding: 20px;
              margin: 20px 0;
              background: #f9f9f9;
              position: relative;
              min-height: 150px; /* Ensure enough space for QR code */
            }
            .detail-content {
              width: 65%; /* Leave space for QR code */
              float: left;
            }
            .detail-row {
              margin-bottom: 12px;
              display: flex;
              justify-content: space-between;
            }
            .qr-code {
              position: absolute;
              top: 20px;
              right: 20px;
              text-align: center;
              width: 140px; /* Fixed width for QR section */
            }
            .qr-code img {
              width: 120px;
              height: 120px;
              border: 1px solid #ddd;
            }
            .qr-label {
              font-size: 10px;
              color: #666;
              margin-top: 5px;
            }
            .clearfix::after {
              content: "";
              display: table;
              clear: both;
            }
            .instructions {
              margin-top: 30px;
              padding: 15px;
              background: #f0f0f0;
              border-radius: 5px;
            }
            .footer {
              text-align: center;
              margin-top: 30px;
              font-size: 12px;
              color: #999;
              border-top: 1px solid #ddd;
              padding-top: 20px;
            }
            @media print {
              body { margin: 0; }
              .no-print { display: none; }
            }
          </style>
        </head>
        <body>
          <div class="header">
            <div class="title">VÉ THAM DỰ SỰ KIỆN</div>
            <div class="event-title">${ticket.eventTitle || 'Sự kiện'}</div>
            <div class="ticket-number">Mã vé: ${ticket.ticketNumber || ticket.id || ''}</div>
          </div>
          
          <div class="details clearfix">
            ${qrCodeDataUrl ? `
              <div class="qr-code">
                <img src="${qrCodeDataUrl}" alt="QR Code" />
                <div class="qr-label">Mã QR Check-in</div>
              </div>
            ` : ''}
            
            <div class="detail-content">
              <div class="detail-row">
                <span><strong>Thời gian:</strong></span>
                <span>${eventDate}</span>
              </div>
              <div class="detail-row">
                <span><strong>Địa điểm:</strong></span>
                <span>${ticket.eventLocation || 'Chưa xác định'}</span>
              </div>
              <div class="detail-row">
                <span><strong>Loại vé:</strong></span>
                <span>${ticket.ticketTypeName || 'Standard'}</span>
              </div>
              <div class="detail-row">
                <span><strong>Giá:</strong></span>
                <span>${price}</span>
              </div>
              <div class="detail-row">
                <span><strong>Trạng thái:</strong></span>
                <span>${statusText}</span>
              </div>
            </div>
          </div>
          
          <div class="instructions">
            <h4>Hướng dẫn:</h4>
            <ul>
              <li>Mang theo vé này đến sự kiện</li>
              <li>Xuất trình mã QR để check-in</li>
              <li>Giữ liên hệ với ban tổ chức nếu có thắc mắc</li>
            </ul>
          </div>
          
          <div class="footer">
            EventTicketing - Nền tảng đặt vé sự kiện
          </div>
          
          <script>
            window.onload = function() {
              setTimeout(() => {
                window.print();
                window.onafterprint = function() {
                  window.close();
                }
              }, 500);
            }
          </script>
        </body>
      </html>
    `);
    
    printWindow.document.close();
    
  } catch (error) {
    console.error('Error printing ticket:', error);
    throw new Error('Failed to print ticket');
  }
}
