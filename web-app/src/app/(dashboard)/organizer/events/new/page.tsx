"use client";

import { useCategories, useLocations } from "@/hooks/useFeaturedEvents";
import type { CategoryDto, CreateEventWithImagesBody, CreateEventWithImagesParams, EventCreateDto, TicketTypeDto } from "@/lib/api";
import { createEvent, createEventWithImages } from "@/lib/api/modules/events";
import { createTicketType } from "@/lib/api/modules/tickets";
import { organizerEventCreateSchema, organizerTicketTypeSchema, type OrganizerEventCreateInput, type OrganizerTicketTypeInput } from "@/lib/validation/organizer/events";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { Suspense, useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

const basicSchema = organizerEventCreateSchema;
const ticketTypeSchema = organizerTicketTypeSchema;

type BasicFormValues = OrganizerEventCreateInput;
type TicketTypeFormValues = OrganizerTicketTypeInput;

function CreateEventWizard() {
  const router = useRouter();
  const { data: categoriesData } = useCategories();
  const { data: locationPage } = useLocations();
  
  const categories = categoriesData?.categories ?? [];
  const locations = locationPage?.locations ?? [];

  const [step, setStep] = useState<number>(1);
  const [images, setImages] = useState<File[]>([]);
  const [ticketTypes, setTicketTypes] = useState<TicketTypeFormValues[]>([]);
  const [editingTicketType, setEditingTicketType] = useState<number | null>(null);

  const { register, handleSubmit, formState: { errors }, getValues, watch, setValue, trigger } = useForm<BasicFormValues>({ 
    resolver: zodResolver(basicSchema),
    mode: "onChange"
  });

  const watchedValues = watch();

  const saveEvent = useMutation({
    mutationFn: async (payload: { base: EventCreateDto; images?: File[] }) => {
      if (payload.images && payload.images.length > 0) {
        const formData: CreateEventWithImagesBody = { images: payload.images };
        const params: CreateEventWithImagesParams = { event: JSON.stringify(payload.base), primaryImageIndex: 0 };
        return createEventWithImages(formData, params);
      }
      return createEvent(payload.base);
    },
  });

  const validateCurrentStep = async () => {
    if (step === 1) {
      const isValid = await trigger();
      if (!isValid) {
        toast.error("Vui lòng kiểm tra lại thông tin trong form");
        return false;
      }
    }
    return true;
  };

  const handleNextStep = async () => {
    if (await validateCurrentStep()) {
      setStep(step + 1);
    }
  };

  const handlePrevStep = () => {
    setStep(step - 1);
  };

  const addTicketType = (ticketType: TicketTypeFormValues) => {
    if (editingTicketType !== null) {
      setTicketTypes(prev => prev.map((t, i) => i === editingTicketType ? ticketType : t));
      setEditingTicketType(null);
    } else {
      setTicketTypes(prev => [...prev, ticketType]);
    }
  };

  const removeTicketType = (index: number) => {
    setTicketTypes(prev => prev.filter((_, i) => i !== index));
  };

  const editTicketType = (index: number) => {
    setEditingTicketType(index);
  };

  const onSubmitAll = async () => {
    const base = getValues();
    if (!base) return;

    const start = new Date(base.startDate);
    const end = new Date(base.endDate);
    if (!(start instanceof Date) || isNaN(start.getTime()) || !(end instanceof Date) || isNaN(end.getTime())) {
      toast.error('Thời gian sự kiện không hợp lệ');
      return;
    }
    if (start >= end) {
      toast.error('Thời gian bắt đầu phải trước thời gian kết thúc');
      return;
    }

    if (!base.isFree && ticketTypes.length === 0) {
      toast.error('Vui lòng thêm ít nhất một loại vé (vì sự kiện không miễn phí)');
      setStep(3);
      return;
    }

    for (const [idx, tt] of ticketTypes.entries()) {
      if (!tt.name || String(tt.name).trim().length < 2) {
        toast.error(`Loại vé #${idx + 1}: Tên không hợp lệ`);
        setStep(3);
        return;
      }
      if (!base.isFree && (tt.price == null || Number(tt.price) < 0)) {
        toast.error(`Loại vé #${idx + 1}: Giá không hợp lệ`);
        setStep(3);
        return;
      }
      if (tt.quantity == null || Number(tt.quantity) < 1) {
        toast.error(`Loại vé #${idx + 1}: Số lượng phải >= 1`);
        setStep(3);
        return;
      }
      if (tt.salesStartDate) {
        const s = new Date(tt.salesStartDate);
        if (isNaN(s.getTime()) || s < start || s > end) {
          toast.error(`Loại vé #${idx + 1}: 'Bán từ' phải nằm trong khoảng thời gian sự kiện`);
          setStep(3);
          return;
        }
      }
      if (tt.salesEndDate) {
        const e = new Date(tt.salesEndDate);
        if (isNaN(e.getTime()) || e < start || e > end) {
          toast.error(`Loại vé #${idx + 1}: 'Bán đến' phải nằm trong khoảng thời gian sự kiện`);
          setStep(3);
          return;
        }
        if (tt.salesStartDate) {
          const s = new Date(tt.salesStartDate);
          if (s > e) {
            toast.error(`Loại vé #${idx + 1}: 'Bán từ' phải trước hoặc bằng 'Bán đến'`);
            setStep(3);
            return;
          }
        }
      }
    }

    try {
      toast.info("Đang tạo sự kiện...");
      
      const formattedBase = {
        ...base,
        startDate: base.startDate + ':00',
        endDate: base.endDate + ':00',
      } as EventCreateDto;
      
      const res = await saveEvent.mutateAsync({ base: formattedBase, images });
      const eventId = (res as any)?.data?.data?.id || (res as any)?.data?.id || (res as any)?.data?.data;
      if (eventId && ticketTypes.length > 0) {
        toast.info("Đang tạo loại vé...");
        for (const tt of ticketTypes) {
          const dto: TicketTypeDto = {
            eventId,
            name: tt.name!,
            description: tt.description,
            price: tt.price!,
            quantity: tt.quantity!,
            minTicketsPerOrder: tt.minTicketsPerOrder,
            maxTicketsPerCustomer: tt.maxTicketsPerCustomer,
            isVIP: !!tt.isVIP,
            isEarlyBird: !!tt.isEarlyBird,
            isActive: tt.isActive ?? true,
            salesStartDate: tt.salesStartDate ? tt.salesStartDate + ':00' : undefined,
            salesEndDate: tt.salesEndDate ? tt.salesEndDate + ':00' : undefined,
            quantitySold: 0,
          };
          try { await createTicketType(eventId, dto); } catch {}
        }
      }
      toast.success("Tạo sự kiện thành công");
      if (eventId) router.push(`/organizer/events/${eventId}/edit`); else router.push("/organizer");
    } catch (e: any) {
      toast.error(e?.response?.data?.message || "Tạo sự kiện thất bại");
    }
  };

  return (
    <section className="py-10">
      <div className="container-page max-w-4xl space-y-6">
        {/* Header với progress bar */}
        <div className="space-y-4">
          <div>
            <h1 className="text-2xl font-semibold">Tạo sự kiện mới</h1>
            <p className="text-slate-600">Bước {step} / 4</p>
          </div>
          
          {/* Progress bar */}
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div 
              className="bg-blue-600 h-2 rounded-full transition-all duration-300" 
              style={{ width: `${(step / 4) * 100}%` }}
            ></div>
          </div>
          
          {/* Step indicators */}
          <div className="flex justify-between text-sm">
            <span className={step >= 1 ? "text-blue-600 font-medium" : "text-gray-400"}>Thông tin cơ bản</span>
            <span className={step >= 2 ? "text-blue-600 font-medium" : "text-gray-400"}>Hình ảnh</span>
            <span className={step >= 3 ? "text-blue-600 font-medium" : "text-gray-400"}>Loại vé</span>
            <span className={step >= 4 ? "text-blue-600 font-medium" : "text-gray-400"}>Xác nhận</span>
          </div>
        </div>

        {/* Step 1: Thông tin cơ bản */}
        {step === 1 && (
          <form onSubmit={handleSubmit(handleNextStep)} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium mb-2">Tiêu đề sự kiện *</label>
                <input 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.title ? 'border-red-500' : 'border-gray-300'}`} 
                  placeholder="Nhập tiêu đề sự kiện..."
                  {...register("title")} 
                />
                {errors.title && <p className="text-sm text-red-600 mt-1">{errors.title.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Danh mục *</label>
                <select 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.categoryId ? 'border-red-500' : 'border-gray-300'}`}
                  {...register("categoryId")}
                >
                  <option value="">Chọn danh mục</option>
                  {categories.map((category: CategoryDto) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
                {errors.categoryId && <p className="text-sm text-red-600 mt-1">{errors.categoryId.message}</p>}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">Mô tả ngắn *</label>
              <input 
                className={`w-full rounded-lg border px-4 py-3 ${errors.shortDescription ? 'border-red-500' : 'border-gray-300'}`} 
                placeholder="Mô tả ngắn gọn về sự kiện..."
                {...register("shortDescription")} 
              />
              {errors.shortDescription && <p className="text-sm text-red-600 mt-1">{errors.shortDescription.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">Mô tả chi tiết *</label>
              <textarea 
                className={`w-full rounded-lg border px-4 py-3 min-h-32 ${errors.description ? 'border-red-500' : 'border-gray-300'}`} 
                placeholder="Mô tả chi tiết về sự kiện..."
                {...register("description")} 
              />
              {errors.description && <p className="text-sm text-red-600 mt-1">{errors.description.message}</p>}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium mb-2">Địa điểm *</label>
                <select 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.locationId ? 'border-red-500' : 'border-gray-300'}`}
                  {...register("locationId")}
                >
                  <option value="">Chọn địa điểm</option>
                  {locations.map((location: any) => (
                    <option key={location.id} value={location.id}>
                      {location.name || location.locationName || location.address}
                    </option>
                  ))}
                </select>
                {errors.locationId && <p className="text-sm text-red-600 mt-1">{errors.locationId.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Thành phố *</label>
                <input 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.city ? 'border-red-500' : 'border-gray-300'}`} 
                  placeholder="Nhập thành phố..."
                  {...register("city")} 
                />
                {errors.city && <p className="text-sm text-red-600 mt-1">{errors.city.message}</p>}
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div>
                <label className="block text-sm font-medium mb-2">Địa chỉ *</label>
                <input 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.address ? 'border-red-500' : 'border-gray-300'}`} 
                  placeholder="Nhập địa chỉ chi tiết..."
                  {...register("address")} 
                />
                {errors.address && <p className="text-sm text-red-600 mt-1">{errors.address.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Vĩ độ</label>
                <input 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.latitude ? 'border-red-500' : 'border-gray-300'}`} 
                  type="number" 
                  step="any" 
                  placeholder="10.762622"
                  {...register("latitude")} 
                />
                {errors.latitude && <p className="text-sm text-red-600 mt-1">{errors.latitude.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Kinh độ</label>
                <input 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.longitude ? 'border-red-500' : 'border-gray-300'}`} 
                  type="number" 
                  step="any" 
                  placeholder="106.660172"
                  {...register("longitude")} 
                />
                {errors.longitude && <p className="text-sm text-red-600 mt-1">{errors.longitude.message}</p>}
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div>
                <label className="block text-sm font-medium mb-2">Số người tối đa *</label>
                <input 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.maxAttendees ? 'border-red-500' : 'border-gray-300'}`} 
                  type="number" 
                  placeholder="1000"
                  {...register("maxAttendees")} 
                />
                {errors.maxAttendees && <p className="text-sm text-red-600 mt-1">{errors.maxAttendees.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Thời gian bắt đầu *</label>
                <input 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.startDate ? 'border-red-500' : 'border-gray-300'}`} 
                  type="datetime-local" 
                  min={new Date().toISOString().slice(0, 16)}
                  {...register("startDate")} 
                />
                {errors.startDate && <p className="text-sm text-red-600 mt-1">{errors.startDate.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Thời gian kết thúc *</label>
                <input 
                  className={`w-full rounded-lg border px-4 py-3 ${errors.endDate ? 'border-red-500' : 'border-gray-300'}`} 
                  type="datetime-local" 
                  min={new Date().toISOString().slice(0, 16)}
                  {...register("endDate")} 
                />
                {errors.endDate && <p className="text-sm text-red-600 mt-1">{errors.endDate.message}</p>}
              </div>
            </div>

            <div className="flex flex-wrap gap-6">
              <label className="inline-flex items-center gap-3">
                <input type="checkbox" {...register("isFree")} className="w-4 h-4 text-blue-600" />
                <span className="text-sm font-medium">Miễn phí</span>
              </label>
              <label className="inline-flex items-center gap-3">
                <input type="checkbox" defaultChecked {...register("isDraft")} className="w-4 h-4 text-blue-600" />
                <span className="text-sm font-medium">Lưu nháp</span>
              </label>
              <label className="inline-flex items-center gap-3">
                <input type="checkbox" {...register("isPrivate")} className="w-4 h-4 text-blue-600" />
                <span className="text-sm font-medium">Sự kiện riêng tư</span>
              </label>
            </div>

            <div className="flex items-center justify-end gap-4 pt-6">
              <button 
                type="button" 
                onClick={() => router.back()} 
                className="rounded-lg border border-gray-300 px-6 py-3 text-gray-700 hover:bg-gray-50"
              >
                Hủy
              </button>
              <button 
                type="submit"
                className="rounded-lg bg-blue-600 px-6 py-3 text-white hover:bg-blue-700"
              >
                Tiếp tục
              </button>
            </div>
          </form>
        )}

        {/* Step 2: Hình ảnh */}
        {step === 2 && (
          <div className="space-y-6">
            <div>
              <h2 className="text-xl font-semibold mb-2">Hình ảnh sự kiện</h2>
              <p className="text-slate-600">Tải lên hình ảnh cho sự kiện (tối đa 5 ảnh)</p>
            </div>
            
            <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
              <input 
                className="hidden" 
                type="file" 
                multiple 
                accept="image/*" 
                id="event-images"
                onChange={(e) => {
                  const files = Array.from(e.target.files || []).slice(0, 5);
                  setImages(files);
                }}
              />
              <label htmlFor="event-images" className="cursor-pointer">
                <div className="space-y-4">
                  <div className="mx-auto w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center">
                    <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                    </svg>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-900">Chọn hình ảnh</p>
                    <p className="text-sm text-gray-500">PNG, JPG, GIF tối đa 10MB</p>
                  </div>
                </div>
              </label>
            </div>

            {images.length > 0 && (
              <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                {images.map((file, index) => (
                  <div key={index} className="relative">
                    <img 
                      src={URL.createObjectURL(file)} 
                      alt={`Preview ${index + 1}`}
                      className="w-full h-24 object-cover rounded-lg"
                    />
                    <button
                      onClick={() => setImages(prev => prev.filter((_, i) => i !== index))}
                      className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-red-600"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            )}

            <div className="flex items-center justify-between pt-6">
              <button 
                onClick={handlePrevStep} 
                className="rounded-lg border border-gray-300 px-6 py-3 text-gray-700 hover:bg-gray-50"
              >
                Quay lại
              </button>
              <button 
                onClick={handleNextStep}
                className="rounded-lg bg-blue-600 px-6 py-3 text-white hover:bg-blue-700"
              >
                Tiếp tục
              </button>
            </div>
          </div>
        )}

        {/* Step 3: Loại vé */}
        {step === 3 && (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-semibold mb-2">Loại vé</h2>
                <p className="text-slate-600">
                  {!watchedValues.isFree ? 'Thêm các loại vé cho sự kiện' : 'Thêm các loại vé (có thể miễn phí)'}
                </p>
              </div>
              <button
                onClick={() => setEditingTicketType(null)}
                className="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
              >
                Thêm loại vé
              </button>
            </div>

            {/* Form thêm/sửa loại vé */}
            <TicketTypeForm 
              onSubmit={addTicketType}
              editingTicketType={editingTicketType !== null ? ticketTypes[editingTicketType] : null}
              onCancel={() => setEditingTicketType(null)}
            />

            {/* Danh sách loại vé */}
            {ticketTypes.length === 0 ? (
              <div className="text-center py-8 text-slate-500">
                <p>Chưa có loại vé nào. Hãy thêm loại vé đầu tiên.</p>
              </div>
            ) : (
              <div className="space-y-4">
                {ticketTypes.map((ticketType, index) => (
                  <div key={index} className="border rounded-lg p-4 bg-gray-50">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <h3 className="font-medium">{ticketType.name}</h3>
                        <p className="text-sm text-gray-600">{ticketType.description}</p>
                        <div className="flex items-center gap-4 mt-2 text-sm">
                          <span className="font-medium">
                            {watchedValues.isFree ? 'Miễn phí' : `${ticketType.price?.toLocaleString('vi-VN')} VNĐ`}
                          </span>
                          <span>Số lượng: {ticketType.quantity}</span>
                          {ticketType.isVIP && <span className="bg-purple-100 text-purple-800 px-2 py-1 rounded">VIP</span>}
                          {ticketType.isEarlyBird && <span className="bg-green-100 text-green-800 px-2 py-1 rounded">Early Bird</span>}
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => editTicketType(index)}
                          className="text-blue-600 hover:text-blue-800 text-sm"
                        >
                          Sửa
                        </button>
                        <button
                          onClick={() => removeTicketType(index)}
                          className="text-red-600 hover:text-red-800 text-sm"
                        >
                          Xóa
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            <div className="flex items-center justify-between pt-6">
              <button 
                onClick={handlePrevStep} 
                className="rounded-lg border border-gray-300 px-6 py-3 text-gray-700 hover:bg-gray-50"
              >
                Quay lại
              </button>
              <button 
                onClick={handleNextStep}
                className="rounded-lg bg-blue-600 px-6 py-3 text-white hover:bg-blue-700"
              >
                Tiếp tục
              </button>
            </div>
          </div>
        )}

        {/* Step 4: Xác nhận */}
        {step === 4 && (
          <div className="space-y-6">
            <div>
              <h2 className="text-xl font-semibold mb-2">Xem lại & Tạo sự kiện</h2>
              <p className="text-slate-600">Kiểm tra lại thông tin trước khi tạo sự kiện</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <h3 className="font-semibold text-lg">Thông tin cơ bản</h3>
                <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                  <div><span className="font-medium">Tiêu đề:</span> {getValues("title")}</div>
                  <div><span className="font-medium">Danh mục:</span> {categories.find(c => c.id === getValues("categoryId"))?.name}</div>
                  <div><span className="font-medium">Thời gian:</span> {new Date(getValues("startDate")).toLocaleString('vi-VN')} → {new Date(getValues("endDate")).toLocaleString('vi-VN')}</div>
                  <div><span className="font-medium">Địa điểm:</span> {locations.find(l => l.id === getValues("locationId"))?.name || getValues("address")}</div>
                  <div><span className="font-medium">Số người tối đa:</span> {getValues("maxAttendees")}</div>
                  <div><span className="font-medium">Miễn phí:</span> {getValues("isFree") ? 'Có' : 'Không'}</div>
                </div>
              </div>

              <div className="space-y-4">
                <h3 className="font-semibold text-lg">Tóm tắt</h3>
                <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                  <div><span className="font-medium">Hình ảnh:</span> {images.length} ảnh</div>
                  <div><span className="font-medium">Loại vé:</span> {ticketTypes.length} loại</div>
                  <div><span className="font-medium">Trạng thái:</span> {getValues("isDraft") ? 'Nháp' : 'Xuất bản ngay'}</div>
                  <div><span className="font-medium">Quyền riêng tư:</span> {getValues("isPrivate") ? 'Riêng tư' : 'Công khai'}</div>
                </div>
              </div>
            </div>

            {ticketTypes.length > 0 && (
              <div>
                <h3 className="font-semibold text-lg mb-4">Loại vé</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {ticketTypes.map((ticketType, index) => (
                    <div key={index} className="border rounded-lg p-4">
                      <h4 className="font-medium">{ticketType.name}</h4>
                      <p className="text-sm text-gray-600">{ticketType.description}</p>
                      <div className="flex items-center gap-4 mt-2 text-sm">
                        <span className="font-medium">
                          {watchedValues.isFree ? 'Miễn phí' : `${ticketType.price?.toLocaleString('vi-VN')} VNĐ`}
                        </span>
                        <span>Số lượng: {ticketType.quantity}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div className="flex items-center justify-between pt-6">
              <button 
                onClick={handlePrevStep} 
                className="rounded-lg border border-gray-300 px-6 py-3 text-gray-700 hover:bg-gray-50"
              >
                Quay lại
              </button>
              <button 
                onClick={onSubmitAll} 
                disabled={saveEvent.isPending}
                className="rounded-lg bg-green-600 px-6 py-3 text-white hover:bg-green-700 disabled:opacity-50"
              >
                {saveEvent.isPending ? 'Đang tạo...' : 'Tạo sự kiện'}
              </button>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}

// Component form loại vé
function TicketTypeForm({ 
  onSubmit, 
  editingTicketType, 
  onCancel 
}: { 
  onSubmit: (data: TicketTypeFormValues) => void;
  editingTicketType: TicketTypeFormValues | null;
  onCancel: () => void;
}) {
  const { register, handleSubmit, formState: { errors }, reset } = useForm<TicketTypeFormValues>({
    resolver: zodResolver(ticketTypeSchema),
    defaultValues: editingTicketType || {
      name: '',
      description: '',
      price: 0,
      quantity: 1,
      isActive: true,
      isVIP: false,
      isEarlyBird: false,
    }
  });

  // Reset form khi editingTicketType thay đổi
  useEffect(() => {
    if (editingTicketType) {
      reset(editingTicketType);
    } else {
      reset({
        name: '',
        description: '',
        price: 0,
        quantity: 1,
        isActive: true,
        isVIP: false,
        isEarlyBird: false,
      });
    }
  }, [editingTicketType, reset]);

  const handleFormSubmit = (data: TicketTypeFormValues) => {
    onSubmit(data);
    reset();
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="border rounded-lg p-6 bg-white">
      <h3 className="font-semibold mb-4">{editingTicketType ? 'Sửa loại vé' : 'Thêm loại vé mới'}</h3>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium mb-2">Tên loại vé *</label>
          <input 
            className={`w-full rounded-lg border px-3 py-2 ${errors.name ? 'border-red-500' : 'border-gray-300'}`}
            placeholder="Ví dụ: Vé thường, VIP, Early Bird..."
            {...register("name")}
          />
          {errors.name && <p className="text-sm text-red-600 mt-1">{errors.name.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium mb-2">Giá (VNĐ) *</label>
          <input 
            type="number"
            className={`w-full rounded-lg border px-3 py-2 ${errors.price ? 'border-red-500' : 'border-gray-300'}`}
            placeholder="0"
            {...register("price")}
          />
          {errors.price && <p className="text-sm text-red-600 mt-1">{errors.price.message}</p>}
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium mb-2">Mô tả</label>
        <textarea 
          className={`w-full rounded-lg border px-3 py-2 ${errors.description ? 'border-red-500' : 'border-gray-300'}`}
          placeholder="Mô tả về loại vé này..."
          rows={2}
          {...register("description")}
        />
        {errors.description && <p className="text-sm text-red-600 mt-1">{errors.description.message}</p>}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label className="block text-sm font-medium mb-2">Số lượng *</label>
          <input 
            type="number"
            className={`w-full rounded-lg border px-3 py-2 ${errors.quantity ? 'border-red-500' : 'border-gray-300'}`}
            placeholder="100"
            {...register("quantity")}
          />
          {errors.quantity && <p className="text-sm text-red-600 mt-1">{errors.quantity.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium mb-2">Min/đơn hàng</label>
          <input 
            type="number"
            className={`w-full rounded-lg border px-3 py-2 ${errors.minTicketsPerOrder ? 'border-red-500' : 'border-gray-300'}`}
            placeholder="1"
            {...register("minTicketsPerOrder")}
          />
          {errors.minTicketsPerOrder && <p className="text-sm text-red-600 mt-1">{errors.minTicketsPerOrder.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium mb-2">Max/khách hàng</label>
          <input 
            type="number"
            className={`w-full rounded-lg border px-3 py-2 ${errors.maxTicketsPerCustomer ? 'border-red-500' : 'border-gray-300'}`}
            placeholder="10"
            {...register("maxTicketsPerCustomer")}
          />
          {errors.maxTicketsPerCustomer && <p className="text-sm text-red-600 mt-1">{errors.maxTicketsPerCustomer.message}</p>}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium mb-2">Bán từ</label>
          <input 
            type="datetime-local"
            className={`w-full rounded-lg border px-3 py-2 ${errors.salesStartDate ? 'border-red-500' : 'border-gray-300'}`}
            {...register("salesStartDate")}
          />
          {errors.salesStartDate && <p className="text-sm text-red-600 mt-1">{errors.salesStartDate.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium mb-2">Bán đến</label>
          <input 
            type="datetime-local"
            className={`w-full rounded-lg border px-3 py-2 ${errors.salesEndDate ? 'border-red-500' : 'border-gray-300'}`}
            {...register("salesEndDate")}
          />
          {errors.salesEndDate && <p className="text-sm text-red-600 mt-1">{errors.salesEndDate.message}</p>}
        </div>
      </div>

      <div className="flex flex-wrap gap-6 mt-4">
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

      <div className="flex items-center gap-3 pt-4">
        <button 
          type="submit"
          className="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
        >
          {editingTicketType ? 'Cập nhật' : 'Thêm loại vé'}
        </button>
        {editingTicketType && (
          <button 
            type="button" 
            onClick={onCancel}
            className="rounded-lg border border-gray-300 px-4 py-2 text-gray-700 hover:bg-gray-50"
          >
            Hủy
          </button>
        )}
      </div>
    </form>
  );
}

export default function NewEventPage() {
  return (
    <Suspense fallback={<section className="py-10"><div className="container-page">Đang tải...</div></section>}>
      <CreateEventWizard />
    </Suspense>
  );
}



