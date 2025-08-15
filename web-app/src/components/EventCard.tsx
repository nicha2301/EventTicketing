import Link from "next/link";
import { Calendar, MapPin } from "lucide-react";
import { formatPriceVND, truncate } from "@/lib/utils";
import { EventImage } from "@/components/ui/event-image";

export type EventItem = {
  id: string;
  title: string;
  image: string;
  date: string;
  location: string;
  price: number;
  category: string;
};

export default function EventCard({ event }: { event: EventItem }) {
  return (
    <Link
      href={`/events/${event.id}`}
      className="group overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition hover:shadow-md"
    >
      <div className="relative aspect-[16/10] w-full overflow-hidden bg-slate-100">
        <EventImage
          src={event.image}
          alt={event.title}
          fill
          sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
          className="object-cover transition duration-300 group-hover:scale-105"
        />
      </div>
      <div className="space-y-2 p-4">
        <h3 className="line-clamp-1 text-base font-semibold text-slate-900">
          {truncate(event.title, 70)}
        </h3>
        <div className="flex items-center gap-3 text-sm text-slate-600">
          <span className="inline-flex items-center gap-1"><Calendar className="h-4 w-4" />{event.date}</span>
          <span className="inline-flex items-center gap-1"><MapPin className="h-4 w-4" />{event.location}</span>
        </div>
        <div className="flex items-center justify-between pt-1">
          <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-700">{event.category}</span>
          <span className="text-sm font-semibold text-slate-900">{formatPriceVND(event.price)}</span>
        </div>
      </div>
    </Link>
  );
}



