import SectionHeading from "@/components/SectionHeading";
import EventCard from "@/components/EventCard";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { useUpcomingEvents } from "@/hooks/useUpcomingEvents";
import { sanitizeEventImageUrl } from "@/lib/utils/image";

export default function UpcomingEventsSection() {
  const { data, isLoading, isError } = useUpcomingEvents(6);
  const mapped = (data?.data?.data ?? []).map((e) => ({
    id: e.id ?? "",
    title: e.title ?? "",
    image: sanitizeEventImageUrl(e.featuredImageUrl, e.imageUrls),
    date: e.startDate ? new Date(e.startDate).toLocaleString("vi-VN", { dateStyle: "medium", timeStyle: "short" }) : "",
    location: e.city ?? "",
    price: e.isFree ? 0 : e.minTicketPrice ?? 0,
    category: e.categoryName ?? "",
  }));
  
  return (
    <section className="py-8 sm:py-12">
      <SectionHeading title="Sắp diễn ra" subtitle="Đừng bỏ lỡ những sự kiện sắp tới" />
      {isLoading ? (
        <div className="container-page flex justify-center py-8">
          <div className="flex flex-col items-center space-y-4">
            <LoadingSpinner size="lg" />
            <p className="text-gray-600">Đang tải sự kiện sắp diễn ra...</p>
          </div>
        </div>
      ) : (
        <div className="container-page grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {mapped.length > 0 ? (
            mapped.map((e: any) => <EventCard key={e.id} event={e} />)
          ) : (
            <div className="col-span-full text-center py-8">
              <p className="text-gray-600">
                {isError ? "Không thể tải sự kiện. Vui lòng thử lại sau." : "Chưa có sự kiện sắp diễn ra."}
              </p>
            </div>
          )}
        </div>
      )}
    </section>
  );
}


