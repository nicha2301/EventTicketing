"use client";

import { useState, useEffect } from "react";
import { Calendar, MapPin, DollarSign, Tag, X, Filter } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useSearchMetadata } from "@/hooks/useSearchMetadata";

export interface SearchFilters {
  query: string;
  category?: string;
  location?: string;
  priceMin?: number;
  priceMax?: number;
  dateFrom?: string;
  dateTo?: string;
  isFree?: boolean;
}

interface SearchFiltersProps {
  filters: SearchFilters;
  onFiltersChange: (filters: SearchFilters) => void;
  onClear: () => void;
}

export function SearchFilters({ 
  filters, 
  onFiltersChange, 
  onClear 
}: SearchFiltersProps) {
  const [localFilters, setLocalFilters] = useState(filters);
  const { categories, locations, isLoading } = useSearchMetadata();

  // Derived categories and locations from API
  const CATEGORIES = (categories || []).map((cat: any) => cat.name).filter(Boolean);
  const POPULAR_LOCATIONS = (locations || []).map((loc: any) => loc.name || loc.city).filter(Boolean);

  const updateFilter = (key: keyof SearchFilters, value: any) => {
    const newFilters = { ...localFilters, [key]: value };
    setLocalFilters(newFilters);
    onFiltersChange(newFilters);
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
    <div className="space-y-6">
      {/* Modern Filter Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-blue-50 rounded-lg">
            <Filter className="w-5 h-5 text-blue-600" />
          </div>
          <div>
            <h3 className="font-semibold text-gray-900">Bộ lọc</h3>
            {hasActiveFilters && (
              <p className="text-sm text-gray-500">{getActiveFilterCount()} bộ lọc đang hoạt động</p>
            )}
          </div>
        </div>
        
        {hasActiveFilters && (
          <Button
            variant="ghost"
            size="sm"
            onClick={onClear}
            className="text-red-600 hover:text-red-700 hover:bg-red-50 px-3 py-1.5 text-sm font-medium"
          >
            <X className="w-4 h-4 mr-1" />
            Xóa tất cả
          </Button>
        )}
      </div>

      {/* Filter Content - Luôn hiển thị */}
      <div className="space-y-6">
          {/* Category Filter */}
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <div className="p-1.5 bg-blue-50 rounded-lg">
                <Tag className="w-4 h-4 text-blue-600" />
              </div>
              <label className="text-sm font-medium text-gray-900">Thể loại</label>
            </div>
            
            <select
              value={filters.category || ''}
              onChange={(e) => updateFilter('category', e.target.value || undefined)}
              className="w-full px-3 py-2.5 text-sm border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white transition-all"
            >
              <option value="">Tất cả thể loại</option>
              {CATEGORIES.map((category: string) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>

          {/* Location Filter */}
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <div className="p-1.5 bg-green-50 rounded-lg">
                <MapPin className="w-4 h-4 text-green-600" />
              </div>
              <label className="text-sm font-medium text-gray-900">Địa điểm</label>
            </div>
            
            <select
              value={filters.location || ''}
              onChange={(e) => updateFilter('location', e.target.value || undefined)}
              className="w-full px-3 py-2.5 text-sm border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent bg-white transition-all"
            >
              <option value="">Tất cả địa điểm</option>
              {POPULAR_LOCATIONS.map((location: string) => (
                <option key={location} value={location}>
                  {location}
                </option>
              ))}
            </select>
          </div>

          {/* Price Range Filter */}
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <div className="p-1.5 bg-amber-50 rounded-lg">
                <DollarSign className="w-4 h-4 text-amber-600" />
              </div>
              <label className="text-sm font-medium text-gray-900">Khoảng giá</label>
            </div>
            
            {/* Free Events Toggle */}
            <div className="p-3 bg-gray-50 rounded-xl">
              <label className="flex items-center gap-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={filters.isFree || false}
                  onChange={(e) => updateFilter('isFree', e.target.checked || undefined)}
                  className="w-4 h-4 text-blue-600 bg-white border-gray-300 rounded focus:ring-blue-500 focus:ring-2"
                />
                <span className="text-sm font-medium text-gray-700">Chỉ sự kiện miễn phí</span>
              </label>
            </div>
            
            {/* Price Range Inputs */}
            {!filters.isFree && (
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <Input
                    type="number"
                    placeholder="Từ"
                    value={filters.priceMin || ''}
                    onChange={(e) => updateFilter('priceMin', 
                      e.target.value ? parseInt(e.target.value) : undefined
                    )}
                    className="rounded-xl border-gray-200 focus:ring-2 focus:ring-amber-500 focus:border-transparent"
                  />
                  <label className="text-xs text-gray-500 mt-1 block">Giá tối thiểu (₫)</label>
                </div>
                <div>
                  <Input
                    type="number"
                    placeholder="Đến"
                    value={filters.priceMax || ''}
                    onChange={(e) => updateFilter('priceMax', 
                      e.target.value ? parseInt(e.target.value) : undefined
                    )}
                    className="rounded-xl border-gray-200 focus:ring-2 focus:ring-amber-500 focus:border-transparent"
                  />
                  <label className="text-xs text-gray-500 mt-1 block">Giá tối đa (₫)</label>
                </div>
              </div>
            )}
          </div>

          {/* Date Range Filter */}
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <div className="p-1.5 bg-purple-50 rounded-lg">
                <Calendar className="w-4 h-4 text-purple-600" />
              </div>
              <label className="text-sm font-medium text-gray-900">Thời gian</label>
            </div>
            
            <div className="space-y-3">
              <div>
                <Input
                  type="date"
                  value={filters.dateFrom || ''}
                  onChange={(e) => updateFilter('dateFrom', e.target.value || undefined)}
                  className="w-full rounded-xl border-gray-200 focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                />
                <label className="text-xs text-gray-500 mt-1 block">Từ ngày</label>
              </div>
              <div>
                <Input
                  type="date"
                  value={filters.dateTo || ''}
                  onChange={(e) => updateFilter('dateTo', e.target.value || undefined)}
                  className="w-full rounded-xl border-gray-200 focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                />
                <label className="text-xs text-gray-500 mt-1 block">Đến ngày</label>
              </div>
            </div>
          </div>
        </div>
    </div>
  );
}
