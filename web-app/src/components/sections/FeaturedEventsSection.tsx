import SectionHeading from "@/components/SectionHeading";
import EventCard from "@/components/EventCard";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { useFeaturedEvents } from "@/hooks/useFeaturedEvents";
import { sanitizeEventImageUrl } from "@/lib/utils/image";

type Props = {
  activeCategory: string | null;
};

export default function FeaturedEventsSection({ activeCategory }: Props) {
  const { data, isLoading, isError } = useFeaturedEvents(6);
  const apiMapped = (data ?? []).map((e: any) => ({
    id: e.id ?? "",
    title: e.title ?? "",
    image: sanitizeEventImageUrl(e.featuredImageUrl, e.imageUrls),
    date: e.startDate ? new Date(e.startDate).toLocaleString("vi-VN", { dateStyle: "medium", timeStyle: "short" }) : "",
    location: e.city ?? "",
    price: e.isFree ? 0 : e.minTicketPrice ?? 0,
    category: e.categoryName ?? "",
  }));

  const baseList = isError ? [] : apiMapped;
  const filtered = activeCategory ? baseList.filter((e: any) => e.category === activeCategory) : baseList;
  
  return (
    <section id="events" className="py-8 sm:py-12">
      <SectionHeading
        title="Sự kiện nổi bật"
        subtitle="Được cộng đồng quan tâm nhiều nhất tuần này"
        action={<a href="#" className="text-sm font-medium text-slate-900 hover:underline">Xem tất cả</a>}
      />
      {isLoading ? (
        <div className="container-page flex justify-center py-8">
          <div className="flex flex-col items-center space-y-4">
            <LoadingSpinner size="lg" />
            <p className="text-gray-600">Đang tải sự kiện nổi bật...</p>
          </div>
        </div>
      ) : (
        <div className="container-page grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.length > 0 ? (
            filtered.map((e: any) => <EventCard key={e.id} event={e} />)
          ) : (
            <div className="col-span-full text-center py-8">
              <p className="text-gray-600">
                {isError ? "Không thể tải sự kiện. Vui lòng thử lại sau." : "Chưa có sự kiện nổi bật."}
              </p>
            </div>
          )}
        </div>
      )}
    </section>
  );
}


