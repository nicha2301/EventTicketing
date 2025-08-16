"use client";

import { Suspense, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { Search, Grid3x3, List, Filter, X } from "lucide-react";
import { useEnhancedSearch } from "@/hooks/useEnhancedSearch";
import { SearchFilters } from "@/components/search/SearchFilters";
import { SearchSort } from "@/components/search/SearchSort";
import { SavedSearches } from "@/components/search/SavedSearches";
import { SearchResultsGrid } from "@/components/search/SearchResultsGrid";
import { EmptySearchState } from "@/components/search/EmptySearchState";
import { Button } from "@/components/ui/button";
import SectionHeading from "@/components/SectionHeading";
import LoadingSpinner from "@/components/ui/LoadingSpinner";

export const dynamic = "force-dynamic";

function SearchContent() {
  const searchParams = useSearchParams();
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  
  const {
    events,
    total,
    totalPages,
    currentPage,
    hasMore,
    filters,
    sortBy,
    isLoading,
    error,
    searchHistory,
    updateFilters,
    updateSort,
    applySearch,
    resetSearch,
    loadMore,
  } = useEnhancedSearch();

  useEffect(() => {
    const q = searchParams.get("q");
    const category = searchParams.get("category");
    const location = searchParams.get("location");
    
    if (q || category || location) {
      updateFilters({
        query: q || '',
        category: category || '',
        location: location || '',
      });
    }
  }, [searchParams, updateFilters]);

  const handleFiltersChange = (newFilters: any) => {
    updateFilters(newFilters);
  };

  const handleSortChange = (newSort: any) => {
    updateSort(newSort);
  };

  const handleApplySearch = (newFilters: any, newSort: any) => {
    applySearch(newFilters, newSort);
  };

  const handleResetFilters = () => {
    resetSearch();
  };

  const hasActiveFilters = Object.entries(filters).some(([key, value]) => 
    key !== 'query' && value !== undefined && value !== '' && value !== null
  );
  
  const getActiveFilterCount = () => {
    return Object.entries(filters).filter(([key, value]) => 
      key !== 'query' && value !== undefined && value !== '' && value !== null
    ).length;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b">
        <div className="container-page py-6">
          <SectionHeading 
            title="Tìm kiếm sự kiện" 
            subtitle={`${total} sự kiện được tìm thấy`}
          />
          
          {/* Main Search Bar */}
          <div className="mt-6">
            <div className="relative max-w-3xl mx-auto lg:mx-0">
              <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                type="text"
                placeholder="Tìm kiếm sự kiện..."
                value={filters.query}
                onChange={(e) => updateFilters({ query: e.target.value })}
                className="w-full pl-12 pr-4 py-4 border border-gray-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50 hover:bg-white transition-colors text-lg"
              />
            </div>
          </div>
        </div>
      </div>

      <div className="container-page py-6">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Sidebar - Fixed width trên desktop */}
          <div className="w-full lg:w-80 lg:flex-shrink-0">
            <div className="sticky top-24 space-y-6">
              {/* Search Filters Card */}
              <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                <div className="p-6 border-b border-gray-50 bg-gradient-to-r from-blue-50 to-indigo-50">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="p-2 bg-white rounded-xl shadow-sm">
                        <Filter className="w-5 h-5 text-blue-600" />
                      </div>
                      <div>
                        <h3 className="font-semibold text-gray-900">Bộ lọc tìm kiếm</h3>
                        {hasActiveFilters && (
                          <p className="text-xs text-blue-600 mt-0.5">
                            {getActiveFilterCount()} bộ lọc đang áp dụng
                          </p>
                        )}
                      </div>
                    </div>
                    
                    {hasActiveFilters && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={handleResetFilters}
                        className="text-red-600 hover:text-red-700 hover:bg-red-50 text-xs font-medium px-3 py-1.5 rounded-lg"
                      >
                        <X className="w-4 h-4 mr-1" />
                        Xóa tất cả
                      </Button>
                    )}
                  </div>
                </div>
                
                <div className="p-6">
                  <SearchFilters
                    filters={filters}
                    onFiltersChange={handleFiltersChange}
                    onClear={handleResetFilters}
                  />
                </div>
              </div>

              {/* Saved Searches Card */}
              <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                <SavedSearches
                  currentFilters={filters}
                  currentSort={sortBy}
                  onApplySearch={handleApplySearch}
                />
              </div>
            </div>
          </div>

          {/* Main Content - Flexible width */}
          <div className="flex-1 min-w-0">
            {/* Sort and View Controls */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6 gap-4 bg-white rounded-xl p-4 shadow-sm border border-gray-100">
              <div className="flex items-center gap-4">
                <SearchSort
                  sortBy={sortBy}
                  onSortChange={handleSortChange}
                  resultCount={total}
                />
              </div>
              
              {/* View Mode Toggle */}
              <div className="flex items-center gap-3">
                <span className="text-sm text-gray-600 font-medium">Hiển thị:</span>
                <div className="flex border border-gray-200 rounded-lg overflow-hidden bg-gray-50">
                  <button
                    onClick={() => setViewMode('grid')}
                    className={`px-3 py-2 text-sm font-medium transition-all ${
                      viewMode === 'grid' 
                        ? 'bg-blue-500 text-white shadow-sm' 
                        : 'text-gray-600 hover:text-gray-900 hover:bg-white'
                    }`}
                  >
                    <Grid3x3 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => setViewMode('list')}
                    className={`px-3 py-2 text-sm font-medium transition-all ${
                      viewMode === 'list' 
                        ? 'bg-blue-500 text-white shadow-sm' 
                        : 'text-gray-600 hover:text-gray-900 hover:bg-white'
                    }`}
                  >
                    <List className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>

            {/* Error State or API Unavailable */}
            {error && (
              <EmptySearchState type="api-unavailable" />
            )}

            {/* Search Results */}
            {!error && (
              <SearchResultsGrid
                events={events}
                isLoading={isLoading}
                hasMore={hasMore}
                onLoadMore={loadMore}
                viewMode={viewMode}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function SearchPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center space-y-4">
          <LoadingSpinner size="lg" />
          <p className="text-gray-600">Đang tải trang tìm kiếm...</p>
        </div>
      </div>
    }>
      <SearchContent />
    </Suspense>
  );
}


