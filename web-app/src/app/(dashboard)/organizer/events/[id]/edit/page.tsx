"use client";

import { useCancelEvent, useDeleteEvent, useDeleteEventImage, useEventDetail, useEventImages, usePublishEvent, useSaveCloudinaryImage, useUpdateEvent, useUploadEventImage } from "@/hooks/useEvents";
import { useCategories, useLocations } from "@/hooks/useFeaturedEvents";
import { useRequireRole } from "@/hooks/useRequireRole";
import { type EventUpdateDto } from "@/lib/api";
import { organizerEventUpdateSchema, type OrganizerEventUpdateInput } from "@/lib/validation/organizer/events";
import { zodResolver } from "@hookform/resolvers/zod";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { Suspense, useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

const schema = organizerEventUpdateSchema;
type FormValues = OrganizerEventUpdateInput;

function EditEventContent() {
  useRequireRole("ORGANIZER");
  const params = useParams();
  const router = useRouter();
  const eventId = params.id as string;

  const { register, handleSubmit, reset } = useForm<FormValues>({ resolver: zodResolver(schema) });

  const { data: eventData, isLoading: isLoadingEvent, error: loadError } = useEventDetail(eventId);
  const { data: images = [], refetch: refetchImages } = useEventImages(eventId);
  const { data: categoriesData } = useCategories();
  const { data: locationsData } = useLocations();

  function toLocalDateTimeInput(value?: string) {
    if (!value) return "";
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return "";
    const pad = (n: number) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  useEffect(() => {
    if (eventData) {
      reset({
        title: eventData.title as any,
        shortDescription: (eventData as any).shortDescription,
        description: (eventData as any).description,
        categoryId: (eventData as any).categoryId as any,
        locationId: (eventData as any).locationId as any,
        address: (eventData as any).address,
        city: (eventData as any).city,
        latitude: (eventData as any).latitude as any,
        longitude: (eventData as any).longitude as any,
        maxAttendees: (eventData as any).maxAttendees as any,
        startDate: toLocalDateTimeInput((eventData as any).startDate),
        endDate: toLocalDateTimeInput((eventData as any).endDate),
      });
      setTimeout(() => refetchImages(), 0);
    }
  }, [eventData, reset, refetchImages]);

  const updateMut = useUpdateEvent(eventId);
  const publishMut = usePublishEvent();
  const cancelMut = useCancelEvent();
  const uploadMut = useUploadEventImage(eventId);
  const saveCloudinaryMut = useSaveCloudinaryImage(eventId);
  const deleteEventMut = useDeleteEvent();
  const deleteImageMut = useDeleteEventImage(eventId);

  const onSubmit = (values: FormValues) => {
    const toBackend = (s?: string) => {
      if (!s) return s as any;
      const base = s.replace('T', ' ');
      return base.length === 16 ? `${base}:00` : base;
    };
    const dto: EventUpdateDto = { 
      ...values,
      startDate: toBackend(values.startDate) as any,
      endDate: toBackend(values.endDate) as any,
    };
    updateMut.mutate(dto, { onSuccess: () => {
      router.push(`/organizer/events`);
      try { (window as any).next?.router?.refresh?.(); } catch {}
      toast.success("Sự kiện đã được cập nhật thành công!");
    }, onError: (error) => {
      toast.error(error?.message || "Không thể cập nhật sự kiện");
    }});
  };

  const handlePublish = async () => {
    try {
      await publishMut.mutateAsync(eventId);
      toast.success("Sự kiện đã được xuất bản thành công!");
      router.push('/organizer/events');
    } catch (error: any) {
      toast.error(error?.message || "Không thể xuất bản sự kiện");
    }
  };

  const handleCancel = async () => {
    const reason = prompt("Lý do hủy sự kiện:");
    if (!reason) return;
    
    try {
      await cancelMut.mutateAsync({ eventId: eventId, reason });
      toast.success("Sự kiện đã được hủy thành công!");
      router.push('/organizer/events');
    } catch (error: any) {
      toast.error(error?.message || "Không thể hủy sự kiện");
    }
  };

  const handleDelete = async () => {
    if (!confirm('Bạn chắc chắn muốn xóa sự kiện này?')) return;
    
    try {
      await deleteEventMut.mutateAsync(eventId);
      toast.success("Sự kiện đã được xóa thành công!");
      router.push('/organizer/events');
    } catch (error: any) {
      toast.error(error?.message || "Không thể xóa sự kiện");
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="container-page py-8">
        {/* Header Section */}
        <div className="mb-8">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
            <div className="mb-4 sm:mb-0">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">Chỉnh sửa sự kiện</h1>
              <p className="text-gray-600 text-lg">Cập nhật thông tin, xuất bản hoặc hủy sự kiện</p>
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

        <div className="max-w-4xl mx-auto">
          {isLoadingEvent ? (
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-8 text-gray-600">Đang tải dữ liệu sự kiện...</div>
          ) : loadError ? (
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-8 text-red-600">Không thể tải dữ liệu sự kiện.</div>
          ) : null}
          {/* Event Information Form */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-8">
            <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center">
              <svg className="w-5 h-5 mr-2 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Thông tin sự kiện
            </h2>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Tiêu đề sự kiện</label>
                  <input 
                    className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                    placeholder="Nhập tiêu đề sự kiện..."
                    {...register("title")} 
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Danh mục</label>
                  <select 
                    className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                    {...register("categoryId")}
                  >
                    <option value="">Chọn danh mục...</option>
                    {((categoriesData as any)?.categories || []).map((c: any) => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                  {eventData && (
                    <p className="text-xs text-gray-500 mt-1">Hiện tại: {(eventData as any).categoryName || (eventData as any).category?.name}</p>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Mô tả ngắn</label>
                <input 
                  className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                  placeholder="Mô tả ngắn gọn về sự kiện..."
                  {...register("shortDescription")} 
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Mô tả chi tiết</label>
                <textarea 
                  className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200 min-h-32" 
                  placeholder="Mô tả chi tiết về sự kiện..."
                  {...register("description")} 
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Địa điểm</label>
                  <select 
                    className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                    {...register("locationId")}
                  >
                    <option value="">Chọn địa điểm...</option>
                    {(((locationsData as any)?.locations) || []).map((l: any) => (
                      <option key={l.id} value={l.id}>{l.name || l.locationName || l.address}</option>
                    ))}
                  </select>
                  {eventData && (
                    <p className="text-xs text-gray-500 mt-1">Hiện tại: {(eventData as any).locationName || (eventData as any).location?.name}</p>
                  )}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Địa chỉ</label>
                  <input 
                    className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                    placeholder="Địa chỉ chi tiết..."
                    {...register("address")} 
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Thành phố</label>
                  <input 
                    className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                    placeholder="Tên thành phố..."
                    {...register("city")} 
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Vĩ độ</label>
                  <input 
                    className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                    type="number" 
                    step="any" 
                    placeholder="0.000000"
                    {...register("latitude")} 
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Kinh độ</label>
                  <input 
                    className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                    type="number" 
                    step="any" 
                    placeholder="0.000000"
                    {...register("longitude")} 
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Số lượng tối đa</label>
                  <input 
                    className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                    type="number" 
                    placeholder="100"
                    {...register("maxAttendees")} 
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Thời gian bắt đầu</label>
                  <input 
                    className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                    type="datetime-local" 
                    {...register("startDate")} 
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Thời gian kết thúc</label>
                <input 
                  className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                  type="datetime-local" 
                  {...register("endDate")} 
                />
              </div>

              <div className="flex items-center justify-between pt-6 border-t border-gray-200">
                <div className="flex items-center space-x-4">
                  <button 
                    type="submit"
                    className="inline-flex items-center px-6 py-3 border border-transparent text-sm font-medium rounded-lg text-white bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200 shadow-sm hover:shadow-md"
                    disabled={updateMut.isPending}
                  >
                    {updateMut.isPending ? (
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    ) : (
                      <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                    )}
                    Lưu thay đổi
                  </button>
                  
                  {!(eventData as any)?.status || (eventData as any)?.status === 'DRAFT' ? (
                    <button 
                      type="button" 
                      onClick={handlePublish} 
                      className="inline-flex items-center px-6 py-3 border border-gray-300 shadow-sm text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
                      disabled={publishMut.isPending}
                    >
                      {publishMut.isPending ? (
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mr-2"></div>
                      ) : (
                        <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10" />
                        </svg>
                      )}
                      Xuất bản
                    </button>
                  ) : null}
                </div>
                
                {(eventData as any)?.status !== 'CANCELLED' ? (
                  <button 
                    type="button" 
                    onClick={handleCancel} 
                    className="inline-flex items-center px-6 py-3 border border-red-300 shadow-sm text-sm font-medium rounded-lg text-red-700 bg-white hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-all duration-200"
                    disabled={cancelMut.isPending}
                  >
                    {cancelMut.isPending ? (
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-red-600 mr-2"></div>
                    ) : (
                      <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    )}
                    Hủy sự kiện
                  </button>
                ) : (
                  <button 
                    type="button" 
                    onClick={handleDelete} 
                    className="inline-flex items-center px-6 py-3 border border-red-300 shadow-sm text-sm font-medium rounded-lg text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-all duration-200"
                    disabled={deleteEventMut.isPending}
                  >
                    Xoá sự kiện
                  </button>
                )}
              </div>
            </form>
          </div>

          {/* Images Management */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center">
              <svg className="w-5 h-5 mr-2 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              Quản lý ảnh sự kiện
            </h2>

            <div className="space-y-6">
              {/* Current Images */}
              {Array.isArray(images) && images.length > 0 && (
                <div>
                  <h3 className="text-lg font-medium text-gray-900 mb-4">Ảnh hiện có</h3>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    {images.map((img: any) => (
                      <div key={img.id} className="relative group">
                        <img src={img.secureUrl || img.url} alt={img.publicId || img.id} className="w-full h-28 object-cover rounded-lg border" />
                        <div className="absolute top-2 right-2 flex items-center gap-2 opacity-0 group-hover:opacity-100 transition">
                          {img.isPrimary && (
                            <span className="px-2 py-0.5 text-xs rounded bg-blue-600 text-white">Chính</span>
                          )}
                          <button
                            type="button"
                            onClick={() => deleteImageMut.mutate(img.id)}
                            className="px-2 py-1 text-xs rounded bg-red-600 text-white hover:bg-red-700"
                          >Xoá</button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
              {/* File Upload */}
              <div className="bg-gray-50 rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Tải ảnh lên</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">Chọn file ảnh</label>
                    <input 
                      type="file" 
                      accept="image/*" 
                      id="event-image-file" 
                      className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200" 
                    />
                    <div className="mt-4 flex items-center gap-4">
                      <label className="inline-flex items-center gap-2">
                        <input type="checkbox" id="event-image-primary" className="w-4 h-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500" />
                        <span className="text-sm font-medium text-gray-700">Đặt làm ảnh chính</span>
                      </label>
                      <button
                        type="button"
                        onClick={() => {
                          const fileInput = document.getElementById('event-image-file') as HTMLInputElement;
                          const primaryInput = document.getElementById('event-image-primary') as HTMLInputElement;
                          const file = fileInput?.files?.[0];
                          if (file) uploadMut.mutate({ file, isPrimary: primaryInput?.checked });
                        }}
                        className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
                        disabled={uploadMut.isPending}
                      >
                        {uploadMut.isPending ? (
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                        ) : (
                          <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10" />
                          </svg>
                        )}
                        Tải ảnh
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              {/* Cloudinary integration removed */}

              {/* Danger Zone */}
              <div className="bg-red-50 rounded-lg p-6 border border-red-200">
                <h3 className="text-lg font-semibold text-red-800 mb-2">Xóa sự kiện</h3>
                <p className="text-sm text-red-700 mb-4">Hành động này không thể hoàn tác. Sự kiện và dữ liệu liên quan có thể bị xoá theo chính sách backend.</p>
                <button
                  type="button"
                  onClick={handleDelete}
                  className="inline-flex items-center px-4 py-2 rounded-md bg-red-600 text-white hover:bg-red-700"
                  disabled={deleteEventMut.isPending}
                >
                  Xóa sự kiện
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function EditEventPage() {
  return (
    <Suspense fallback={<section className="py-10"><div className="container-page">Đang tải...</div></section>}>
      <EditEventContent />
    </Suspense>
  );
}


