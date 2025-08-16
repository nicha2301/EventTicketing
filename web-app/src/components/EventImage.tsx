"use client";

import Image from "next/image";
import { useState } from "react";

interface EventImageProps {
  src?: string;
  alt: string;
  className?: string;
  width?: number;
  height?: number;
}

export default function EventImage({ 
  src, 
  alt, 
  className = "", 
  width = 300, 
  height = 200 
}: EventImageProps) {
  const [imageError, setImageError] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const sanitizeEventImageUrl = (url?: string): string => {
    if (!url) return "/assets/images/event-placeholder.jpg";
    
    if (url.startsWith("http://") || url.startsWith("https://")) {
      return url;
    }
    
    if (url.startsWith("/")) {
      return url;
    }
    
    return `/assets/images/events/${url}`;
  };

  const imageUrl = sanitizeEventImageUrl(src);

  if (imageError) {
    return (
      <div className={`bg-gray-200 flex items-center justify-center ${className}`}>
        <svg 
          className="w-8 h-8 text-gray-400" 
          fill="none" 
          stroke="currentColor" 
          viewBox="0 0 24 24"
        >
          <path 
            strokeLinecap="round" 
            strokeLinejoin="round" 
            strokeWidth={2} 
            d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" 
          />
        </svg>
      </div>
    );
  }

  return (
    <div className={`relative overflow-hidden ${className}`}>
      {isLoading && (
        <div className="absolute inset-0 bg-gray-200 animate-pulse flex items-center justify-center">
          <div className="w-8 h-8 border-2 border-gray-300 border-t-blue-600 rounded-full animate-spin"></div>
        </div>
      )}
      <Image
        src={imageUrl}
        alt={alt}
        width={width}
        height={height}
        className={`${isLoading ? 'opacity-0' : 'opacity-100'} transition-opacity duration-300 object-cover w-full h-full`}
        onLoad={() => setIsLoading(false)}
        onError={() => {
          setImageError(true);
          setIsLoading(false);
        }}
        unoptimized
      />
    </div>
  );
}
