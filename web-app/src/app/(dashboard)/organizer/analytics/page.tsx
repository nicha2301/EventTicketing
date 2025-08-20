"use client";

import { Suspense, useEffect, useState } from "react";
import Link from "next/link";
import { useKpiDashboard, useEventPerformance as useEventPerformanceHook, useDailyRevenue as useDailyRevenueHook, useSalesByType, usePaymentMethodsAnalysis as usePaymentMethodsHook, useAttendeeAnalytics as useAttendeesHook, useRegistrationTimeline as useTimelineHook } from "@/hooks/useAnalytics";
import { useAuthStore } from "@/store/auth";
import { useAuthHydration } from "@/hooks/useAuthHydration";
import { PageLoading, ErrorState } from "@/components/ui/LoadingSpinner";
import { useOrganizerEvents } from "@/hooks/useFeaturedEvents";
import { toast } from "sonner";
import { Line, Bar, Doughnut } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Tooltip,
  Legend,
  Filler,
} from "chart.js";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Tooltip,
  Legend,
  Filler,
);

function Stat({ label, value, icon, color = "blue" }: { 
  label: string; 
  value: string | number; 
  icon?: React.ReactNode;
  color?: "blue" | "green" | "purple" | "yellow" | "red";
}) {
  const colorClasses = {
    blue: "bg-blue-100 text-blue-600",
    green: "bg-green-100 text-green-600", 
    purple: "bg-purple-100 text-purple-600",
    yellow: "bg-yellow-100 text-yellow-600",
    red: "bg-red-100 text-red-600"
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <div className="flex items-center">
        <div className="flex-shrink-0">
          <div className={`w-10 h-10 ${colorClasses[color]} rounded-lg flex items-center justify-center`}>
            {icon}
          </div>
        </div>
        <div className="ml-4">
          <p className="text-sm font-medium text-gray-500">{label}</p>
          <p className="text-2xl font-bold text-gray-900">{value}</p>
        </div>
      </div>
    </div>
  );
}

