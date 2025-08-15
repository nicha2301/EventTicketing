export function isValidImageUrl(url: string | null | undefined): boolean {
  if (!url || typeof url !== 'string') return false;
  
  return url.startsWith('http://') || 
         url.startsWith('https://') || 
         url.startsWith('/');
}

export function sanitizeImageUrl(url: string | null | undefined): string | null {
  if (!url || typeof url !== 'string') return null;
  
  if (!url.startsWith('http') && !url.startsWith('/')) {
    return `/${url}`;
  }
  
  return url;
}

export function getImageUrlWithFallback(
  url: string | null | undefined, 
  fallback: string = '/window.svg'
): string {
  const sanitized = sanitizeImageUrl(url);
  return sanitized || fallback;
}

export function sanitizeEventImageUrl(
  featuredImageUrl?: string | null, 
  imageUrls?: string[] | null
): string {
  const primaryUrl = featuredImageUrl || imageUrls?.[0];
  return getImageUrlWithFallback(primaryUrl, '/event-placeholder.svg');
}
