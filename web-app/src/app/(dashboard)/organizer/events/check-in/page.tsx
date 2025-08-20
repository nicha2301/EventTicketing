"use client";

import { PageLoading } from "@/components/ui/LoadingSpinner";
import { useAuthHydration } from "@/hooks/useAuthHydration";
import { useOrganizerEvents } from "@/hooks/useFeaturedEvents";
import { useRequireRole } from "@/hooks/useRequireRole";
import { useCheckIn, useEventTickets } from "@/hooks/useTickets";
import type { TicketCheckInRequestDto, TicketDto } from "@/lib/api";
import { useAuthStore } from "@/store/auth";
import { useQueryClient } from "@tanstack/react-query";
import { BrowserMultiFormatReader } from '@zxing/library';
import { Suspense, useEffect, useRef, useState } from "react";
import { toast } from "sonner";

function CheckInContent() {
  useRequireRole("ORGANIZER", { openModal: true, allowAdmin: true });
  const [selectedEventId, setSelectedEventId] = useState<string>("");
  const [page, setPage] = useState(0);
  const pageSize = 10;
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'PAID' | 'CHECKED_IN'>('ALL');
  const [pendingTicketId, setPendingTicketId] = useState<string | null>(null);

  const { data: eventsData, isLoading: eventsLoading } = useOrganizerEvents(0, 100);
  const events = eventsData?.content ?? [];

  const isHydrated = useAuthHydration();
  useEffect(() => {
    const firstId = (events as any)?.[0]?.id as string | undefined;
    if (isHydrated && !selectedEventId && firstId) {
      setSelectedEventId(firstId);
    }
  }, [isHydrated, events, selectedEventId]);

  const { data, isLoading, error, refetch } = useEventTickets(selectedEventId, 0, 100);

  const checkInMut = useCheckIn();

  useEffect(() => {
    if (checkInMut.isSuccess) {
      toast.success("Check-in thành công!");
      checkInMut.reset();
      if (selectedEventId) refetch();
      setPendingTicketId(null);
    }
    if (checkInMut.isError) {
      const anyErr = checkInMut.error as any;
      toast.error(anyErr?.response?.data?.message || anyErr?.message || "Không thể check-in vé");
      checkInMut.reset();
      setPendingTicketId(null);
    }
  }, [checkInMut.isSuccess, checkInMut.isError, checkInMut.error, checkInMut.reset, selectedEventId, refetch]);

  const tickets = (data as any)?.data?.content ?? [];
  const allowed = new Set(['PAID', 'CHECKED_IN']);
  const filteredBase = tickets.filter((t: any) => allowed.has(String(t.status)));
  const totalPaid = tickets.filter((t: any) => String(t.status) === 'PAID').length;
  const totalCheckedIn = tickets.filter((t: any) => String(t.status) === 'CHECKED_IN').length;
  const totalEligible = totalPaid + totalCheckedIn;
  const checkedInPercent = totalEligible > 0 ? Math.round((totalCheckedIn / totalEligible) * 100) : 0;
  const filteredByStatus = statusFilter === 'ALL' ? filteredBase : filteredBase.filter((t: any) => String(t.status) === statusFilter);
  const normalizedSearch = search.trim().toLowerCase();
  const filtered = normalizedSearch
    ? filteredByStatus.filter((t: any) => String(t.userName || '').toLowerCase().includes(normalizedSearch))
    : filteredByStatus;
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const pageStart = page * pageSize;
  const pageItems = filtered.slice(pageStart, pageStart + pageSize);

  const videoRef = useRef<HTMLVideoElement | null>(null);
  const [isScanning, setIsScanning] = useState(false);
  const [scanError, setScanError] = useState<string | null>(null);
  const [manualTicketNumber, setManualTicketNumber] = useState("");
  const zxingReader = useRef<BrowserMultiFormatReader | null>(null);

  useEffect(() => {
    let stream: MediaStream | null = null;
    let raf = 0;
    let detector: any = null as any;

    async function startScan() {
      try {
        let detector: any = null;
        let useZxing = false;

        if (typeof window !== 'undefined') {
          // @ts-ignore
          if (window.BarcodeDetector) {
            // @ts-ignore
            detector = new window.BarcodeDetector({ formats: ['qr_code'] });
          } else if ('BarcodeDetector' in window) {
            // @ts-ignore
            detector = new window.BarcodeDetector({ formats: ['qr_code'] });
          } else {
            useZxing = true;
            if (!zxingReader.current) {
              zxingReader.current = new BrowserMultiFormatReader();
            }
          }
        } else {
          setScanError('Trình duyệt không hỗ trợ quét QR. Hãy nhập mã vé thủ công.');
          return;
        }

        stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
        if (videoRef.current) {
          videoRef.current.srcObject = stream;
          await videoRef.current.play();
        }

        if (useZxing && zxingReader.current) {
          try {
            const result = await zxingReader.current.decodeFromVideoDevice(
              null,
              videoRef.current!,
              (result, error) => {
                if (result) {
                  setIsScanning(false);
                  if (selectedEventId) {
                    const { dto, error } = buildCheckInPayloadFromInput(result.getText(), selectedEventId);
                    if (error || !dto) {
                      setScanError(error || "QR không hợp lệ");
                      return;
                    }
                    setPendingTicketId(dto.ticketId || null);
                    checkInMut.mutate(dto);
                    toast.info("Đang xử lý check-in...");
                  } else {
                    toast.error("Vui lòng chọn sự kiện trước khi quét QR");
                  }
                }
                if (error && error.name !== 'NotFoundException') {
                  console.error('ZXing error:', error);
                }
              }
            );
          } catch (zxingError) {
            setScanError('Không thể khởi tạo quét QR. Hãy nhập mã vé thủ công.');
          }
        } else if (detector) {
          const tick = async () => {
            try {
              if (videoRef.current && detector) {
                const barcodes = await detector.detect(videoRef.current as any);
                if (barcodes && barcodes.length > 0) {
                  const raw = barcodes[0].rawValue;
                  if (raw) {
                    setIsScanning(false);
                    if (selectedEventId) {
                      const { dto, error } = buildCheckInPayloadFromInput(raw, selectedEventId);
                      if (error || !dto) {
                        setScanError(error || "QR không hợp lệ");
                        return;
                      }
                      setPendingTicketId(dto.ticketId || null);
                      checkInMut.mutate(dto);
                      toast.info("Đang xử lý check-in...");
                    } else {
                      toast.error("Vui lòng chọn sự kiện trước khi quét QR");
                    }
                    return;
                  }
                }
              }
            } catch (e) {
              // ignore frame errors
            }
            raf = requestAnimationFrame(tick);
          };
          raf = requestAnimationFrame(tick);
        }
      } catch (e: any) {
        setScanError(e?.message || 'Không thể truy cập camera');
      }
    }

    if (isScanning) startScan();

    return () => {
      if (raf) cancelAnimationFrame(raf);
      if (stream) {
        stream.getTracks().forEach((t) => t.stop());
      }
      if (zxingReader.current) {
        zxingReader.current.reset();
      }
    };
  }, [isScanning, selectedEventId, checkInMut]);

  function buildCheckInPayloadFromInput(inputRaw: string, eventIdForPage: string): { dto?: TicketCheckInRequestDto; error?: string } {
    const input = (inputRaw || "").trim();
    if (!eventIdForPage) return { error: "Vui lòng chọn sự kiện trước" };

    // QR format: TICKET:<ticketId>:<eventId>:<userId>
    if (input.toUpperCase().startsWith("TICKET:")) {
      const parts = input.split(":");
      if (parts.length < 4) {
        return { error: "QR không hợp lệ (thiếu dữ liệu)" };
      }
      const [_prefix, ticketId, eventIdInQr, userIdInQr] = parts;
      if (!ticketId) return { error: "QR không hợp lệ (thiếu ticketId)" };
      if (!eventIdInQr) return { error: "QR không hợp lệ (thiếu eventId)" };
      if (eventIdInQr !== eventIdForPage) {
        return { error: "QR thuộc sự kiện khác" };
      }
      return { dto: { eventId: eventIdForPage, ticketId, userId: userIdInQr } };
    }

    const validTicketNumber = /^[A-Za-z0-9\-]{4,64}$/;
    if (!validTicketNumber.test(input)) {
      return { error: "Mã vé không đúng định dạng" };
    }
    return { dto: { eventId: eventIdForPage, ticketNumber: input } };
  }

  useEffect(() => {
    setPage(0);
    setSearch("");
    setStatusFilter('ALL');
    if (selectedEventId) refetch();
  }, [selectedEventId, refetch]);

  if (!isHydrated || eventsLoading) {
    return <PageLoading message="Đang tải danh sách sự kiện..." />
  }

  if (events.length === 0) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
        <div className="container-page py-8">
          <div className="text-center py-12">
            <h1 className="text-2xl font-semibold text-gray-900 mb-4">Check-in</h1>
            <p className="text-gray-600 mb-6">Bạn chưa có sự kiện nào để thực hiện check-in</p>
            <a href="/organizer/events/new" className="inline-flex items-center px-6 py-3 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700">
              Tạo sự kiện đầu tiên
            </a>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="container-page py-8 space-y-6">
        <div className="mb-2">
          <h1 className="text-3xl font-bold text-gray-900">Check-in</h1>
          <p className="text-gray-600">Quét/đánh dấu check-in cho vé của sự kiện</p>
        </div>

        {/* Controls: Event + Search + Filter */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Chọn sự kiện</label>
              <select
                value={selectedEventId}
                onChange={(e) => setSelectedEventId(e.target.value)}
                className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">-- Chọn sự kiện --</option>
                {events.map((event: any) => (
                  <option key={event.id} value={event.id}>
                    {event.title} ({event.status})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Tìm theo tên người mua</label>
              <input
                value={search}
                onChange={(e) => { setSearch(e.target.value); setPage(0); }}
                placeholder="Nhập tên người mua..."
                className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Phân loại</label>
              <select
                value={statusFilter}
                onChange={(e) => { setStatusFilter(e.target.value as any); setPage(0); }}
                className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="ALL">Tất cả (PAID + CHECKED_IN)</option>
                <option value="PAID">Chưa check-in (PAID)</option>
                <option value="CHECKED_IN">Đã check-in (CHECKED_IN)</option>
              </select>
            </div>
          </div>
        </div>

        {selectedEventId && (
          <>
            {/* KPI Summary */}
            <div className="grid grid-cols-1 gap-6">
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mb-4">
                  <div>
                    <div className="text-sm text-gray-600">Đã check-in</div>
                    <div className="text-2xl font-bold text-gray-900">{totalCheckedIn}</div>
                  </div>
                  <div>
                    <div className="text-sm text-gray-600">Tổng vé hợp lệ</div>
                    <div className="text-2xl font-bold text-gray-900">{totalEligible}</div>
                  </div>
                  <div>
                    <div className="text-sm text-gray-600">Tỉ lệ</div>
                    <div className="text-2xl font-bold text-gray-900">{checkedInPercent}%</div>
                  </div>
                </div>
                <div>
                  <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
                    <div className="h-full bg-green-500" style={{ width: `${checkedInPercent}%` }} />
                  </div>
                  <div className="mt-2 text-sm text-gray-600">{totalCheckedIn}/{totalEligible} đã check-in</div>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-2">
              {/* QR Card */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-3">
                <h2 className="text-lg font-semibold">Quét mã QR</h2>
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => { setScanError(null); setIsScanning((s) => !s); }}
                    className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                  >{isScanning ? 'Dừng quét' : 'Bắt đầu quét'}</button>
                  {scanError && <span className="text-sm text-red-600">{scanError}</span>}
                </div>
                {isScanning && (
                  <div className="mt-2">
                    <video ref={videoRef} className="w-full max-w-md rounded-lg border" muted playsInline />
                  </div>
                )}
              </div>

              {/* Manual Card */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-3">
                <h2 className="text-lg font-semibold">Nhập mã vé thủ công</h2>
                <div className="flex items-center gap-3">
                  <input
                    value={manualTicketNumber}
                    onChange={(e) => setManualTicketNumber(e.target.value)}
                    placeholder="Nhập mã vé..."
                    className="flex-1 rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                  <button
                    onClick={() => {
                      const input = manualTicketNumber.trim();
                      if (!input) return;
                      const { dto, error } = buildCheckInPayloadFromInput(input, selectedEventId);
                      if (error || !dto) {
                        toast.error(error || "Mã vé không hợp lệ");
                        return;
                      }
                      setPendingTicketId(dto.ticketId || null);
                      checkInMut.mutate(dto);
                      setManualTicketNumber("");
                    }}
                    disabled={checkInMut.isPending}
                    className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-60"
                  >{checkInMut.isPending ? 'Đang check-in...' : 'Check-in'}</button>
                </div>
              </div>
            </div>

            {/* Tickets Table */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-100">
                  <thead className="bg-gray-50/50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Mã vé</th>
                      <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Người mua</th>
                      <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Loại vé</th>
                      <th className="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Trạng thái</th>
                      <th className="px-6 py-3" />
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {isLoading ? (
                      <tr><td className="px-6 py-6" colSpan={5}>Đang tải vé...</td></tr>
                    ) : error ? (
                      <tr><td className="px-6 py-6 text-red-600" colSpan={5}>Không thể tải vé.</td></tr>
                    ) : filtered.length === 0 ? (
                      <tr><td className="px-6 py-6 text-gray-600" colSpan={5}>Không có vé phù hợp.</td></tr>
                    ) : (
                      pageItems.map((t: TicketDto) => (
                        <tr key={t.id} className="hover:bg-gray-50/50">
                          <td className="px-6 py-3 text-sm font-mono">{t.ticketNumber || t.id}</td>
                          <td className="px-6 py-3 text-sm text-gray-900">{t.userName}</td>
                          <td className="px-6 py-3 text-sm text-gray-700">{t.ticketTypeName}</td>
                          <td className="px-6 py-3 text-sm text-gray-700">{t.status}</td>
                          <td className="px-6 py-3 text-right">
                            {t.status === 'CHECKED_IN' ? (
                              <span className="text-green-700 text-sm">Đã check-in</span>
                            ) : (
                              <button
                                onClick={() => { setPendingTicketId(t.id!); checkInMut.mutate({ eventId: selectedEventId, ticketId: t.id }) }}
                                disabled={checkInMut.isPending && pendingTicketId === t.id}
                                className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-60"
                              >
                                {checkInMut.isPending && pendingTicketId === t.id ? 'Đang check-in...' : 'Check-in'}
                              </button>
                            )}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
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
          </>
        )}
      </div>
    </div>
  );
}

export default function CheckInPage() {
  return (
    <Suspense fallback={<section className="py-10"><div className="container-page">Đang tải...</div></section>}>
      <CheckInContent />
    </Suspense>
  );
}
