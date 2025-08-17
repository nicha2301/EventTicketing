import SectionHeading from "@/components/SectionHeading";
import EventCard from "@/components/EventCard";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { useUpcomingEvents } from "@/hooks/useUpcomingEvents";
import { sanitizeEventImageUrl } from "@/lib/utils/image";
import Link from "next/link";

export default function UpcomingEventsSection() {
  const { data, isLoading, isError } = useUpcomingEvents(6);
  const mapped = (data ?? []).map((e: any) => ({
    id: e.id ?? "",
    title: e.title ?? "",
    image: sanitizeEventImageUrl(e.featuredImageUrl, e.imageUrls),
    date: e.startDate ? new Date(e.startDate).toLocaleString("vi-VN", { dateStyle: "medium", timeStyle: "short" }) : "",
    location: e.city ?? "",
    price: e.isFree ? 0 : e.minTicketPrice ?? 0,
    category: e.categoryName ?? "",
  }));

  return (
    <section className="py-12 sm:py-16 bg-white">
      <div className="container-page">
        <SectionHeading
          title="Sự kiện sắp diễn ra"
          subtitle="Khám phá những sự kiện thú vị trong thời gian tới"
          action={
            <Link 
              href="/search" 
              className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-full text-sm font-medium hover:bg-blue-700 transition-colors"
            >
              Xem tất cả
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </Link>
          }
        />
        
        {isLoading ? (
          <div className="flex justify-center py-12">
            <div className="flex flex-col items-center space-y-4">
              <LoadingSpinner size="lg" />
              <p className="text-gray-600 font-medium">Đang tải sự kiện sắp diễn ra...</p>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 mt-8">
            {mapped.length > 0 ? (
              mapped.map((e: any) => <EventCard key={e.id} event={e} />)
            ) : (
              <div className="col-span-full text-center py-12">
                <div className="max-w-md mx-auto">
                  <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">
                    {isError ? "Không thể tải sự kiện" : "Chưa có sự kiện sắp diễn ra"}
                  </h3>
                  <p className="text-gray-600">
                    {isError ? "Vui lòng thử lại sau." : "Hãy quay lại sau để xem các sự kiện mới."}
                  </p>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </section>
  );
}


