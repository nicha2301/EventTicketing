"use client";

import { useState } from "react";
import { Calendar, MapPin, Users, Star, Heart, ExternalLink, Tag } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import type { SearchResult } from "@/hooks/useEnhancedSearch";

interface SearchEventCardProps {
  event: SearchResult;
  viewMode?: 'grid' | 'list';
}

export function SearchEventCard({ event, viewMode = 'grid' }: SearchEventCardProps) {
  const [isLiked, setIsLiked] = useState(false);

  const toggleLike = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsLiked(!isLiked);
  };

  const formatPrice = (min: number, max: number, isFree: boolean) => {
    if (isFree) return 'Miễn phí';
    if (min === max) return `${min.toLocaleString('vi-VN')}đ`;
    return `${min.toLocaleString('vi-VN')}đ - ${max.toLocaleString('vi-VN')}đ`;
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  };

  return (
    <div className={`group bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-lg hover:border-gray-200 transition-all duration-300 hover:-translate-y-1 ${
      viewMode === 'list' ? 'flex' : ''
    }`}>
      {/* Event Image */}
      <div className={`relative ${viewMode === 'list' ? 'w-64 flex-shrink-0' : 'aspect-[4/3]'}`}>
        <Image
          src={event.imageUrl || '/next.svg'}
          alt={event.title}
          fill
          className="object-cover transition duration-500 group-hover:scale-110"
          unoptimized={!event.imageUrl}
        />
        
        {/* Category Badge */}
        <div className="absolute top-3 left-3">
          <span className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white/90 backdrop-blur-sm rounded-full text-xs font-medium text-gray-700 shadow-sm">
            <Tag className="w-3 h-3" />
            {event.category}
          </span>
        </div>

        {/* Price Badge */}
        <div className="absolute top-3 right-3">
          <span className={`inline-flex items-center px-3 py-1.5 rounded-full text-xs font-bold shadow-sm ${
            event.isFree 
              ? 'bg-green-500 text-white' 
              : 'bg-white/90 backdrop-blur-sm text-gray-900'
          }`}>
            {event.isFree ? 'Miễn phí' : formatPrice(event.ticketPrice.min, event.ticketPrice.max, event.isFree)}
          </span>
        </div>

        {/* Like button */}
        <button
          onClick={toggleLike}
          className="absolute bottom-3 right-3 p-2 bg-white/90 backdrop-blur-sm rounded-full hover:bg-white transition-all shadow-sm"
        >
          <Heart
            className={`w-4 h-4 ${
              isLiked ? 'fill-red-500 text-red-500' : 'text-gray-600'
            }`}
          />
        </button>
      </div>

      {/* Event Details */}
      <div className={`p-5 space-y-3 ${viewMode === 'list' ? 'flex-1' : ''}`}>
        {/* Title */}
        <h3 className="font-bold text-gray-900 text-lg leading-tight line-clamp-2 group-hover:text-blue-600 transition-colors">
          {event.title}
        </h3>

        {/* Date and Location */}
        <div className="space-y-2 text-sm text-gray-600">
          <div className="flex items-center gap-1.5">
            <Calendar className="w-4 h-4 text-blue-500" />
            <span>{formatDate(event.startDate)}</span>
          </div>
          
          <div className="flex items-center gap-1.5">
            <MapPin className="w-4 h-4 text-red-500" />
            <span className="line-clamp-1">{event.location}</span>
          </div>
        </div>

        {/* Stats and Rating */}
        <div className="flex items-center justify-between pt-2">
          {event.stats.averageRating > 0 && (
            <div className="flex items-center gap-1 text-sm">
              <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
              <span className="font-medium text-gray-900">
                {event.stats.averageRating.toFixed(1)}
              </span>
              <span className="text-gray-500">({event.stats.ratings})</span>
            </div>
          )}
          
          {event.stats.attendees > 0 && (
            <div className="flex items-center gap-1 text-sm text-gray-600">
              <Users className="w-4 h-4" />
              <span>{event.stats.attendees}</span>
            </div>
          )}
        </div>

        {/* Action Button */}
        <div className="pt-3">
          <Link href={`/events/${event.id}`}>
            <Button 
              size="sm" 
              className="w-full bg-blue-600 hover:bg-blue-700 text-white transition-colors"
            >
              Xem chi tiết
              <ExternalLink className="w-3 h-3 ml-1" />
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
