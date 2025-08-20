import Link from "next/link";

export default function CTASection() {
  return (
    <section id="about" className="py-12 sm:py-16">
      <div className="container-page overflow-hidden rounded-2xl border border-slate-200 bg-gradient-to-br from-slate-50 to-white p-8 sm:p-12">
        <div className="mx-auto max-w-2xl text-center">
          <h3 className="text-2xl font-semibold tracking-tight sm:text-3xl">Tổ chức sự kiện của riêng bạn</h3>
          <p className="mt-2 text-slate-600">
            Đăng ký trở thành Organizer để quản lý vé, doanh thu và check-in nhanh chóng.
          </p>
          <div className="mt-6 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Link href="/auth/register" className="w-full rounded-md bg-slate-900 px-5 py-3 text-center text-sm font-medium text-white hover:bg-black sm:w-auto">
              Bắt đầu ngay
            </Link>
            <Link href="#" className="w-full rounded-md border border-slate-300 bg-white px-5 py-3 text-center text-sm font-medium text-slate-900 hover:bg-slate-50 sm:w-auto">
              Tìm hiểu tính năng
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}





