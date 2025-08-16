"use client";

import { useState } from "react";
import SectionHeading from "@/components/SectionHeading";

const faqs = [
  {
    q: "Làm thế nào để mua vé?",
    a: "Tìm sự kiện bạn quan tâm, chọn loại vé và thanh toán theo hướng dẫn. Vé sẽ được gửi qua email.",
  },
  {
    q: "Tôi có thể hoàn hủy vé không?",
    a: "Tùy chính sách từng sự kiện. Vui lòng xem chi tiết tại trang sự kiện hoặc liên hệ hỗ trợ.",
  },
  {
    q: "Tôi muốn tổ chức sự kiện trên nền tảng?",
    a: "Hãy đăng ký tài khoản Organizer để tạo, quản lý sự kiện và doanh thu dễ dàng.",
  },
];

export default function FAQSection() {
  const [openIdx, setOpenIdx] = useState<number | null>(0);
  return (
    <section className="py-8 sm:py-12">
      <SectionHeading title="Câu hỏi thường gặp" />
      <div className="container-page mx-auto max-w-3xl divide-y divide-slate-200 rounded-2xl border border-slate-200 bg-white">
        {faqs.map((f, idx) => {
          const open = openIdx === idx;
          return (
            <div key={idx}>
              <button
                className="flex w-full items-center justify-between px-5 py-4 text-left"
                onClick={() => setOpenIdx(open ? null : idx)}
              >
                <span className="font-medium">{f.q}</span>
                <span className="text-slate-500">{open ? "−" : "+"}</span>
              </button>
              {open ? <div className="px-5 pb-5 text-slate-600">{f.a}</div> : null}
            </div>
          );
        })}
      </div>
    </section>
  );
}



