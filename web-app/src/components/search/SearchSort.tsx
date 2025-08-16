"use client";

import { useState } from "react";
import { ChevronDown, Calendar, DollarSign, Star, TrendingUp } from "lucide-react";

export type SortOption = 
  | 'relevance'
  | 'date-asc'
  | 'date-desc'
  | 'price-asc'
  | 'price-desc'
  | 'popularity'
  | 'newest';

interface SearchSortProps {
  sortBy: SortOption;
  onSortChange: (sort: SortOption) => void;
  resultCount?: number;
}

const SORT_OPTIONS: { value: SortOption; label: string; icon: React.ReactNode }[] = [
  {
    value: 'relevance',
    label: 'Liên quan nhất',
    icon: <Star className="w-4 h-4" />
  },
  {
    value: 'date-asc',
    label: 'Sớm nhất',
    icon: <Calendar className="w-4 h-4" />
  },
  {
    value: 'date-desc', 
    label: 'Muộn nhất',
    icon: <Calendar className="w-4 h-4" />
  },
  {
    value: 'price-asc',
    label: 'Giá thấp đến cao',
    icon: <DollarSign className="w-4 h-4" />
  },
  {
    value: 'price-desc',
    label: 'Giá cao đến thấp', 
    icon: <DollarSign className="w-4 h-4" />
  },
  {
    value: 'popularity',
    label: 'Phổ biến nhất',
    icon: <TrendingUp className="w-4 h-4" />
  },
  {
    value: 'newest',
    label: 'Mới nhất',
    icon: <Calendar className="w-4 h-4" />
  }
];

export function SearchSort({ sortBy, onSortChange, resultCount }: SearchSortProps) {
  const [isOpen, setIsOpen] = useState(false);
  
  const currentSort = SORT_OPTIONS.find(option => option.value === sortBy);

  return (
    <div className="flex items-center justify-between py-4">
      {/* Result Count */}
      <div className="text-sm text-gray-600">
        {resultCount !== undefined && (
          <span>
            Tìm thấy <strong>{resultCount.toLocaleString()}</strong> sự kiện
          </span>
        )}
      </div>

      {/* Sort Dropdown */}
      <div className="relative">
        <button
          onClick={() => setIsOpen(!isOpen)}
          className="flex items-center space-x-2 px-4 py-2 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
        >
          {currentSort?.icon}
          <span className="text-sm font-medium text-gray-700">
            Sắp xếp: {currentSort?.label}
          </span>
          <ChevronDown className={`w-4 h-4 text-gray-400 transition-transform ${
            isOpen ? 'rotate-180' : ''
          }`} />
        </button>

        {/* Dropdown Menu */}
        {isOpen && (
          <>
            {/* Backdrop */}
            <div 
              className="fixed inset-0 z-10" 
              onClick={() => setIsOpen(false)}
            />
            
            {/* Menu */}
            <div className="absolute top-full right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-20">
              <div className="py-1">
                {SORT_OPTIONS.map((option) => (
                  <button
                    key={option.value}
                    onClick={() => {
                      onSortChange(option.value);
                      setIsOpen(false);
                    }}
                    className={`w-full flex items-center space-x-3 px-4 py-2 text-sm text-left hover:bg-gray-50 transition-colors ${
                      sortBy === option.value 
                        ? 'bg-blue-50 text-blue-700 font-medium' 
                        : 'text-gray-700'
                    }`}
                  >
                    {option.icon}
                    <span>{option.label}</span>
                    {sortBy === option.value && (
                      <div className="ml-auto w-2 h-2 bg-blue-600 rounded-full" />
                    )}
                  </button>
                ))}
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
