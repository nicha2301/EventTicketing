import Link from "next/link";
import { Calendar, MapPin, Tag } from "lucide-react";
import { formatPriceVND } from "@/lib/utils";
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
  const isFree = event.price === 0;
  
  return (
    <Link
      href={`/events/${event.id}`}
      className="group block overflow-hidden rounded-2xl bg-white shadow-sm border border-gray-100 transition-all duration-300 hover:shadow-lg hover:border-gray-200 hover:-translate-y-1"
    >
      {/* Image Container */}
      <div className="relative aspect-[4/3] w-full overflow-hidden bg-gradient-to-br from-gray-50 to-gray-100">
        <EventImage
          src={event.image}
          alt={event.title}
          fill
          sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
          className="object-cover transition duration-500 group-hover:scale-110"
        />
        
        {/* Category Badge */}
        <div className="absolute top-3 left-3">
          <span className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white/90 backdrop-blur-sm rounded-full text-xs font-medium text-gray-700 shadow-sm">
            <Tag className="w-3 h-3" />
            {event.category}
          </span>
        </div>
        
        {/* Price Badge */}
        <div className="absolute top-3 right-3">
          <span className={`inline-flex items-center px-3 py-1.5 rounded-full text-xs font-bold shadow-sm ${
            isFree 
              ? 'bg-green-500 text-white' 
              : 'bg-white/90 backdrop-blur-sm text-gray-900'
          }`}>
            {isFree ? 'Miễn phí' : formatPriceVND(event.price)}
          </span>
        </div>
      </div>
      
      {/* Content */}
      <div className="p-5 space-y-3">
        {/* Title */}
        <h3 className="font-bold text-gray-900 text-lg leading-tight line-clamp-2 group-hover:text-blue-600 transition-colors">
          {event.title}
        </h3>
        
        {/* Date and Time */}
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <div className="flex items-center gap-1.5">
            <Calendar className="w-4 h-4 text-blue-500" />
            <span>{event.date}</span>
          </div>
        </div>
        
        {/* Location */}
        <div className="flex items-center gap-1.5 text-sm text-gray-600">
          <MapPin className="w-4 h-4 text-red-500" />
          <span className="line-clamp-1">{event.location}</span>
        </div>
      </div>
    </Link>
  );
}



