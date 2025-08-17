import { useState, useCallback, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { searchEvents, type SearchEventsParams } from '@/lib/api/modules/events';
import { useSearchMetadata } from './useSearchMetadata';
import type { SearchFilters } from '@/components/search/SearchFilters';
import type { SortOption } from '@/components/search/SearchSort';

export interface EnhancedSearchParams {
  query?: string;
  category?: string;
  location?: string;
  priceMin?: number;
  priceMax?: number;
  dateFrom?: string;
  dateTo?: string;
  isFree?: boolean;
  sortBy: SortOption;
  page: number;
  limit: number;
}

export interface SearchResult {
  id: string;
  title: string;
  description: string;
  imageUrl?: string;
  location: string;
  category: string;
  startDate: string;
  endDate: string;
  ticketPrice: {
    min: number;
    max: number;
    currency: string;
  };
  organizer: {
    name: string;
    avatar?: string;
  };
  stats: {
    attendees: number;
    ratings: number;
    averageRating: number;
  };
  isFree: boolean;
  isPopular: boolean;
  isFeatured: boolean;
}

export interface SearchResponse {
  events: SearchResult[];
  total: number;
  totalPages: number;
  currentPage: number;
  hasMore: boolean;
  aggregations: {
    categories: { name: string; count: number }[];
    locations: { name: string; count: number }[];
    priceRanges: { min: number; max: number; count: number }[];
  };
}

const defaultFilters: SearchFilters = {
  query: '',
  category: '',
  location: '',
  priceMin: undefined,
  priceMax: undefined,
  dateFrom: '',
  dateTo: '',
  isFree: false,
};

export function useEnhancedSearch() {
  const [filters, setFilters] = useState<SearchFilters>(defaultFilters);
  const [sortBy, setSortBy] = useState<SortOption>('relevance');
  const [page, setPage] = useState(1);
  const [searchHistory, setSearchHistory] = useState<string[]>([]);

  const limit = 12;

  // Get category and location metadata for mapping
  const { getCategoryId, getLocationId } = useSearchMetadata();

  // Build search parameters
  const searchParams: EnhancedSearchParams = {
    ...filters,
    sortBy,
    page,
    limit,
  };

  // Main search query
  const {
    data: searchData,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ['enhanced-search', searchParams],
    queryFn: async () => {
      try {
        const apiParams: SearchEventsParams = {
          keyword: filters.query || undefined,
          categoryId: filters.category ? getCategoryId(filters.category) : undefined,
          locationId: filters.location ? getLocationId(filters.location) : undefined,
          minPrice: filters.priceMin,
          maxPrice: filters.priceMax,
          startDate: filters.dateFrom || undefined,
          endDate: filters.dateTo || undefined,
          page: page - 1, 
          size: limit,
          sort: sortBy === 'relevance' ? undefined : mapSortToAPI(sortBy),
        };

        const cleanParams: SearchEventsParams = Object.fromEntries(
          Object.entries(apiParams).filter(([_, value]) => value !== undefined)
        ) as SearchEventsParams;

        const response = await searchEvents(cleanParams);
        
        // Transform API response to our format
        return transformSearchResponse(response);
      } catch (error) {
        console.warn('API unavailable, search failed');
        
        // Return empty result when API fails
        return {
          events: [],
          total: 0,
          totalPages: 0,
          currentPage: page,
          hasMore: false,
          aggregations: {
            categories: [],
            locations: [],
            priceRanges: [],
          },
        };
      }
    },
    staleTime: 30000,
    enabled: true,
    retry: 1, 
    retryOnMount: false,
  });

  // Update filters
  const updateFilters = useCallback((newFilters: Partial<SearchFilters>) => {
    setFilters(prev => ({ ...prev, ...newFilters }));
    setPage(1);
  }, []);

  // Update sort
  const updateSort = useCallback((newSort: SortOption) => {
    setSortBy(newSort);
    setPage(1); 
  }, []);

  // Apply complete search
  const applySearch = useCallback((newFilters: SearchFilters, newSort: SortOption) => {
    setFilters(newFilters);
    setSortBy(newSort);
    setPage(1);
    
    if (newFilters.query && newFilters.query.trim()) {
      addToSearchHistory(newFilters.query.trim());
    }
  }, []);

  // Reset search
  const resetSearch = useCallback(() => {
    setFilters(defaultFilters);
    setSortBy('relevance');
    setPage(1);
  }, []);

  // Load more (pagination)
  const loadMore = useCallback(() => {
    if (searchData?.hasMore) {
      setPage(prev => prev + 1);
    }
  }, [searchData?.hasMore]);

  // Search history management
  const addToSearchHistory = useCallback((query: string) => {
    setSearchHistory(prev => {
      const updated = [query, ...prev.filter(q => q !== query)].slice(0, 5);
      localStorage.setItem('eventTicketing_searchHistory', JSON.stringify(updated));
      return updated;
    });
  }, []);

  // Load search history on mount
  useEffect(() => {
    try {
      const saved = localStorage.getItem('eventTicketing_searchHistory');
      if (saved) {
        setSearchHistory(JSON.parse(saved));
      }
    } catch (error) {
      console.error('Error loading search history:', error);
    }
  }, []);

  return {
    // Data
    events: searchData?.events || [],
    total: searchData?.total || 0,
    totalPages: searchData?.totalPages || 0,
    currentPage: page,
    hasMore: searchData?.hasMore || false,
    aggregations: searchData?.aggregations,
    
    // State
    filters,
    sortBy,
    isLoading,
    error,
    searchHistory,
    
    // Actions
    updateFilters,
    updateSort,
    applySearch,
    resetSearch,
    loadMore,
    refetch,
    setPage,
  };
}

