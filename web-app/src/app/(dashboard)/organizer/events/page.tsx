"use client";

import { ErrorState, PageLoading } from "@/components/ui/LoadingSpinner";
import { useAuthHydration } from "@/hooks/useAuthHydration";
import { useOrganizerEvents } from "@/hooks/useFeaturedEvents";
import { useRequireRole } from "@/hooks/useRequireRole";
import type { EventDto } from "@/lib/api";
import { cancelEvent, publishEvent } from "@/lib/api/modules/events";
import { useAuthStore } from "@/store/auth";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import { Suspense, useEffect, useMemo as useReactMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { usePublishEvent, useCancelEvent, useDeleteEvent } from "@/hooks/useEvents";
import { toast } from "sonner";

function useOrganizerId() {
  const { currentUser } = useAuthStore();
  return currentUser?.id ?? "";
}

function useIsOrganizerRole() {
  const { currentUser } = useAuthStore();
  return currentUser?.role === 'ORGANIZER' || currentUser?.role === 'ADMIN';
}

function EventsTable({ items, onPublish, onCancel, onDelete, pendingPublishId, pendingCancelId, pendingDeleteId }: { 
  items: EventDto[]; 
  onPublish: (id: string) => void; 
  onCancel: (id: string) => void; 
  onDelete: (id: string) => void;
  pendingPublishId?: string | null;
  pendingCancelId?: string | null;
  pendingDeleteId?: string | null;
}) {
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DRAFT': return 'bg-gray-100 text-gray-800';
      case 'PUBLISHED': return 'bg-green-100 text-green-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      case 'COMPLETED': return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'DRAFT': return 'Nháp';
      case 'PUBLISHED': return 'Đã xuất bản';
      case 'CANCELLED': return 'Đã hủy';
      case 'COMPLETED': return 'Hoàn thành';
    }
    return status;
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-100">
          <thead className="bg-gray-50/50">
            <tr>
              <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Sự kiện</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Trạng thái</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Thời gian</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Đã bán</th>
              <th className="px-6 py-4 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Hành động</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {items.map((e) => (
              <tr key={e.id} className="hover:bg-gray-50/50 transition-colors duration-200">
                <td className="px-6 py-4">
                  <div className="flex items-start space-x-3">
                    <div className="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center">
                      <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-semibold text-gray-900 truncate">{e.title}</p>
                      <p className="text-sm text-gray-500 truncate">{e.locationName}</p>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(e.status)}`}>
                    {getStatusText(e.status)}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-900">
                    {new Date(e.startDate).toLocaleDateString("vi-VN", { day: '2-digit', month: '2-digit', year: 'numeric' })}
                  </div>
                  <div className="text-sm text-gray-500">
                    {new Date(e.startDate).toLocaleTimeString("vi-VN", { hour: '2-digit', minute: '2-digit' })}
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="flex items-center">
                    <div className="flex-1 bg-gray-200 rounded-full h-2 mr-3">
                      <div className="bg-gradient-to-r from-blue-500 to-purple-600 h-2 rounded-full transition-all duration-300" style={{ width: `${Math.min(100, (e.currentAttendees / e.maxAttendees) * 100)}%` }} />
                    </div>
                    <span className="text-sm font-medium text-gray-900">{e.currentAttendees}/{e.maxAttendees}</span>
                  </div>
                </td>
                <td className="px-6 py-4 text-right">
                  <div className="flex items-center justify-end space-x-2">
                    <Link href={`/organizer/events/${e.id}/edit`} className="inline-flex items-center px-3 py-1.5 border border-gray-300 shadow-sm text-xs font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200">Sửa</Link>
                    {e.status === 'DRAFT' && (
                      <button onClick={() => onPublish(e.id as string)} disabled={pendingPublishId === e.id} className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition-colors duration-200 disabled:opacity-60">
                        {pendingPublishId === e.id ? (
                          <>
                            <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-white mr-1"></div>
                            Đang xử lý...
                          </>
                        ) : (
                          'Xuất bản'
                        )}
                      </button>
                    )}
                    {e.status === 'PUBLISHED' && (
                      <button onClick={() => onCancel(e.id as string)} disabled={pendingCancelId === e.id} className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white bg-yellow-600 hover:bg-yellow-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 transition-colors duration-200 disabled:opacity-60">
                        {pendingCancelId === e.id ? (
                          <>
                            <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-white mr-1"></div>
                            Đang xử lý...
                          </>
                        ) : (
                          'Hủy'
                        )}
                      </button>
                    )}
                    {e.status === 'CANCELLED' && (
                      <button onClick={() => onDelete(e.id as string)} disabled={pendingDeleteId === e.id} className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-colors duration-200 disabled:opacity-60">
                        {pendingDeleteId === e.id ? (
                          <>
                            <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-white mr-1"></div>
                            Đang xử lý...
                          </>
                        ) : (
                          'Xóa'
                        )}
                      </button>
                    )}
                    <Link href={`/organizer/events/${e.id}/tickets`} className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200">Vé</Link>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function OrganizerEventsListContent() {
  useRequireRole("ORGANIZER", { openModal: true, allowAdmin: true });
  const isHydrated = useAuthHydration();
  const [page, setPage] = useState<number>(0);
  const size = 10;
  const { data, isLoading, error, refetch } = useOrganizerEvents(page, size);
  const [q, setQ] = useState("");
  const [status, setStatus] = useState<string>("");
  const debounced = useReactMemo(() => {
    let timer: any;
    return (v: string) => {
      if (timer) clearTimeout(timer);
      timer = setTimeout(() => setQ(v), 200);
    };
  }, []);

  const itemsRaw = data?.content ?? [];
  const items = itemsRaw.filter((e) => (
    (!q || e.title.toLowerCase().includes(q.toLowerCase())) &&
    (!status || e.status === status as any)
  ));
  const totalPages = data?.totalPages ?? 1;

  const qc = useQueryClient();
  const publishMut = usePublishEvent();
  const cancelMut = useCancelEvent();
  const deleteMut = useDeleteEvent();
  const [pendingPublishId, setPendingPublishId] = useState<string | null>(null);
  const [pendingCancelId, setPendingCancelId] = useState<string | null>(null);
  const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);

  const handlePublish = async (eventId: string) => {
    try {
      setPendingPublishId(eventId);
      await publishMut.mutateAsync(eventId);
      toast.success("Sự kiện đã được xuất bản thành công!");
      refetch();
    } catch (error: any) {
      toast.error(error?.message || "Không thể xuất bản sự kiện");
    }
    finally {
      setPendingPublishId(null);
    }
  };

  const handleCancel = async (eventId: string, eventTitle: string) => {
    const reason = prompt(`Lý do hủy sự kiện "${eventTitle}":`);
    if (!reason) return;
    
    try {
      setPendingCancelId(eventId);
      await cancelMut.mutateAsync({ eventId, reason });
      toast.success("Sự kiện đã được hủy thành công!");
      refetch();
    } catch (error: any) {
      toast.error(error?.message || "Không thể hủy sự kiện");
    }
    finally {
      setPendingCancelId(null);
    }
  };

  const handleDelete = async (eventId: string, eventTitle: string) => {
    if (!confirm(`Bạn có chắc chắn muốn xóa sự kiện "${eventTitle}"?`)) return;
    
    try {
      setPendingDeleteId(eventId);
      await deleteMut.mutateAsync(eventId);
      toast.success("Sự kiện đã được xóa thành công!");
      refetch();
    } catch (error: any) {
      toast.error(error?.message || "Không thể xóa sự kiện");
    }
    finally {
      setPendingDeleteId(null);
    }
  };

  // Ensure fetch after hydration
  useEffect(() => {
    if (isHydrated) refetch();
  }, [isHydrated, page, size, refetch]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="container-page py-8">
        <div className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div className="mb-2 sm:mb-0">
            <h1 className="text-3xl font-bold text-gray-900 mb-1">Danh sách sự kiện</h1>
            <p className="text-gray-600">Bộ lọc và hành động nhanh</p>
          </div>
          <div className="flex items-center gap-2">
            <Link href="/organizer/events/new" className="inline-flex items-center px-6 py-3 border border-transparent text-sm font-medium rounded-lg text-white bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200 shadow-lg hover:shadow-xl">Tạo sự kiện</Link>
            <Link href="/organizer/analytics" className="inline-flex items-center px-6 py-3 border border-gray-300 text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200 shadow-lg hover:shadow-xl">Analytics</Link>
            <Link href="/organizer/events/check-in" className="inline-flex items-center px-6 py-3 border border-transparent text-sm font-medium rounded-lg text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-all duration-200 shadow-lg hover:shadow-xl">Check-in</Link>
          </div>
        </div>

        {/* Stats Cards moved from /organizer */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
                  <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Tổng sự kiện</p>
                <p className="text-2xl font-bold text-gray-900">{itemsRaw.length}</p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                  <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Đã xuất bản</p>
                <p className="text-2xl font-bold text-gray-900">{itemsRaw.filter((e:any)=>e.status==='PUBLISHED').length}</p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-yellow-100 rounded-lg flex items-center justify-center">
                  <svg className="w-5 h-5 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Sắp diễn ra</p>
                <p className="text-2xl font-bold text-gray-900">{itemsRaw.filter((e:any)=> new Date(e.startDate)> new Date()).length}</p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-purple-100 rounded-lg flex items-center justify-center">
                  <svg className="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
                  </svg>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Tổng vé bán</p>
                <p className="text-2xl font-bold text-gray-900">{itemsRaw.reduce((sum:any,e:any)=> sum + (e.currentAttendees||0),0)}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 mb-6">
          <div className="flex flex-col md:flex-row md:items-center gap-3">
            <div className="flex-1">
              <input className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500" placeholder="Tìm theo tiêu đề..." onChange={(e) => debounced(e.target.value)} />
            </div>
            <div>
              <select className="rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500" value={status} onChange={(e) => setStatus(e.target.value)}>
                <option value="">Tất cả trạng thái</option>
                <option value="DRAFT">Nháp</option>
                <option value="PUBLISHED">Đã xuất bản</option>
                <option value="CANCELLED">Đã huỷ</option>
                <option value="COMPLETED">Hoàn thành</option>
              </select>
            </div>
          </div>
        </div>

        {!isHydrated || isLoading ? (
          <PageLoading message="Đang tải danh sách sự kiện..." />
        ) : error ? (
          <ErrorState title="Không thể tải danh sách sự kiện" message="Vui lòng thử lại" onRetry={() => refetch()} />
        ) : items.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center">
            <svg className="mx-auto h-12 w-12 text-gray-400 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
            <h3 className="text-sm font-medium text-gray-900">Không có kết quả</h3>
            <p className="mt-1 text-sm text-gray-500">Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc.</p>
          </div>
        ) : (
          <EventsTable 
            items={items as EventDto[]} 
            onPublish={handlePublish}
            onCancel={(id) => {
              const evt = items.find((e:any)=> e.id===id);
              if (!evt) return;
              handleCancel(id, evt?.title || '');
            }}
            onDelete={(id) => {
              const evt = items.find((e:any)=> e.id===id);
              if (!evt) return;
              handleDelete(id, evt?.title || '');
            }}
            pendingPublishId={pendingPublishId}
            pendingCancelId={pendingCancelId}
            pendingDeleteId={pendingDeleteId}
          />
        )}

        {totalPages > 1 && (
          <div className="bg-white mt-6 rounded-xl shadow-sm border border-gray-100 px-6 py-4">
            <div className="flex items-center justify-between">
              <div className="text-sm text-gray-700">Trang {page + 1} / {totalPages}</div>
              <div className="flex items-center space-x-2">
                <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0} className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50">Trước</button>
                <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))} disabled={page === totalPages - 1} className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50">Sau</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default function OrganizerEventsListPage() {
  return (
    <Suspense fallback={<section className="py-10"><div className="container-page">Đang tải...</div></section>}>
      <OrganizerEventsListContent />
    </Suspense>
  );
}


