import { NextResponse, type NextRequest } from "next/server";

const PROTECTED_PREFIXES = ["/organizer", "/admin"];
const AUTH_REQUIRED_PAGES = ["/my-tickets", "/payment-history", "/profile"];

export function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;
  const isProtectedPrefix = PROTECTED_PREFIXES.some((p) => pathname.startsWith(p));
  const requiresAuth = AUTH_REQUIRED_PAGES.includes(pathname);
  
  if (!isProtectedPrefix && !requiresAuth) return NextResponse.next();

  const roleCookie = req.cookies.get("role")?.value;
  const accessTokenCookie = req.cookies.get("accessToken")?.value;
  
  if (requiresAuth && !accessTokenCookie) {
    const url = req.nextUrl.clone();
    url.pathname = "/login";
    url.searchParams.set("redirect", pathname);
    return NextResponse.redirect(url);
  }
  
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
  matcher: ["/organizer/:path*", "/admin/:path*"],
};