// Helper function to map our sort options to API format
function mapSortToAPI(sort: SortOption): string {
  const mapping: Record<SortOption, string> = {
    relevance: 'title,asc', 
    'date-asc': 'startDate,asc',
    'date-desc': 'startDate,desc',
    'price-asc': 'price,asc',
    'price-desc': 'price,desc',
    popularity: 'title,asc', 
    newest: 'createdDate,desc',
  };
  return mapping[sort] || 'title,asc';
}

// Helper function to transform API response
function transformSearchResponse(apiResponse: any): SearchResponse {
  return {
    events: (apiResponse.events || []).map((event: any) => ({
      id: event.id,
      title: event.title || '',
      description: event.shortDescription || event.description || '',
      imageUrl: event.primaryImageUrl || event.featuredImageUrl,
      location: event.city || event.address || 'Chưa xác định',
      category: event.categoryName || 'Khác',
      startDate: event.startDate,
      endDate: event.endDate || event.startDate,
      ticketPrice: {
        min: event.isFree ? 0 : (event.price || event.minTicketPrice || 0),
        max: event.isFree ? 0 : (event.price || event.maxTicketPrice || 0),
        currency: 'VND',
      },
      organizer: {
        name: event.organizer?.name || event.organizerName || 'Organizer',
        avatar: event.organizer?.avatar,
      },
      stats: {
        attendees: event.attendeesCount || 0,
        ratings: event.ratingsCount || 0,
        averageRating: event.averageRating || 0,
      },
      isFree: event.isFree || false,
      isPopular: event.isPopular || false,
      isFeatured: event.isFeatured || false,
    })),
    total: apiResponse.totalElements || 0,
    totalPages: apiResponse.totalPages || 1,
    currentPage: (apiResponse.currentPage || 0) + 1, 
    hasMore: apiResponse.totalPages ? (apiResponse.currentPage + 1) < apiResponse.totalPages : false,
    aggregations: {
      categories: [], 
      locations: [],
      priceRanges: [],
    },
  };
}
