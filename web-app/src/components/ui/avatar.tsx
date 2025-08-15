"use client";

import Image from "next/image";
import { User } from "lucide-react";
import { useState } from "react";
import { isValidImageUrl } from "@/lib/utils/image";

interface AvatarProps {
  src?: string | null;
  alt?: string;
  size?: number;
  className?: string;
}

export function Avatar({ 
  src, 
  alt = "Avatar", 
  size = 96, 
  className = "" 
}: AvatarProps) {
  const [hasError, setHasError] = useState(false);
  
  // Kiểm tra src có hợp lệ không và handle error
  const shouldShowImage = isValidImageUrl(src) && !hasError;
  
  return (
    <div className={`relative overflow-hidden bg-gray-100 border-2 border-gray-200 ${className}`}>
      {shouldShowImage ? (
        <Image
          src={src!}
          alt={alt}
          width={size}
          height={size}
          className="w-full h-full object-cover"
          onError={() => setHasError(true)}
          priority={size > 48} // Priority cho avatar lớn
        />
      ) : (
        <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-blue-100 to-blue-200">
          <User className={`text-blue-600 ${size > 48 ? 'w-8 h-8' : 'w-4 h-4'}`} />
        </div>
      )}
    </div>
  );
}
