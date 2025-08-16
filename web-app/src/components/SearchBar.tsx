"use client";

import { Search } from "lucide-react";
import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

type Props = {
  placeholder?: string;
  onSearch?: (keyword: string) => void;
};

export default function SearchBar({ placeholder = "Tìm kiếm sự kiện, địa điểm...", onSearch }: Props) {
  const [value, setValue] = useState("");
  const router = useRouter();
  const commit = (kw: string) => {
    onSearch?.(kw);
    if (kw.trim().length > 0) router.push(`/search?q=${encodeURIComponent(kw)}`);
  };
  
  return (
    <div className="relative w-full max-w-xl">
      <div className="relative">
        <input
          value={value}
          onChange={(e) => setValue(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") commit(value);
          }}
          className="w-full rounded-full border border-slate-300 bg-white px-4 py-3 pr-12 text-sm shadow-sm outline-none placeholder:text-slate-400 focus:border-slate-400"
          placeholder={placeholder}
        />
        <button
          type="button"
          onClick={() => commit(value)}
          className="absolute right-1 top-1 inline-flex h-10 w-10 items-center justify-center rounded-full bg-slate-900 text-white hover:bg-black"
          aria-label="Search"
        >
          <Search className="h-5 w-5" />
        </button>
      </div>
      
      {/* Quick Actions */}
      <div className="mt-2 flex flex-wrap gap-2 text-xs">
        <Link
          href="/search?category=Âm nhạc"
          className="px-2 py-1 bg-blue-100 text-blue-700 rounded-full hover:bg-blue-200 transition-colors"
        >
          Âm nhạc
        </Link>
        <Link
          href="/search?category=Hội thảo"
          className="px-2 py-1 bg-green-100 text-green-700 rounded-full hover:bg-green-200 transition-colors"
        >
          Hội thảo
        </Link>
        <Link
          href="/search?location=TP.HCM"
          className="px-2 py-1 bg-purple-100 text-purple-700 rounded-full hover:bg-purple-200 transition-colors"
        >
          TP.HCM
        </Link>
        <Link
          href="/search?isFree=true"
          className="px-2 py-1 bg-orange-100 text-orange-700 rounded-full hover:bg-orange-200 transition-colors"
        >
          Miễn phí
        </Link>
      </div>
    </div>
  );
}


