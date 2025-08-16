export function formatCurrency(amount: number, currency: string = 'VND'): string {
  try {
    if (currency === 'VND') {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }).format(amount);
    }
    
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  } catch (error) {
    // Fallback formatting
    if (currency === 'VND') {
      return `${amount.toLocaleString('vi-VN')} ₫`;
    }
    return `${amount.toLocaleString('vi-VN')} ${currency}`;
  }
}

export function formatPrice(amount: number): string {
  return formatCurrency(amount, 'VND');
}

export function formatPriceShort(amount: number): string {
  try {
    if (amount >= 1000000) {
      const millions = amount / 1000000;
      return `${millions.toFixed(millions >= 10 ? 0 : 1)}M ₫`;
    } else if (amount >= 1000) {
      const thousands = amount / 1000;
      return `${thousands.toFixed(thousands >= 10 ? 0 : 1)}K ₫`;
    }
    return `${amount.toLocaleString('vi-VN')} ₫`;
  } catch (error) {
    return formatCurrency(amount);
  }
}

export function parseCurrency(currencyString: string): number {
  try {
    // Remove currency symbols and spaces, keep only digits and decimal points
    const cleanString = currencyString.replace(/[^\d.,]/g, '');
    
    // Handle Vietnamese number format (1.000.000,50)
    if (cleanString.includes(',') && cleanString.lastIndexOf(',') > cleanString.lastIndexOf('.')) {
      // Replace dots (thousands separator) and comma (decimal separator)
      const normalized = cleanString.replace(/\./g, '').replace(',', '.');
      return parseFloat(normalized);
    }
    
    // Handle standard format (1,000,000.50)
    const normalized = cleanString.replace(/,/g, '');
    return parseFloat(normalized);
  } catch (error) {
    return 0;
  }
}

export function calculateDiscount(originalPrice: number, discountedPrice: number): {
  amount: number;
  percentage: number;
} {
  const discountAmount = originalPrice - discountedPrice;
  const discountPercentage = (discountAmount / originalPrice) * 100;
  
  return {
    amount: discountAmount,
    percentage: Math.round(discountPercentage)
  };
}

export function addVAT(amount: number, vatRate: number = 0.1): number {
  return amount * (1 + vatRate);
}

export function removeVAT(amount: number, vatRate: number = 0.1): number {
  return amount / (1 + vatRate);
}
