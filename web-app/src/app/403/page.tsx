export default function ForbiddenPage() {
  return (
    <section className="py-20">
      <div className="container-page text-center space-y-3">
        <h1 className="text-3xl font-semibold">403 - Không có quyền truy cập</h1>
        <p className="text-slate-600">Bạn không có quyền truy cập trang này.</p>
        <a href="/" className="inline-block rounded-md border px-4 py-2 text-gray-700 mt-2">Về trang chủ</a>
      </div>
    </section>
  );
}



