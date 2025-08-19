"use client";
import { getFeaturedEvents, getUpcomingEvents } from "@/lib/api/modules/events";
import { getUserTickets } from "@/lib/api/modules/tickets";
import { useAuthStore } from "@/store/auth";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { Suspense, useMemo } from "react";

function Stats({ total, reserved, paid }: { total: number; reserved: number; paid: number }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div className="rounded-lg border p-4">
        <div className="text-xs text-slate-500">Tổng vé</div>
        <div className="text-2xl font-semibold">{total}</div>
      </div>
      <div className="rounded-lg border p-4">
        <div className="text-xs text-slate-500">Đã thanh toán</div>
        <div className="text-2xl font-semibold">{paid}</div>
      </div>
      <div className="rounded-lg border p-4">
        <div className="text-xs text-slate-500">Đang giữ chỗ</div>
        <div className="text-2xl font-semibold">{reserved}</div>
      </div>
    </div>
  );
}

function DashboardContent() {
  const { currentUser, isHydrated } = useAuthStore();
  const isLoggedIn = !!currentUser;

  const ticketsQ = useQuery({
    queryKey: ["dashboard-tickets"],
    enabled: isLoggedIn,
    queryFn: async () => await getUserTickets(undefined, 0, 50),
    placeholderData: (prev) => prev,
  });
  const upcomingQ = useQuery({
    queryKey: ["dashboard-upcoming"],
    queryFn: async () => await getUpcomingEvents(6),
  });
  const featuredQ = useQuery({
    queryKey: ["dashboard-featured"],
    queryFn: async () => await getFeaturedEvents(6),
  });

  const ticketsRaw: any[] = useMemo(() => ((ticketsQ.data as any)?.data?.content ?? []), [ticketsQ.data]);
  const total = ticketsRaw.length;
  const reserved = ticketsRaw.filter((t) => t?.status === 'RESERVED').length;
  const paid = ticketsRaw.filter((t) => t?.status === 'PAID').length;
  const recent = ticketsRaw.slice(0, 5);

  return (
    <section className="py-10">
      <div className="container-page space-y-6">
        <div>
          <h1 className="text-2xl font-semibold">Bảng điều khiển</h1>
          <p className="text-slate-600">Tổng quan hoạt động và gợi ý cho bạn</p>
        </div>

        {!isHydrated ? (
          <div className="text-slate-600">Đang tải...</div>
        ) : !isLoggedIn ? (
          <div className="rounded-lg border p-6 text-center">
            <div className="text-slate-700">Bạn cần đăng nhập để xem bảng điều khiển.</div>
            <Link href="/?login=1" className="inline-block mt-3 rounded-md bg-slate-900 px-4 py-2 text-white">Đăng nhập</Link>
          </div>
        ) : (
          <>
            <Stats total={total} reserved={reserved} paid={paid} />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="rounded-lg border p-4">
                <div className="flex items-center justify-between mb-2">
                  <h2 className="font-semibold">Hoạt động gần đây</h2>
                  <Link href="/tickets" className="text-sm text-blue-600 hover:text-blue-800">Xem tất cả</Link>
                </div>
                <div className="divide-y">
                  {ticketsQ.isLoading ? (
                    <div className="text-slate-600 py-2">Đang tải...</div>
                  ) : recent.length === 0 ? (
                    <div className="text-slate-600 py-2">Chưa có vé nào.</div>
                  ) : (
                    recent.map((t: any) => (
                      <Link key={t.id} href={`/tickets/${t.id}`} className="flex items-center justify-between py-2 hover:bg-gray-50">
                        <div className="text-sm text-gray-900">{t.eventTitle || t.ticketTypeName}</div>
                        <div className="text-xs text-gray-600">{t.status}</div>
                      </Link>
                    ))
                  )}
                </div>
              </div>

              <div className="rounded-lg border p-4">
                <div className="flex items-center justify-between mb-2">
                  <h2 className="font-semibold">Sự kiện sắp tới</h2>
                  <Link href="/search" className="text-sm text-blue-600 hover:text-blue-800">Khám phá</Link>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {upcomingQ.isLoading ? (
                    <div className="text-slate-600">Đang tải...</div>
                  ) : (upcomingQ.data ?? []).length === 0 ? (
                    <div className="text-slate-600">Chưa có sự kiện.</div>
                  ) : (
                    (upcomingQ.data ?? []).map((e: any) => (
                      <Link key={e.id} href={`/events/${e.id}`} className="rounded-lg border p-3 hover:border-gray-400">
                        <div className="font-medium text-gray-900 line-clamp-2">{e.title}</div>
                        <div className="text-xs text-slate-600 mt-1">{new Date(e.startDate).toLocaleDateString('vi-VN')}</div>
                      </Link>
                    ))
                  )}
                </div>
              </div>
            </div>

            <div className="rounded-lg border p-4">
              <div className="flex items-center justify-between mb-2">
                <h2 className="font-semibold">Gợi ý cho bạn</h2>
                <Link href="/search" className="text-sm text-blue-600 hover:text-blue-800">Xem thêm</Link>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
                {featuredQ.isLoading ? (
                  <div className="text-slate-600">Đang tải...</div>
                ) : (featuredQ.data ?? []).length === 0 ? (
                  <div className="text-slate-600">Chưa có gợi ý.</div>
                ) : (
                  (featuredQ.data ?? []).map((e: any) => (
                    <Link key={e.id} href={`/events/${e.id}`} className="rounded-lg border p-3 hover:border-gray-400">
                      <div className="font-medium text-gray-900 line-clamp-2">{e.title}</div>
                      <div className="text-xs text-slate-600 mt-1">{e.locationName}</div>
                    </Link>
                  ))
                )}
              </div>
            </div>
          </>
        )}
      </div>
    </section>
  );
}

export default function DashboardPage() {
  return (
    <Suspense fallback={<section className="py-10"><div className="container-page">Đang tải...</div></section>}>
      <DashboardContent />
    </Suspense>
  );
}



