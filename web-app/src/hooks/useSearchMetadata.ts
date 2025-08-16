import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getAllCategories, getAllLocations } from '@/lib/api/generated/client';

// Hook to get all categories and locations for mapping
export function useSearchMetadata() {
  const categoriesQuery = useQuery({
    queryKey: ['all-categories'],
    queryFn: () => getAllCategories({ pageable: { page: 0, size: 100 } }),
    staleTime: 5 * 60 * 1000,
    retry: 1,
    retryOnMount: false, 
  });

  const locationsQuery = useQuery({
    queryKey: ['all-locations'], 
    queryFn: () => getAllLocations({ pageable: { page: 0, size: 100 } }),
    staleTime: 5 * 60 * 1000,
    retry: 1, 
    retryOnMount: false,
  });

  // Create lookup maps from API data
  const categoryMap = useMemo(() => {
    const categories = (categoriesQuery.data?.data as any)?.content || [];
    const nameToId = new Map<string, string>();
    const idToName = new Map<string, string>();
    
    categories.forEach((cat: any) => {
      if (cat.name && cat.id) {
        nameToId.set(cat.name, cat.id);
        idToName.set(cat.id, cat.name);
      }
    });
    
    return { nameToId, idToName, categories };
  }, [categoriesQuery.data]);

  const locationMap = useMemo(() => {
    const locations = (locationsQuery.data?.data as any)?.content || [];
    const nameToId = new Map<string, string>();
    const idToName = new Map<string, string>();
    
    locations.forEach((loc: any) => {
      if (loc.name && loc.id) {
        nameToId.set(loc.name, loc.id);
        idToName.set(loc.id, loc.name);
      }
      if (loc.city && loc.city !== loc.name && loc.id) {
        nameToId.set(loc.city, loc.id);
      }
    });
    
    return { nameToId, idToName, locations };
  }, [locationsQuery.data]);

  return {
    categories: categoryMap.categories,
    locations: locationMap.locations,
    isLoading: categoriesQuery.isLoading || locationsQuery.isLoading,
    error: categoriesQuery.error || locationsQuery.error,
    getCategoryId: (name: string) => categoryMap.nameToId.get(name),
    getCategoryName: (id: string) => categoryMap.idToName.get(id),
    getLocationId: (name: string) => locationMap.nameToId.get(name),
    getLocationName: (id: string) => locationMap.idToName.get(id),
  };

}
