import Image from "next/image";
import Link from "next/link";
import SearchBar from "@/components/SearchBar";

export default function HeroSection() {
  return (
    <section className="relative overflow-hidden">
      <div className="pointer-events-none absolute inset-0 -z-10 select-none opacity-20">
        <Image src="/globe.svg" alt="" fill className="object-cover" />
      </div>
      <div className="container-page py-16 sm:py-20">
        <div className="mx-auto max-w-3xl text-center">
          <div className="inline-flex rounded-full border border-slate-200 bg-white/80 px-3 py-1 text-xs font-medium text-slate-700 backdrop-blur">
            Săn vé sự kiện hot mỗi tuần
          </div>
          <h1 className="mt-4 text-3xl font-bold tracking-tight sm:text-5xl">
            Khám phá, đặt vé và trải nghiệm sự kiện dễ dàng
          </h1>
          <p className="mt-3 text-base text-slate-600 sm:text-lg">
            Từ concert, hội thảo đến workshop — tất cả trong một nền tảng nhanh, gọn, an toàn.
          </p>
          <div className="mt-6 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <SearchBar />
            <Link href="#events" className="w-full rounded-md bg-slate-900 px-5 py-3 text-center text-sm font-medium text-white hover:bg-black sm:w-auto">
              Khám phá sự kiện
            </Link>
            <Link href="#about" className="w-full rounded-md border border-slate-300 bg-white px-5 py-3 text-center text-sm font-medium text-slate-900 hover:bg-slate-50 sm:w-auto">
              Tìm hiểu thêm
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}


