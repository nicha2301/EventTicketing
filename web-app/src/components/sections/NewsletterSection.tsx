"use client";

import { useState } from "react";
import SectionHeading from "@/components/SectionHeading";

export default function NewsletterSection() {
  const [email, setEmail] = useState("");
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // TODO: Implement newsletter subscription logic
  };
  return (
    <section className="py-8 sm:py-12">
      <SectionHeading title="Đăng ký nhận bản tin" subtitle="Nhận thông tin sự kiện hot mỗi tuần" />
      <div className="container-page">
        <form onSubmit={handleSubmit} className="mx-auto flex max-w-xl items-center gap-2">
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="flex-1 rounded-md border border-slate-300 bg-white px-4 py-3 text-sm outline-none placeholder:text-slate-400 focus:border-slate-400"
            placeholder="you@example.com"
          />
          <button type="submit" className="rounded-md bg-slate-900 px-5 py-3 text-sm font-medium text-white hover:bg-black">
            Đăng ký
          </button>
        </form>
      </div>
    </section>
  );
}




