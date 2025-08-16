"use client";

import { useState, useEffect } from "react";
import { Search, Star, StarOff, X, Clock } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { SearchFilters } from "./SearchFilters";
import type { SortOption } from "./SearchSort";

interface SavedSearch {
  id: string;
  name: string;
  filters: SearchFilters;
  sortBy: SortOption;
  savedAt: string;
  lastUsed?: string;
}

interface SavedSearchesProps {
  currentFilters: SearchFilters;
  currentSort: SortOption;
  onApplySearch: (filters: SearchFilters, sort: SortOption) => void;
}

const STORAGE_KEY = 'eventTicketing_savedSearches';
const RECENT_SEARCHES_KEY = 'eventTicketing_recentSearches';

export function SavedSearches({ 
  currentFilters, 
  currentSort, 
  onApplySearch 
}: SavedSearchesProps) {
  const [savedSearches, setSavedSearches] = useState<SavedSearch[]>([]);
  const [recentSearches, setRecentSearches] = useState<string[]>([]);
  const [showSaveModal, setShowSaveModal] = useState(false);
  const [searchName, setSearchName] = useState('');

  // Load saved searches from localStorage
  useEffect(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        setSavedSearches(JSON.parse(saved));
      }
      
      const recent = localStorage.getItem(RECENT_SEARCHES_KEY);
      if (recent) {
        setRecentSearches(JSON.parse(recent));
      }
    } catch (error) {
      console.error('Error loading saved searches:', error);
    }
  }, []);

  // Save current search
  const saveCurrentSearch = () => {
    if (!searchName.trim()) return;

    const newSearch: SavedSearch = {
      id: Date.now().toString(),
      name: searchName.trim(),
      filters: currentFilters,
      sortBy: currentSort,
      savedAt: new Date().toISOString()
    };

    const updatedSearches = [...savedSearches, newSearch];
    setSavedSearches(updatedSearches);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updatedSearches));
    
    setSearchName('');
    setShowSaveModal(false);
  };

  // Delete saved search
  const deleteSavedSearch = (id: string) => {
    const updatedSearches = savedSearches.filter(s => s.id !== id);
    setSavedSearches(updatedSearches);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updatedSearches));
  };

  // Apply saved search
  const applySavedSearch = (search: SavedSearch) => {
    onApplySearch(search.filters, search.sortBy);
    
    // Update last used
    const updatedSearches = savedSearches.map(s => 
      s.id === search.id 
        ? { ...s, lastUsed: new Date().toISOString() }
        : s
    );
    setSavedSearches(updatedSearches);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updatedSearches));
  };

  // Add to recent searches
  const addToRecentSearches = (query: string) => {
    if (!query.trim()) return;
    
    const updated = [query, ...recentSearches.filter(q => q !== query)].slice(0, 5);
    setRecentSearches(updated);
    localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(updated));
  };

  // Apply recent search
  const applyRecentSearch = (query: string) => {
    onApplySearch({ ...currentFilters, query }, currentSort);
  };

  const hasActiveFilters = Object.entries(currentFilters).some(([key, value]) => 
    key !== 'query' && value !== undefined && value !== '' && value !== null
  );

  const canSaveCurrentSearch = currentFilters.query || hasActiveFilters;

  return (
    <div className="space-y-4">
      {/* Save Current Search */}
      {canSaveCurrentSearch && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="font-medium text-blue-900">Lưu tìm kiếm này</h4>
              <p className="text-sm text-blue-700">Lưu bộ lọc hiện tại để sử dụng lại sau</p>
            </div>
            <Button
              size="sm"
              onClick={() => setShowSaveModal(true)}
              className="bg-blue-600 hover:bg-blue-700"
            >
              <Star className="w-4 h-4 mr-1" />
              Lưu
            </Button>
          </div>
        </div>
      )}

      {/* Recent Searches */}
      {recentSearches.length > 0 && (
        <div>
          <h4 className="font-medium text-gray-900 mb-3 flex items-center">
            <Clock className="w-4 h-4 mr-2" />
            Tìm kiếm gần đây
          </h4>
          <div className="flex flex-wrap gap-2">
            {recentSearches.map((query, index) => (
              <button
                key={index}
                onClick={() => applyRecentSearch(query)}
                className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm hover:bg-gray-200 transition-colors"
              >
                {query}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Saved Searches */}
      {savedSearches.length > 0 && (
        <div>
          <h4 className="font-medium text-gray-900 mb-3 flex items-center">
            <Star className="w-4 h-4 mr-2" />
            Tìm kiếm đã lưu
          </h4>
          <div className="space-y-2">
            {savedSearches.map((search) => (
              <div
                key={search.id}
                className="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <div className="flex-1">
                  <div className="flex items-center space-x-2">
                    <h5 className="font-medium text-gray-900">{search.name}</h5>
                    <span className="text-xs text-gray-500">
                      {new Date(search.savedAt).toLocaleDateString('vi-VN')}
                    </span>
                  </div>
                  <p className="text-sm text-gray-600">
                    {search.filters.query && `"${search.filters.query}"`}
                    {search.filters.category && ` • ${search.filters.category}`}
                    {search.filters.location && ` • ${search.filters.location}`}
                  </p>
                </div>
                <div className="flex items-center space-x-2">
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => applySavedSearch(search)}
                  >
                    <Search className="w-4 h-4" />
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => deleteSavedSearch(search.id)}
                    className="text-red-600 hover:text-red-700"
                  >
                    <X className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Save Modal */}
      {showSaveModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Lưu tìm kiếm
            </h3>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Tên tìm kiếm
              </label>
              <input
                type="text"
                value={searchName}
                onChange={(e) => setSearchName(e.target.value)}
                placeholder="VD: Sự kiện âm nhạc TP.HCM"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                autoFocus
              />
            </div>
            <div className="flex justify-end space-x-3">
              <Button
                variant="ghost"
                onClick={() => setShowSaveModal(false)}
              >
                Hủy
              </Button>
              <Button
                onClick={saveCurrentSearch}
                disabled={!searchName.trim()}
              >
                Lưu
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