function AnalyticsContent() {
  const { currentUser } = useAuthStore();
  const isHydrated = useAuthHydration();
  const organizerId = currentUser?.id ?? "";
  const { data: eventsPage } = useOrganizerEvents(0, 50);
  const [eventId, setEventId] = useState("");

  useEffect(() => {
    const firstId = (eventsPage as any)?.content?.[0]?.id as string | undefined;
    if (isHydrated && !eventId && firstId) {
      setEventId(firstId);
    }
  }, [isHydrated, eventsPage, eventId]);

  const kpi = useKpiDashboard(eventId);
  const perf = useEventPerformanceHook(eventId);
  const revenue = useDailyRevenueHook(eventId);
  const salesByType = useSalesByType(eventId);
  const paymentMethods = usePaymentMethodsHook(eventId);
  const attendees = useAttendeesHook(eventId);
  const timeline = useTimelineHook(eventId);

  const {
    data: kpiData,
    error: kpiError,
    isLoading: kpiLoading,
  } = kpi;
  const {
    data: perfData,
    error: performanceError,
    isLoading: performanceLoading,
  } = perf;
  const {
    data: revenueData,
    error: revenueError,
    isLoading: revenueLoading,
  } = revenue;
  const {
    data: salesByTypeData,
    error: salesError,
    isLoading: salesLoading,
  } = salesByType;
  const {
    data: paymentMethodsData,
    error: paymentError,
    isLoading: paymentLoading,
  } = paymentMethods;
  const {
    data: attendeesData,
    error: attendeesError,
    isLoading: attendeesLoading,
  } = attendees;
  const {
    data: timelineData,
    error: timelineError,
    isLoading: timelineLoading,
  } = timeline;

  // Add error handling for analytics data
  useEffect(() => {
    if (kpiError) {
      toast.error("Không thể tải dữ liệu KPI");
    }
    if (performanceError) {
      toast.error("Không thể tải dữ liệu hiệu suất");
    }
    if (revenueError) {
      toast.error("Không thể tải dữ liệu doanh thu");
    }
    if (salesError) {
      toast.error("Không thể tải dữ liệu bán hàng");
    }
    if (paymentError) {
      toast.error("Không thể tải dữ liệu thanh toán");
    }
    if (attendeesError) {
      toast.error("Không thể tải dữ liệu người tham gia");
    }
    if (timelineError) {
      toast.error("Không thể tải dữ liệu timeline");
    }
  }, [kpiError, performanceError, revenueError, salesError, paymentError, attendeesError, timelineError]);

  // Type-safe data access
  const kpiStats = kpiData as any;
  const perfStats = perfData as any;
  const revenueStats = revenueData as any;
  const salesStats = salesByTypeData as any;
  const paymentStats = paymentMethodsData as any;
  const attendeesStats = attendeesData as any;
  const timelineStats = timelineData as any;

  if (!isHydrated) {
    return <PageLoading message="Đang tải dữ liệu tổ chức..." />
  }

  // Data mapping functions for charts
  const mapTicketSalesData = (data: any) => {
    if (!data?.ticketTypeData) return { labels: [], values: [], revenues: [] };
    
    const labels: string[] = [];
    const values: number[] = [];
    const revenues: number[] = [];
    
    Object.entries(data.ticketTypeData).forEach(([type, info]: [string, any]) => {
      labels.push(type);
      values.push(info.count || 0);
      revenues.push(info.revenue || 0);
    });
    
    return { labels, values, revenues };
  };

  const mapPaymentMethodsData = (data: any) => {
    if (!data?.paymentMethods) return { labels: [], values: [], amounts: [] };
    
    const labels: string[] = [];
    const values: number[] = [];
    const amounts: number[] = [];
    
    Object.entries(data.paymentMethods).forEach(([method, info]: [string, any]) => {
      labels.push(method.toUpperCase());
      values.push(info.transactionCount || 0);
      amounts.push(info.totalAmount || 0);
    });
    
    return { labels, values, amounts };
  };

  const mapTimelineData = (data: any) => {
    if (!data || typeof data !== 'object') return { labels: [], values: [] };
    
    const entries = Object.entries(data);
    
    const dateEntries = entries.filter(([key]) => {
      return /^\d{4}-\d{2}-\d{2}$/.test(key);
    });
    
    if (dateEntries.length === 0) {
      return { labels: ['Chưa có dữ liệu'], values: [0] };
    }
    
    dateEntries.sort(([a], [b]) => a.localeCompare(b));
    
    return {
      labels: dateEntries.map(([date]) => {
        try {
          const d = new Date(date + 'T00:00:00'); 
          if (isNaN(d.getTime())) return date; 
          return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
        } catch {
          return date;
        }
      }),
      values: dateEntries.map(([, count]) => {
        const value = Number(count);
        return isNaN(value) ? 0 : value;
      })
    };
  };

  const mapRevenueData = (data: any) => {
    if (!data || typeof data !== 'object') return { labels: [], values: [] };

    const daily = data.dailyRevenue && typeof data.dailyRevenue === 'object' ? data.dailyRevenue : data;

    const entries = Object.entries(daily);

    const isPlaceholderData = entries.some(([key]) => key.startsWith('additionalProp'));
    if (isPlaceholderData) return { labels: ['Chưa có dữ liệu'], values: [0] };

    const dateEntries = entries.filter(([key]) => /^\d{4}-\d{2}-\d{2}$/.test(key));
    if (dateEntries.length === 0) return { labels: ['Chưa có dữ liệu'], values: [0] };

    dateEntries.sort(([a], [b]) => a.localeCompare(b));

    return {
      labels: dateEntries.map(([date]) => {
        try {
          const d = new Date(date + 'T00:00:00');
          if (isNaN(d.getTime())) return date;
          return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
        } catch {
          return date;
        }
      }),
      values: dateEntries.map(([, amount]) => {
        const value = Number(amount);
        return isNaN(value) ? 0 : value;
      })
    };
  };

  const ticketSalesData = mapTicketSalesData(salesStats);
  const paymentData = mapPaymentMethodsData(paymentStats);
  const timelineChartData = mapTimelineData(timelineStats);
  const revenueChartData = mapRevenueData(revenueStats);
  const revenueSummary = {
    totalRevenue: Number(revenueStats?.totalRevenue) || 0,
    currencyCode: revenueStats?.currencyCode || 'VND',
    startDate: revenueStats?.startDate,
    endDate: revenueStats?.endDate,
  };

  const palette = [
    "#3b82f6", "#22c55e", "#a855f7", "#f59e0b", "#ef4444", "#06b6d4", "#84cc16", "#f43f5e",
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="container-page py-8">
        {/* Header Section */}
        <div className="mb-8">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
            <div className="mb-4 sm:mb-0">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">Analytics Dashboard</h1>
              <p className="text-gray-600 text-lg">Phân tích hiệu suất và doanh thu sự kiện</p>
            </div>
            <div className="flex items-center space-x-3">
              <Link 
                href="/organizer/events"
                className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
              >
                <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                Quay lại
              </Link>
            </div>
          </div>
        </div>

        {/* Event Selector */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-8">
          <div className="flex flex-col sm:flex-row sm:items-end gap-4">
            <div className="flex-1">
              <label className="block text-sm font-medium text-gray-700 mb-2">Chọn sự kiện để phân tích</label>
              <select 
                className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200"
                value={eventId} 
                onChange={(e) => setEventId(e.target.value)}
              >
                <option value="">Chọn sự kiện...</option>
                {(eventsPage?.content ?? []).map((e: any) => (
                  <option key={e.id} value={e.id}>{e.title}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {!eventId ? (
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center">
            <svg className="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            <h3 className="text-lg font-medium text-gray-900 mb-2">Chọn sự kiện để xem analytics</h3>
            <p className="text-gray-500">Vui lòng chọn một sự kiện từ danh sách trên để xem các chỉ số và phân tích chi tiết.</p>
          </div>
        ) : (
          <>
            {/* KPI Stats */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              {perfStats ? (
                <>
                  <Stat 
                    label="Tổng doanh thu" 
                    value={`${(perfStats.totalRevenue ?? 0).toLocaleString('vi-VN')} VND`}
                    icon={
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                      </svg>
                    }
                    color="purple"
                  />
                  <Stat 
                    label="Vé đã bán" 
                    value={perfStats.ticketsSold || 0}
                    icon={
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
                      </svg>
                    }
                    color="blue"
                  />
                  <Stat 
                    label="Tỷ lệ tham dự" 
                    value={`${perfStats.attendanceRate || 0}%`}
                    icon={
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                      </svg>
                    }
                    color="green"
                  />
                  <Stat 
                    label="Đánh giá trung bình" 
                    value={`${perfStats.averageRating || 0}/5`}
                    icon={
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                      </svg>
                    }
                    color="yellow"
                  />
                </>
              ) : (
                <div className="col-span-full text-center py-12">
                  <p className="text-gray-500">Đang tải dữ liệu KPI...</p>
                </div>
              )}
            </div>

            {/* Sales and Revenue Charts */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
              {/* Ticket Sales by Type */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <svg className="w-5 h-5 mr-2 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2m0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                  Bán vé theo loại
                </h3>
                <div className="bg-gray-50 rounded-lg p-4">
                  <Bar
                    data={{
                      labels: ticketSalesData.labels,
                      datasets: [{
                        label: "Số vé đã bán",
                        data: ticketSalesData.values,
                        backgroundColor: ticketSalesData.labels.map((_, i) => palette[i % palette.length]),
                      }],
                    }}
                    options={{ 
                      responsive: true, 
                      plugins: { 
                        legend: { display: true },
                        tooltip: {
                          callbacks: {
                            afterLabel: function(context) {
                              const index = context.dataIndex;
                              const revenue = ticketSalesData.revenues[index];
                              return `Doanh thu: ${revenue?.toLocaleString('vi-VN')} VND`;
                            }
                          }
                        }
                      } 
                    }}
                  />
                </div>
                {salesStats?.totalSold && (
                  <div className="mt-4 p-4 bg-blue-50 rounded-lg">
                    <div className="flex justify-between items-center">
                      <span className="text-sm font-medium text-blue-900">Tổng vé đã bán:</span>
                      <span className="text-lg font-bold text-blue-900">{salesStats.totalSold}</span>
                    </div>
                    <div className="flex justify-between items-center mt-2">
                      <span className="text-sm font-medium text-blue-900">Tổng doanh thu:</span>
                      <span className="text-lg font-bold text-blue-900">{salesStats.totalRevenue?.toLocaleString('vi-VN')} VND</span>
                    </div>
                  </div>
                )}
              </div>

              {/* Revenue Timeline */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <svg className="w-5 h-5 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z" />
                  </svg>
                  Doanh thu theo ngày
                  <span className="ml-2 text-sm font-normal text-gray-500">({revenueSummary.currencyCode})</span>
                </h3>
                <div className="text-sm text-gray-500 -mt-2 mb-3">
                  {revenueSummary.startDate && revenueSummary.endDate ? (
                    <span>
                      Khoảng thời gian: {new Date(revenueSummary.startDate + 'T00:00:00').toLocaleDateString('vi-VN')} → {new Date(revenueSummary.endDate + 'T00:00:00').toLocaleDateString('vi-VN')}
                    </span>
                  ) : null}
                </div>
                <div className="bg-gray-50 rounded-lg p-4">
                  <Line
                    data={{
                      labels: revenueChartData.labels,
                      datasets: [{
                        label: "Doanh thu (VND)",
                        data: revenueChartData.values,
                        fill: true,
                        borderColor: "#22c55e",
                        backgroundColor: "rgba(34, 197, 94, 0.2)",
                        tension: 0.3,
                      }],
                    }}
                    options={{ 
                      responsive: true, 
                      plugins: { 
                        legend: { display: true },
                        tooltip: {
                          callbacks: {
                            label: function(context) {
                              return `Doanh thu: ${context.parsed.y.toLocaleString('vi-VN')} ${revenueSummary.currencyCode}`;
                            }
                          }
                        }
                      } 
                    }}
                  />
                </div>
                <div className="mt-4 p-3 bg-green-50 rounded-lg flex justify-between items-center">
                  <span className="text-sm font-medium text-green-900">Tổng doanh thu:</span>
                  <span className="text-lg font-bold text-green-900">{revenueSummary.totalRevenue.toLocaleString('vi-VN')} {revenueSummary.currencyCode}</span>
                </div>
              </div>
            </div>

            {/* Payment Methods and Registration Timeline */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
              {/* Payment Methods */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <svg className="w-5 h-5 mr-2 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                  </svg>
                  Phương thức thanh toán
                </h3>
                <div className="bg-gray-50 rounded-lg p-4">
                  <Doughnut
                    data={{
                      labels: paymentData.labels,
                      datasets: [{
                        label: "Số giao dịch",
                        data: paymentData.values,
                        backgroundColor: paymentData.labels.map((_, i) => palette[i % palette.length]),
                      }],
                    }}
                    options={{ 
                      responsive: true, 
                      plugins: { 
                        legend: { position: 'bottom' as const },
                        tooltip: {
                          callbacks: {
                            afterLabel: function(context) {
                              const index = context.dataIndex;
                              const amount = paymentData.amounts[index];
                              return `Tổng tiền: ${amount?.toLocaleString('vi-VN')} VND`;
                            }
                          }
                        }
                      } 
                    }}
                  />
                </div>
                {paymentStats?.totalTransactions && (
                  <div className="mt-4 p-4 bg-purple-50 rounded-lg">
                    <div className="flex justify-between items-center">
                      <span className="text-sm font-medium text-purple-900">Tổng giao dịch:</span>
                      <span className="text-lg font-bold text-purple-900">{paymentStats.totalTransactions}</span>
                    </div>
                    <div className="flex justify-between items-center mt-2">
                      <span className="text-sm font-medium text-purple-900">Tổng tiền:</span>
                      <span className="text-lg font-bold text-purple-900">{paymentStats.totalAmount?.toLocaleString('vi-VN')} VND</span>
                    </div>
                  </div>
                )}
              </div>

              {/* Registration Timeline */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <svg className="w-5 h-5 mr-2 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Timeline đăng ký
                </h3>
                <div className="bg-gray-50 rounded-lg p-4">
                  <Line
                    data={{
                      labels: timelineChartData.labels,
                      datasets: [{
                        label: "Số đăng ký",
                        data: timelineChartData.values,
                        borderColor: "#ef4444",
                        backgroundColor: "rgba(239, 68, 68, 0.2)",
                        tension: 0.3,
                        fill: true,
                      }],
                    }}
                    options={{ 
                      responsive: true, 
                      plugins: { 
                        legend: { display: true },
                        tooltip: {
                          callbacks: {
                            label: function(context) {
                              return `Đăng ký: ${context.parsed.y} người`;
                            }
                          }
                        }
                      } 
                    }}
                  />
                </div>
              </div>
            </div>

            {/* Additional Performance Metrics */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
              {/* ROI and Profit */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <svg className="w-5 h-5 mr-2 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                  </svg>
                  Hiệu suất tài chính
                </h3>
                <div className="space-y-4">
                  <div className="flex justify-between items-center p-3 bg-indigo-50 rounded-lg">
                    <span className="text-sm font-medium text-indigo-900">ROI:</span>
                    <span className="text-lg font-bold text-indigo-900">{perfStats?.roi || 0}%</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-green-50 rounded-lg">
                    <span className="text-sm font-medium text-green-900">Tỷ suất lợi nhuận:</span>
                    <span className="text-lg font-bold text-green-900">{perfStats?.profitMargin || 0}%</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-yellow-50 rounded-lg">
                    <span className="text-sm font-medium text-yellow-900">NPS Score:</span>
                    <span className="text-lg font-bold text-yellow-900">{perfStats?.npsScore || 0}</span>
                  </div>
                </div>
              </div>

              {/* Attendance Analytics */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <svg className="w-5 h-5 mr-2 text-teal-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                  Phân tích tham dự
                </h3>
                <div className="space-y-4">
                  <div className="flex justify-between items-center p-3 bg-teal-50 rounded-lg">
                    <span className="text-sm font-medium text-teal-900">Đã đăng ký:</span>
                    <span className="text-lg font-bold text-teal-900">{attendeesStats?.totalRegistered || 0}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
                    <span className="text-sm font-medium text-blue-900">Đã check-in:</span>
                    <span className="text-lg font-bold text-blue-900">{attendeesStats?.totalCheckedIn || 0}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-orange-50 rounded-lg">
                    <span className="text-sm font-medium text-orange-900">Tỷ lệ tham dự:</span>
                    <span className="text-lg font-bold text-orange-900">
                      {attendeesStats?.totalRegistered ? 
                        `${Math.round((attendeesStats.totalCheckedIn / attendeesStats.totalRegistered) * 100)}%` : 
                        '0%'
                      }
                    </span>
                  </div>
                </div>
              </div>

              {/* Sales Performance */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <svg className="w-5 h-5 mr-2 text-pink-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2m0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                  Hiệu suất bán hàng
                </h3>
                <div className="space-y-4">
                  <div className="flex justify-between items-center p-3 bg-pink-50 rounded-lg">
                    <span className="text-sm font-medium text-pink-900">Tỷ lệ bán vé:</span>
                    <span className="text-lg font-bold text-pink-900">{perfStats?.ticketSalesRate || 0}%</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-purple-50 rounded-lg">
                    <span className="text-sm font-medium text-purple-900">Mục tiêu vé:</span>
                    <span className="text-lg font-bold text-purple-900">{perfStats?.ticketsTarget || 'N/A'}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-red-50 rounded-lg">
                    <span className="text-sm font-medium text-red-900">Mục tiêu doanh thu:</span>
                    <span className="text-lg font-bold text-red-900">
                      {perfStats?.revenueTarget ? 
                        `${perfStats.revenueTarget.toLocaleString('vi-VN')} VND` : 
                        'N/A'
                      }
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default function AnalyticsPage() {
  return (
    <Suspense fallback={<section className="py-10"><div className="container-page">Đang tải...</div></section>}>
      <AnalyticsContent />
    </Suspense>
  );
}


