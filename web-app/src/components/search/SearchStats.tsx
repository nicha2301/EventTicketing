"use client";

import { TrendingUp, Users, Star, MapPin, Calendar, DollarSign } from "lucide-react";

interface SearchStats {
  totalEvents: number;
  averagePrice: number;
  topCategory: string;
  topLocation: string;
  upcomingEvents: number;
  freeEvents: number;
}

interface SearchStatsProps {
  stats: SearchStats;
  isLoading?: boolean;
}

export function SearchStats({ stats, isLoading }: SearchStatsProps) {
  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border p-6">
        <div className="animate-pulse">
          <div className="h-4 bg-gray-300 rounded w-1/3 mb-4"></div>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="text-center">
                <div className="h-8 bg-gray-300 rounded w-12 mx-auto mb-2"></div>
                <div className="h-3 bg-gray-300 rounded w-16 mx-auto"></div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  const statsItems = [
    {
      label: "Tổng sự kiện",
      value: stats.totalEvents.toLocaleString(),
      icon: Calendar,
      color: "text-blue-600",
      bgColor: "bg-blue-100",
    },
    {
      label: "Sự kiện miễn phí",
      value: stats.freeEvents.toLocaleString(),
      icon: DollarSign,
      color: "text-green-600",
      bgColor: "bg-green-100",
    },
    {
      label: "Giá trung bình",
      value: `${stats.averagePrice.toLocaleString()}đ`,
      icon: TrendingUp,
      color: "text-purple-600",
      bgColor: "bg-purple-100",
    },
    {
      label: "Danh mục phổ biến",
      value: stats.topCategory,
      icon: Star,
      color: "text-yellow-600",
      bgColor: "bg-yellow-100",
    },
    {
      label: "Địa điểm phổ biến",
      value: stats.topLocation,
      icon: MapPin,
      color: "text-red-600",
      bgColor: "bg-red-100",
    },
    {
      label: "Sắp diễn ra",
      value: stats.upcomingEvents.toLocaleString(),
      icon: Users,
      color: "text-indigo-600",
      bgColor: "bg-indigo-100",
    },
  ];

  return (
    <div className="bg-white rounded-lg shadow-sm border p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
        <TrendingUp className="w-5 h-5 mr-2 text-blue-600" />
        Thống kê tìm kiếm
      </h3>
      
      <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
        {statsItems.map((item, index) => (
          <div
            key={index}
            className="text-center p-3 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div className={`inline-flex items-center justify-center w-10 h-10 ${item.bgColor} rounded-full mb-2`}>
              <item.icon className={`w-5 h-5 ${item.color}`} />
            </div>
            <div className="text-lg font-bold text-gray-900 mb-1">
              {item.value}
            </div>
            <div className="text-xs text-gray-600">
              {item.label}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
