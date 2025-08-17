
import { NextResponse, type NextRequest } from "next/server";

const PROTECTED_PREFIXES = ["/organizer", "/admin"];

export function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;
  const isProtectedPrefix = PROTECTED_PREFIXES.some((p) => pathname.startsWith(p));
  const roleCookie = req.cookies.get("role")?.value;

  if (!isProtectedPrefix) return NextResponse.next();

  if (isProtectedPrefix && !roleCookie) {
    const url = req.nextUrl.clone();
    url.pathname = "/login";
    return NextResponse.redirect(url);
  }

  if (pathname.startsWith("/admin") && roleCookie !== "ADMIN") {
    const url = req.nextUrl.clone();
    url.pathname = "/";
    return NextResponse.redirect(url);
  }

  if (pathname.startsWith("/organizer") && roleCookie && !["ORGANIZER", "ADMIN"].includes(roleCookie)) {
    const url = req.nextUrl.clone();
    url.pathname = "/";
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/organizer/:path*",
    "/admin/:path*"
  ],
};


