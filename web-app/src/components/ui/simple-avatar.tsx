'use client'

import { User } from 'lucide-react'
import { cn } from '@/lib/utils/cn'

interface SimpleAvatarProps {
  src?: string | null
  fallback?: string
  className?: string
}

export function SimpleAvatar({ src, fallback, className }: SimpleAvatarProps) {
  if (src) {
    return (
      <div className={cn("relative overflow-hidden rounded-full", className)}>
        <img
          src={src}
          alt="Avatar"
          className="w-full h-full object-cover"
          onError={(e) => {
            const target = e.target as HTMLImageElement
            target.style.display = 'none'
            const fallbackDiv = target.parentElement?.querySelector('.fallback') as HTMLElement
            if (fallbackDiv) {
              fallbackDiv.style.display = 'flex'
            }
          }}
        />
        <div className="fallback absolute inset-0 bg-blue-100 text-blue-600 text-sm font-medium hidden items-center justify-center">
          {fallback || <User className="w-1/2 h-1/2" />}
        </div>
      </div>
    )
  }

  return (
    <div className={cn("bg-blue-100 text-blue-600 text-sm font-medium flex items-center justify-center rounded-full", className)}>
      {fallback || <User className="w-1/2 h-1/2" />}
    </div>
  )
}
