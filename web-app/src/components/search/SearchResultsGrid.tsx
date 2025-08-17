"use client";

import { Button } from "@/components/ui/button";
import { EmptySearchState } from "./EmptySearchState";
import { SearchEventCard } from "./SearchEventCard";
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
  if (isLoading && events.length === 0) {
    return (
      <div className="space-y-6">
        {/* Loading skeleton */}
        <div className={`grid gap-6 ${viewMode === 'grid' ? 'md:grid-cols-2 lg:grid-cols-3' : 'grid-cols-1'}`}>
          {Array.from({ length: 6 }).map((_, index) => (
            <div key={index} className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden animate-pulse">
              <div className="h-48 bg-gradient-to-br from-gray-100 to-gray-200"></div>
              <div className="p-5 space-y-3">
                <div className="h-5 bg-gray-200 rounded w-3/4"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2"></div>
                <div className="h-4 bg-gray-200 rounded w-2/3"></div>
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
          <SearchEventCard 
            key={event.id} 
            event={event} 
            viewMode={viewMode}
          />
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
            className="px-8 py-3 text-base font-medium"
          >
            {isLoading ? 'Đang tải...' : 'Xem thêm sự kiện'}
          </Button>
        </div>
      )}
    </div>
  );
}
