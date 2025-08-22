"use client";

import { PageLoading } from "@/components/ui/LoadingSpinner";
import { useAuthHydration } from "@/hooks/useAuthHydration";
import { useOrganizerEvents } from "@/hooks/useFeaturedEvents";
import { useEventReports, useExportExcel, useExportPdf, useGenerateAttendanceReport, useGenerateRevenueReport, useGenerateSalesReport, useMyReports, useReportById, useReportsByType } from "@/hooks/useReports";
import { organizerReportSchema, type OrganizerReportInput } from "@/lib/validation/organizer/events";
import { useAuthStore } from "@/store/auth";
import { zodResolver } from "@hookform/resolvers/zod";
import Link from "next/link";
import { Suspense, useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

function downloadBlob(data: Blob, filename: string) {
  const url = window.URL.createObjectURL(data);
  const a = document.createElement("a");
  a.href = url; a.download = filename; a.click();
  window.URL.revokeObjectURL(url);
}

function ReportsContent() {
  const isHydrated = useAuthHydration();
  const { data: eventsPage } = useOrganizerEvents(0, 50);
  const { register, formState: { errors }, watch, setValue } = useForm<OrganizerReportInput>({ resolver: zodResolver(organizerReportSchema) });
  const eventId = watch("eventId") || "";
  const fromDate = watch("fromDate") || "";
  const toDate = watch("toDate") || "";
  const [lastReportId, setLastReportId] = useState<number | null>(null);
  const [listMode, setListMode] = useState<'mine' | 'event' | 'type'>('mine');
  const [reportType, setReportType] = useState<'REVENUE' | 'SALES' | 'ATTENDANCE'>('REVENUE');
  const [listPage, setListPage] = useState<number>(0);
  const listSize = 10;
  const myReports = useMyReports(listPage, listSize);
  const eventReports = useEventReports(eventId, listPage, listSize);
  const typeReports = useReportsByType(reportType, listPage, listSize);
  const { data: lastReportDetail } = useReportById(lastReportId ?? undefined);

  const onSuccess = (res: any) => {
      const id = res.data?.data?.id ?? res.data?.data; 
      if (typeof id === "number") setLastReportId(id);
      toast.success("Báo cáo đã được tạo thành công!");
  };
  const onError = (error: any) => toast.error(error?.message || "Không thể tạo báo cáo");

  const salesMut = useGenerateSalesReport();
  const revenueMut = useGenerateRevenueReport();
  const attendanceMut = useGenerateAttendanceReport();

  useEffect(() => {
    if (salesMut.isSuccess && salesMut.data) onSuccess(salesMut.data);
  }, [salesMut.isSuccess, salesMut.data]);
  useEffect(() => {
    if (revenueMut.isSuccess && revenueMut.data) onSuccess(revenueMut.data);
  }, [revenueMut.isSuccess, revenueMut.data]);
  useEffect(() => {
    if (attendanceMut.isSuccess && attendanceMut.data) onSuccess(attendanceMut.data);
  }, [attendanceMut.isSuccess, attendanceMut.data]);
  useEffect(() => {
    if (salesMut.isError) onError(salesMut.error as any);
    if (revenueMut.isError) onError(revenueMut.error as any);
    if (attendanceMut.isError) onError(attendanceMut.error as any);
  }, [salesMut.isError, revenueMut.isError, attendanceMut.isError]);

  const exportPdfMut = useExportPdf();
  const exportExcelMut = useExportExcel();

  const doExportPdf = async () => {
    if (!lastReportId) {
      toast.error("Vui lòng tạo báo cáo trước khi xuất");
      return;
    }
    try {
      const res = await exportPdfMut.mutateAsync(lastReportId);
      if (res instanceof Blob) {
        downloadBlob(res, `report-${lastReportId}.pdf`);
      } else {
        downloadBlob(new Blob([res]), `report-${lastReportId}.pdf`);
      }
      toast.success("Báo cáo PDF đã được xuất thành công!");
    } catch (error: any) {
      console.error('PDF Export Error:', error);
      toast.error(error?.message || "Không thể xuất báo cáo PDF");
    }
  };
  
  const doExportExcel = async () => {
    if (!lastReportId) {
      toast.error("Vui lòng tạo báo cáo trước khi xuất");
      return;
    }
    try {
      const res = await exportExcelMut.mutateAsync(lastReportId);
      if (res instanceof Blob) {
        downloadBlob(res, `report-${lastReportId}.xlsx`);
      } else {
        downloadBlob(new Blob([res]), `report-${lastReportId}.xlsx`);
      }
      toast.success("Báo cáo Excel đã được xuất thành công!");
    } catch (error: any) {
      console.error('Excel Export Error:', error);
      toast.error(error?.message || "Không thể xuất báo cáo Excel");
    }
  };

  const handleExportPdf = async (reportId: number) => {
    try {
      const res = await exportPdfMut.mutateAsync(reportId);
      if (res instanceof Blob) {
        downloadBlob(res, `report-${reportId}.pdf`);
      } else {
        downloadBlob(new Blob([res]), `report-${reportId}.pdf`);
      }
      toast.success("Báo cáo PDF đã được xuất thành công!");
    } catch (error: any) {
      console.error('PDF Export Error:', error);
      toast.error(error?.message || "Không thể xuất báo cáo PDF");
    }
  };

  const handleExportExcel = async (reportId: number) => {
    try {
      const res = await exportExcelMut.mutateAsync(reportId);
      if (res instanceof Blob) {
        downloadBlob(res, `report-${reportId}.xlsx`);
      } else {
        downloadBlob(new Blob([res]), `report-${reportId}.xlsx`);
      }
      toast.success("Báo cáo Excel đã được xuất thành công!");
    } catch (error: any) {
      console.error('Excel Export Error:', error);
      toast.error(error?.message || "Không thể xuất báo cáo Excel");
    }
  };

  const disabled = !eventId || !fromDate || !toDate;
  const baseInput = { eventId, name: "", startDate: fromDate, endDate: toDate };

  useEffect(() => {
    if (!eventId) return;
    const evt = (eventsPage as any)?.content?.find((e: any) => e.id === eventId);
    if (!evt) return;
    const extract = (d?: string) => (typeof d === 'string' ? d.split('T')[0] : undefined);
    const start = extract(evt.startDate);
    const end = extract(evt.endDate);
    if (start && !fromDate) setValue('fromDate', start as any);
    if (end && !toDate) setValue('toDate', end as any);
  }, [eventId, eventsPage, fromDate, toDate, setValue]);

  if (!isHydrated) {
    return <PageLoading message="Đang tải dữ liệu tổ chức..." />
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="container-page py-8">
        {/* Header Section */}
        <div className="mb-8">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
            <div className="mb-4 sm:mb-0">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">Báo cáo & Export</h1>
              <p className="text-gray-600 text-lg">Tạo và tải xuống báo cáo chi tiết về sự kiện</p>
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

        {/* Main Content */}
        <div className="max-w-4xl mx-auto">
          {/* Configuration Card */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-8">
            <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center">
              <svg className="w-5 h-5 mr-2 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              Cấu hình báo cáo
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Chọn sự kiện</label>
                <select 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.eventId ? 'border-red-500' : 'border-gray-300'} focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200`}
                  {...register('eventId')}
                >
                  <option value="">Chọn sự kiện...</option>
                  {(eventsPage?.content ?? []).map((e: any) => (
                    <option key={e.id} value={e.id}>{e.title}</option>
                  ))}
                </select>
                {errors.eventId && <p className="text-sm text-red-600 mt-1">{errors.eventId.message}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Từ ngày</label>
                <input 
                  type="date" 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.fromDate ? 'border-red-500' : 'border-gray-300'} focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200`}
                  {...register('fromDate')}
                />
                {errors.fromDate && <p className="text-sm text-red-600 mt-1">{errors.fromDate.message}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Đến ngày</label>
                <input 
                  type="date" 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.toDate ? 'border-red-500' : 'border-gray-300'} focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200`}
                  {...register('toDate')}
                />
                {errors.toDate && <p className="text-sm text-red-600 mt-1">{errors.toDate.message}</p>}
              </div>
            </div>
          </div>

          {/* Report Generation */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-8">
            <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center">
              <svg className="w-5 h-5 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              Tạo báo cáo
            </h2>

            <div className="flex items-center gap-3 pt-6">
              <button
                onClick={() => salesMut.mutate({ ...baseInput, name: "Sales report" })}
                disabled={disabled || salesMut.isPending}
                className="inline-flex items-center px-6 py-3 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-60"
              >
                {salesMut.isPending ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    Đang tạo báo cáo bán hàng...
                  </>
                ) : (
                  'Tạo báo cáo bán hàng'
                )}
              </button>
              
              <button
                onClick={() => revenueMut.mutate({ ...baseInput, name: "Revenue report" })}
                disabled={disabled || revenueMut.isPending}
                className="inline-flex items-center px-6 py-3 border border-transparent text-sm font-medium rounded-lg text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-60"
              >
                {revenueMut.isPending ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    Đang tạo báo cáo doanh thu...
                  </>
                ) : (
                  'Tạo báo cáo doanh thu'
                )}
              </button>
              
              <button
                onClick={() => attendanceMut.mutate({ ...baseInput, name: "Attendance report" })}
                disabled={disabled || attendanceMut.isPending}
                className="inline-flex items-center px-6 py-3 border border-transparent text-sm font-medium rounded-lg text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 disabled:opacity-60"
              >
                {attendanceMut.isPending ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    Đang tạo báo cáo tham dự...
                  </>
                ) : (
                  'Tạo báo cáo tham dự'
                )}
              </button>
            </div>

            {lastReportId && (
              <div className="flex items-center gap-3 pt-4">
                <button
                  onClick={doExportPdf}
                  className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  Xuất PDF
                </button>
                <button
                  onClick={doExportExcel}
                  className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  Xuất Excel
                </button>
              </div>
            )}
          </div>

          {/* Export Section */}
          {lastReportId && (
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center">
                <svg className="w-5 h-5 mr-2 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                Tải xuống báo cáo
              </h2>

              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  <button 
                    onClick={doExportPdf} 
                    className="inline-flex items-center px-6 py-3 border border-gray-300 shadow-sm text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
                  >
                    <svg className="w-4 h-4 mr-2 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                    </svg>
                    Tải PDF
                  </button>
                  <button 
                    onClick={doExportExcel} 
                    className="inline-flex items-center px-6 py-3 border border-gray-300 shadow-sm text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
                  >
                    <svg className="w-4 h-4 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                    Tải Excel
                  </button>
                </div>
                <div className="text-sm text-gray-500">
                  Report ID: <span className="font-medium text-gray-900">{lastReportId}</span>
                </div>
              </div>
              {lastReportDetail && (
                <div className="mt-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">Xem nhanh báo cáo gần nhất</h3>
                  <div className="bg-gray-50 rounded-lg p-4 text-sm">
                    <div className="mb-2">
                      <strong>Tên:</strong> {(lastReportDetail as any)?.data?.name || 'N/A'}
                    </div>
                    <div className="mb-2">
                      <strong>Loại:</strong> {(lastReportDetail as any)?.data?.type || 'N/A'}
                    </div>
                    <div className="mb-2">
                      <strong>Ngày tạo:</strong> {(lastReportDetail as any)?.data?.dateGenerated ? new Date((lastReportDetail as any).data.dateGenerated).toLocaleString('vi-VN') : 'N/A'}
                    </div>
                    {(lastReportDetail as any)?.data?.resultData && (
                      <div className="mt-3">
                        <strong>Dữ liệu kết quả:</strong>
                        <pre className="mt-2 bg-white p-3 rounded border overflow-auto max-h-48">{JSON.stringify((lastReportDetail as any).data.resultData, null, 2)}</pre>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Status Messages */}
          {(salesMut.isPending || revenueMut.isPending || attendanceMut.isPending) && (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mt-6">
              <div className="flex items-center">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mr-3"></div>
                <span className="text-sm text-blue-800">Đang tạo báo cáo...</span>
              </div>
            </div>
          )}

          {(salesMut.isSuccess || revenueMut.isSuccess || attendanceMut.isSuccess) && (
            <div className="bg-green-50 border border-green-200 rounded-lg p-4 mt-6">
              <div className="flex items-center">
                <svg className="w-4 h-4 text-green-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span className="text-sm text-green-800">Báo cáo đã được tạo thành công!</span>
              </div>
            </div>
          )}
        </div>

        {/* Reports listing with filters */}
        <div className="max-w-4xl mx-auto mt-8">
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <div className="flex flex-col md:flex-row md:items-end gap-4 mb-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Chế độ danh sách</label>
                <select className="rounded-lg border border-gray-300 px-4 py-3" value={listMode} onChange={(e) => { setListMode(e.target.value as any); setListPage(0); }}>
                  <option value="mine">Báo cáo của tôi</option>
                  <option value="event">Theo sự kiện</option>
                  <option value="type">Theo loại</option>
                </select>
              </div>
              {listMode === 'event' && (
                <div className="flex-1">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Sự kiện</label>
                  <select 
                    className="w-full rounded-lg border border-gray-300 px-4 py-3"
                    value={eventId}
                    onChange={(e) => { setValue('eventId', e.target.value as any); setListPage(0); }}
                  >
                    <option value="">Chọn sự kiện...</option>
                    {(eventsPage?.content ?? []).map((e: any) => (
                      <option key={e.id} value={e.id}>{e.title}</option>
                    ))}
                  </select>
                </div>
              )}
              {listMode === 'type' && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Loại</label>
                  <select className="rounded-lg border border-gray-300 px-4 py-3" value={reportType} onChange={(e) => { setReportType(e.target.value as any); setListPage(0); }}>
                    <option value="REVENUE">REVENUE</option>
                    <option value="SALES">SALES</option>
                    <option value="ATTENDANCE">ATTENDANCE</option>
                  </select>
                </div>
              )}
            </div>

            {(() => {
              const data = listMode === 'mine' ? (myReports as any) : listMode === 'event' ? (eventReports as any) : (typeReports as any);
              const items = data?.data?.content ?? [];
              const totalPages = data?.data?.totalPages ?? 1;
              return (
                <>
                  <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-100">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Tên</th>
                          <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Loại</th>
                          <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Ngày tạo</th>
                          <th className="px-4 py-3 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Hành động</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100 bg-white">
                        {items.map((r: any) => (
                          <tr key={r.id} className="hover:bg-gray-50">
                            <td className="px-4 py-3 text-sm text-gray-900">{r.name}</td>
                            <td className="px-4 py-3 text-sm text-gray-700">{r.type}</td>
                            <td className="px-4 py-3 text-sm text-gray-500">{new Date(r.dateGenerated).toLocaleString('vi-VN')}</td>
                            <td className="px-4 py-3 text-sm text-gray-700 text-right space-x-2">
                              <button onClick={() => setLastReportId(r.id)} className="px-3 py-1.5 rounded-md border text-gray-700 hover:bg-gray-50">Xem</button>
                              <button onClick={() => handleExportPdf(r.id)} className="px-3 py-1.5 rounded-md border text-gray-700 hover:bg-gray-50">PDF</button>
                              <button onClick={() => handleExportExcel(r.id)} className="px-3 py-1.5 rounded-md border text-gray-700 hover:bg-gray-50">Excel</button>
                            </td>
                          </tr>
                        ))}
                        {items.length === 0 && (
                          <tr>
                            <td colSpan={4} className="px-4 py-6 text-center text-sm text-gray-500">Không có báo cáo</td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>

                  {totalPages > 1 && (
                    <div className="mt-4 flex items-center justify-between">
                      <div className="text-sm text-gray-600">Trang {listPage + 1} / {totalPages}</div>
                      <div className="space-x-2">
                        <button onClick={() => setListPage(Math.max(0, listPage - 1))} disabled={listPage === 0} className="px-3 py-1.5 rounded-md border text-gray-700 disabled:opacity-50">Trước</button>
                        <button onClick={() => setListPage(Math.min(totalPages - 1, listPage + 1))} disabled={listPage >= totalPages - 1} className="px-3 py-1.5 rounded-md border text-gray-700 disabled:opacity-50">Sau</button>
                      </div>
                    </div>
                  )}
                </>
              );
            })()}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function ReportsPage() {
  return (
    <Suspense fallback={<section className="py-10"><div className="container-page">Đang tải...</div></section>}>
      <ReportsContent />
    </Suspense>
  );
}


