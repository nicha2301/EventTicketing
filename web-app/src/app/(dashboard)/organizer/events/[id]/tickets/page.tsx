"use client";

import { Suspense, useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { organizerTicketTypeSchema, type OrganizerTicketTypeInput } from "@/lib/validation/organizer/events";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { TicketTypeDto } from "@/lib/api";
import { createTicketType, updateTicketType, deleteTicketType } from "@/lib/api/modules/tickets";
import { useRequireRole } from "@/hooks/useRequireRole";
import { useAuthStore } from "@/store/auth";
import { useEventTicketTypes } from "@/hooks/useTickets";
import { getEventTicketTypes as fetchEventTicketTypes } from "@/lib/api/modules/tickets";
import { useAuthHydration } from "@/hooks/useAuthHydration";
import { PageLoading, ErrorState } from "@/components/ui/LoadingSpinner";

const schema = organizerTicketTypeSchema;

type FormValues = OrganizerTicketTypeInput;

function useIsOrganizerRole() {
  const { currentUser } = useAuthStore();
  return currentUser?.role === 'ORGANIZER' || currentUser?.role === 'ADMIN';
}


function TicketTypeForm({ eventId, onDone, editing }: { eventId: string; onDone: () => void; editing?: TicketTypeDto | null }) {
  const queryClient = useQueryClient();
  const { register, handleSubmit, formState: { errors }, reset, setValue } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: editing ? {
      name: editing.name,
      description: editing.description,
      price: editing.price,
      quantity: editing.quantity,
      minTicketsPerOrder: editing.minTicketsPerOrder,
      maxTicketsPerCustomer: editing.maxTicketsPerCustomer,
      isVIP: editing.isVIP,
      isEarlyBird: editing.isEarlyBird,
      isActive: editing.isActive,
      salesStartDate: editing.salesStartDate,
      salesEndDate: editing.salesEndDate,
    } : undefined
  });

  const createMut = useMutation({
    mutationFn: async (data: TicketTypeDto) => createTicketType(eventId, data),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["event-ticket-types", eventId] });
      try { (window as any).next?.router?.refresh?.(); } catch {}
      onDone();
    }
  });
  const updateMut = useMutation({
    mutationFn: async (data: TicketTypeDto) => updateTicketType(editing?.id as string, data),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["event-ticket-types", eventId] });
      try { (window as any).next?.router?.refresh?.(); } catch {}
      onDone();
    }
  });

  const toBackendDate = (s?: string) => {
    if (!s) return s as any;
    return s.length === 16 ? `${s}:00` : s;
  };

  const onSubmit = (values: FormValues) => {
    const dto: TicketTypeDto = {
      eventId,
      name: values.name,
      description: values.description,
      price: values.price,
      quantity: values.quantity,
      minTicketsPerOrder: values.minTicketsPerOrder,
      maxTicketsPerCustomer: values.maxTicketsPerCustomer,
      isVIP: values.isVIP,
      isEarlyBird: values.isEarlyBird,
      isActive: values.isActive,
      salesStartDate: toBackendDate(values.salesStartDate) as any,
      salesEndDate: toBackendDate(values.salesEndDate) as any,
      quantitySold: editing?.quantitySold ?? 0,
    };
    if (editing?.id) {
      updateMut.mutate(dto);
    } else {
      createMut.mutate(dto);
      reset();
    }
  };

  const toLocalDateTimeInput = (value?: string) => {
    if (!value) return "";
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return "";
    const pad = (n: number) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  };

  useEffect(() => {
    if (editing) {
      setValue('name', editing.name);
      setValue('description', editing.description ?? '');
      setValue('price', editing.price as any);
      setValue('quantity', editing.quantity as any);
      setValue('minTicketsPerOrder', editing.minTicketsPerOrder as any);
      setValue('maxTicketsPerCustomer', editing.maxTicketsPerCustomer as any);
      setValue('isVIP', !!editing.isVIP);
      setValue('isEarlyBird', !!editing.isEarlyBird);
      setValue('isActive', !!editing.isActive);
      setValue('salesStartDate', toLocalDateTimeInput(editing.salesStartDate as any));
      setValue('salesEndDate', toLocalDateTimeInput(editing.salesEndDate as any));
    } else {
      reset();
    }
  }, [editing, reset, setValue]);

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Tên loại vé</label>
          <input className={`w-full rounded-lg border px-4 py-3 ${errors.name ? 'border-red-500' : 'border-gray-300'} focus:ring-2 focus:ring-blue-500 focus:border-blue-500`} {...register("name")} />
          {errors.name && <p className="text-sm text-red-600 mt-1">{errors.name.message}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Giá (VND)</label>
          <input type="number" className={`w-full rounded-lg border px-4 py-3 ${errors.price ? 'border-red-500' : 'border-gray-300'} focus:ring-2 focus:ring-blue-500 focus:border-blue-500`} {...register("price")} />
          {errors.price && <p className="text-sm text-red-600 mt-1">{errors.price.message}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Số lượng</label>
          <input type="number" className={`w-full rounded-lg border px-4 py-3 ${errors.quantity ? 'border-red-500' : 'border-gray-300'} focus:ring-2 focus:ring-blue-500 focus:border-blue-500`} {...register("quantity")} />
          {errors.quantity && <p className="text-sm text-red-600 mt-1">{errors.quantity.message}</p>}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Min/order</label>
          <input type="number" className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500" {...register("minTicketsPerOrder")} />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Max/customer</label>
          <input type="number" className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500" {...register("maxTicketsPerCustomer")} />
        </div>
        <div className="flex items-center gap-6">
          <label className="inline-flex items-center gap-2">
            <input type="checkbox" {...register("isVIP")} className="w-4 h-4 text-blue-600" />
            <span className="text-sm font-medium">VIP</span>
          </label>
          <label className="inline-flex items-center gap-2">
            <input type="checkbox" {...register("isEarlyBird")} className="w-4 h-4 text-blue-600" />
            <span className="text-sm font-medium">Early Bird</span>
          </label>
          <label className="inline-flex items-center gap-2">
            <input type="checkbox" defaultChecked {...register("isActive")} className="w-4 h-4 text-blue-600" />
            <span className="text-sm font-medium">Kích hoạt</span>
          </label>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Bán từ</label>
          <input type="datetime-local" className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500" {...register("salesStartDate")} />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Bán đến</label>
          <input type="datetime-local" className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500" {...register("salesEndDate")} />
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Mô tả</label>
        <textarea className="w-full rounded-lg border border-gray-300 px-4 py-3 min-h-24 focus:ring-2 focus:ring-blue-500 focus:border-blue-500" {...register("description")} />
      </div>

      <div className="flex items-center gap-3 pt-2">
        <button className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-60" disabled={createMut.isPending || updateMut.isPending}>
          {(createMut.isPending || updateMut.isPending) ? (
            <>
              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
              {editing?.id ? "Đang cập nhật..." : "Đang tạo..."}
            </>
          ) : (
            editing?.id ? "Cập nhật" : "Thêm loại vé"
          )}
        </button>
        {editing?.id && (
          <button type="button" onClick={onDone} className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50">Hủy</button>
        )}
      </div>
    </form>
  );
}

