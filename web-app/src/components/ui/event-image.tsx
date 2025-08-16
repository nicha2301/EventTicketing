"use client";

import Image from "next/image";
import { useState } from "react";
import { isValidImageUrl } from "@/lib/utils/image";

interface EventImageProps {
  src: string;
  alt: string;
  fill?: boolean;
  sizes?: string;
  className?: string;
  width?: number;
  height?: number;
}

export function EventImage({ 
  src, 
  alt, 
  fill = false,
  sizes,
  className = "",
  width,
  height
}: EventImageProps) {
  const [hasError, setHasError] = useState(false);
  
  const getValidImageSrc = (imageSrc: string): string => {
    if (!imageSrc) return "/window.svg";
    
    if (!imageSrc.startsWith('http') && !imageSrc.startsWith('/')) {
      return `/${imageSrc}`;
    }
    
    return imageSrc;
  };
  
  const imageSrc = hasError ? "/window.svg" : getValidImageSrc(src);
  
  const finalSrc = isValidImageUrl(imageSrc) ? imageSrc : "/window.svg";
  
  const imageProps = {
    src: finalSrc,
    alt,
    className,
    onError: () => setHasError(true),
    ...(fill 
      ? { fill: true, sizes } 
      : { width: width || 400, height: height || 250 }
    )
  };
  
  return <Image {...imageProps} />;
}
