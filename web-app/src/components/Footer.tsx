export default function Footer() {
  return (
    <footer className="border-t border-slate-200 bg-white">
      <div className="container-page flex flex-col items-center justify-between gap-4 py-8 sm:flex-row">
        <p className="text-center text-sm text-slate-500 sm:text-left">
          © {new Date().getFullYear()} EventTicketing. All rights reserved.
        </p>
        <nav className="flex items-center gap-4 text-sm text-slate-600">
          <a href="#" className="hover:text-slate-900">Điều khoản</a>
          <a href="#" className="hover:text-slate-900">Bảo mật</a>
          <a href="#" className="hover:text-slate-900">Liên hệ</a>
        </nav>
      </div>
    </footer>
  );
}