function TicketTypesContent() {
  useRequireRole("ORGANIZER", { openModal: true, allowAdmin: true });
  const params = useParams();
  const eventId = params.id as string;
  const [page, setPage] = useState(0);
  const size = 10;
  const isHydrated = useAuthHydration();
  const { data, isLoading, error, refetch } = useEventTicketTypes(eventId, page, size);
  const [bootLoading, setBootLoading] = useState(false);
  const [bootError, setBootError] = useState<string | null>(null);
  const [bootData, setBootData] = useState<any[] | null>(null);
  const queryClient = useQueryClient();

  const delMut = useMutation({
    mutationFn: async (id: string) => deleteTicketType(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["event-ticket-types", eventId] });
    }
  });

  const items = (data as any)?.data?.content ?? bootData ?? [];
  const totalPages = (data as any)?.data?.totalPages ?? 1;
  const [editing, setEditing] = useState<TicketTypeDto | null>(null);

  const exportCsv = () => {
    const rows = [
      ["id","ticketNumber","ticketTypeName","price","status","userName"],
      ...items.map((t: any) => [t.id, t.ticketNumber, t.ticketTypeName, t.price, t.status, t.userName])
    ];
    const csv = rows.map((r: (string | number | null | undefined)[]) => r.map((x: string | number | null | undefined) => (x ?? '')).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = `tickets-${eventId}.csv`; a.click();
    URL.revokeObjectURL(url);
  };

  useEffect(() => {
    if (!eventId || !isHydrated) return;
    setBootLoading(true);
    setBootError(null);
    const ac = new AbortController();
    fetchEventTicketTypes(eventId, page, size, ac.signal)
      .then((res) => {
        setBootData(res?.data?.content ?? []);
      })
      .catch((e: any) => setBootError(e?.message || ''))
      .finally(() => setBootLoading(false));
    return () => ac.abort();
  }, [eventId, page, size, isHydrated]);

  if (!isHydrated || bootLoading || isLoading) {
    return <PageLoading message="Đang tải loại vé..." />
  }

  if ((!items || items.length === 0) && (bootError || error)) {
    return <ErrorState title="Không thể tải loại vé" message="Vui lòng thử lại." onRetry={() => refetch()} />
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="container-page py-8 space-y-6">
        <div className="mb-2">
          <h1 className="text-3xl font-bold text-gray-900">Loại vé</h1>
          <p className="text-gray-600">Quản lý các loại vé cho sự kiện</p>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 flex items-center justify-between">
          <div className="text-sm text-gray-700">Tổng: {items.length} vé</div>
          <button onClick={exportCsv} className="inline-flex items-center px-3 py-2 border border-gray-300 rounded-md text-sm text-gray-700 bg-white hover:bg-gray-50">Export CSV</button>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-100">
              <thead className="bg-gray-50/50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Tên</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Giá</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Số lượng</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Đã bán</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Trạng thái</th>
                  <th className="px-6 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {items.map((t: TicketTypeDto) => (
                  <tr key={t.id} className="hover:bg-gray-50/50">
                    <td className="px-6 py-3 text-sm text-gray-900">{t.name}</td>
                    <td className="px-6 py-3 text-sm text-gray-700">{new Intl.NumberFormat('vi-VN').format(t.price)}</td>
                    <td className="px-6 py-3 text-sm text-gray-700">{t.quantity}</td>
                    <td className="px-6 py-3 text-sm text-gray-700">{t.quantitySold}</td>
                    <td className="px-6 py-3 text-sm text-gray-700">{t.isActive ? 'Active' : 'Inactive'}</td>
                    <td className="px-6 py-3 text-right space-x-2">
                      <button onClick={() => setEditing(t)} className="inline-flex items-center px-3 py-1.5 border border-gray-300 rounded-md text-xs text-gray-700 bg-white hover:bg-gray-50">Sửa</button>
                      <button onClick={() => { if (confirm('Bạn có chắc chắn muốn xoá loại vé này?')) delMut.mutate(t.id as string) }} className="inline-flex items-center px-3 py-1.5 border border-transparent rounded-md text-xs text-white bg-red-600 hover:bg-red-700">Xoá</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h2 className="text-lg font-semibold mb-4">{editing ? "Sửa loại vé" : "Thêm loại vé mới"}</h2>
          <TicketTypeForm eventId={eventId} editing={editing} onDone={() => setEditing(null)} />
        </div>

        {totalPages > 1 && (
          <div className="bg-white mt-2 rounded-xl shadow-sm border border-gray-100 px-6 py-4">
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

export default function EventTicketTypesPage() {
  return (
    <Suspense fallback={<section className="py-10"><div className="container-page">Đang tải...</div></section>}>
      <TicketTypesContent />
    </Suspense>
  );
}


