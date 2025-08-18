import type { Metadata } from "next";
import "./globals.css";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import { QueryProvider } from "@/lib/api/query-client";
import ErrorBoundary from "@/components/ErrorBoundary";
import { AuthModalProvider } from "@/hooks/useAuthModal";
import { Inter } from "next/font/google";
import { Toaster } from "sonner";
import { Suspense } from "react";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  metadataBase: new URL(process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000"),
  title: "EventTicketing",
  description:
    "Nền tảng đặt vé sự kiện - khám phá, mua vé và trải nghiệm sự kiện dễ dàng",
  openGraph: {
    title: "EventTicketing",
    description:
      "Nền tảng đặt vé sự kiện - khám phá, mua vé và trải nghiệm sự kiện dễ dàng",
    url: "https://localhost:3000/",
    siteName: "EventTicketing",
    images: [
      { url: "/vercel.svg", width: 1200, height: 630, alt: "EventTicketing" },
    ],
    locale: "vi_VN",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "EventTicketing",
    description:
      "Nền tảng đặt vé sự kiện - khám phá, mua vé và trải nghiệm sự kiện dễ dàng",
    images: ["/vercel.svg"],
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="vi" suppressHydrationWarning>
      <body className={`${inter.className} min-h-screen bg-white text-slate-900 antialiased`}>
        <ErrorBoundary>
          <QueryProvider>
            <AuthModalProvider>
              <div className="flex min-h-screen flex-col">
                <Suspense fallback={null}>
                  <Navbar />
                </Suspense>
                <main className="flex-1">{children}</main>
                <Footer />
              </div>
              <Toaster richColors position="top-right" />
            </AuthModalProvider>
          </QueryProvider>
        </ErrorBoundary>
      </body>
    </html>
  );
}


