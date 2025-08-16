"use client";

import { useState } from "react";
import { Calendar, MapPin, Users, Star, Heart, ExternalLink } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { EmptySearchState } from "./EmptySearchState";
import type { SearchResult } from "@/hooks/useEnhancedSearch";

interface SearchResultsGridProps {
  events: SearchResult[];
  isLoading: boolean;
  hasMore: boolean;
  onLoadMore: () => void;
  viewMode?: 'grid' | 'list';
}

export function SearchResultsGrid({
  events,
  isLoading,
  hasMore,
  onLoadMore,
  viewMode = 'grid'
}: SearchResultsGridProps) {
  const [likedEvents, setLikedEvents] = useState<Set<string>>(new Set());

  const toggleLike = (eventId: string) => {
    setLikedEvents(prev => {
      const newSet = new Set(prev);
      if (newSet.has(eventId)) {
        newSet.delete(eventId);
      } else {
        newSet.add(eventId);
      }
      return newSet;
    });
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

  if (isLoading && events.length === 0) {
    return (
      <div className="space-y-6">
        {/* Loading skeleton */}
        <div className={`grid gap-6 ${viewMode === 'grid' ? 'md:grid-cols-2 lg:grid-cols-3' : 'grid-cols-1'}`}>
          {Array.from({ length: 6 }).map((_, index) => (
            <div key={index} className="bg-white rounded-lg shadow-md overflow-hidden animate-pulse">
              <div className="h-48 bg-gray-300"></div>
              <div className="p-4 space-y-3">
                <div className="h-4 bg-gray-300 rounded w-3/4"></div>
                <div className="h-3 bg-gray-300 rounded w-1/2"></div>
                <div className="h-3 bg-gray-300 rounded w-2/3"></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (events.length === 0) {
    return <EmptySearchState type="no-results" />;
  }

  return (
    <div className="space-y-6">
      <div className={`grid gap-6 ${viewMode === 'grid' ? 'md:grid-cols-2 lg:grid-cols-3' : 'grid-cols-1'}`}>
        {events.map((event) => (
          <div
            key={event.id}
            className={`bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow ${
              viewMode === 'list' ? 'flex' : ''
            }`}
          >
            {/* Event Image */}
            <div className={`relative ${viewMode === 'list' ? 'w-64 flex-shrink-0' : 'h-48'}`}>
              <Image
                src={event.imageUrl || '/next.svg'}
                alt={event.title}
                fill
                className="object-cover"
                unoptimized={!event.imageUrl}
              />
              
              {/* Badges */}
              <div className="absolute top-3 left-3 flex flex-col space-y-1">
                {event.isFeatured && (
                  <span className="px-2 py-1 bg-yellow-500 text-white text-xs font-medium rounded">
                    Nổi bật
                  </span>
                )}
                {event.isPopular && (
                  <span className="px-2 py-1 bg-red-500 text-white text-xs font-medium rounded">
                    Hot
                  </span>
                )}
                {event.isFree && (
                  <span className="px-2 py-1 bg-green-500 text-white text-xs font-medium rounded">
                    Miễn phí
                  </span>
                )}
              </div>

              {/* Like button */}
              <button
                onClick={(e) => {
                  e.preventDefault();
                  toggleLike(event.id);
                }}
                className="absolute top-3 right-3 p-2 bg-white bg-opacity-80 rounded-full hover:bg-opacity-100 transition-all"
              >
                <Heart
                  className={`w-4 h-4 ${
                    likedEvents.has(event.id)
                      ? 'fill-red-500 text-red-500'
                      : 'text-gray-600'
                  }`}
                />
              </button>
            </div>

            {/* Event Details */}
            <div className={`p-4 ${viewMode === 'list' ? 'flex-1' : ''}`}>
              <div className="flex items-start justify-between mb-2">
                <span className="inline-block px-2 py-1 bg-blue-100 text-blue-800 text-xs font-medium rounded">
                  {event.category}
                </span>
                {event.stats.averageRating > 0 && (
                  <div className="flex items-center text-yellow-500">
                    <Star className="w-4 h-4 fill-current" />
                    <span className="text-xs text-gray-600 ml-1">
                      {event.stats.averageRating.toFixed(1)}
                    </span>
                  </div>
                )}
              </div>

              <h3 className="font-semibold text-gray-900 mb-2 line-clamp-2">
                {event.title}
              </h3>

              <div className="space-y-2 text-sm text-gray-600 mb-3">
                <div className="flex items-center">
                  <Calendar className="w-4 h-4 mr-2 flex-shrink-0" />
                  <span>{formatDate(event.startDate)}</span>
                </div>
                
                <div className="flex items-center">
                  <MapPin className="w-4 h-4 mr-2 flex-shrink-0" />
                  <span className="truncate">{event.location}</span>
                </div>

                {event.stats.attendees > 0 && (
                  <div className="flex items-center">
                    <Users className="w-4 h-4 mr-2 flex-shrink-0" />
                    <span>{event.stats.attendees} người tham gia</span>
                  </div>
                )}
              </div>

              {event.description && (
                <p className="text-sm text-gray-600 mb-3 line-clamp-2">
                  {event.description}
                </p>
              )}

              <div className="flex items-center justify-between">
                <div className="text-lg font-bold text-blue-600">
                  {formatPrice(event.ticketPrice.min, event.ticketPrice.max, event.isFree)}
                </div>
                
                <Link href={`/events/${event.id}`}>
                  <Button size="sm" className="flex items-center">
                    Xem chi tiết
                    <ExternalLink className="w-3 h-3 ml-1" />
                  </Button>
                </Link>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Load More Button */}
      {hasMore && (
        <div className="text-center pt-6">
          <Button
            onClick={onLoadMore}
            disabled={isLoading}
            variant="outline"
            size="lg"
          >
            {isLoading ? 'Đang tải...' : 'Xem thêm sự kiện'}
          </Button>
        </div>
      )}
    </div>
  );
}
