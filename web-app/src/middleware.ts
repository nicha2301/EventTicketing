
import { NextResponse, type NextRequest } from "next/server";

export function middleware(req: NextRequest) {
  const { pathname, search } = req.nextUrl;
  const roleCookie = req.cookies.get("role")?.value;

  if (pathname.startsWith("/admin")) {
    if (roleCookie !== "ADMIN") {
      const url = req.nextUrl.clone();
      url.pathname = "/";
      return NextResponse.redirect(url);
    }
    return NextResponse.next();
  }

  if (pathname.startsWith("/organizer")) {
    if (!roleCookie) {
      const url = req.nextUrl.clone();
      url.pathname = "/auth/gate";
      url.searchParams.set("returnTo", pathname + search);
      return NextResponse.rewrite(url);
    }
    if (roleCookie !== "ORGANIZER" && roleCookie !== "ADMIN") {
      const url = req.nextUrl.clone();
      url.pathname = "/";
      return NextResponse.redirect(url);
    }
    return NextResponse.next();
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/organizer/:path*",
    "/admin/:path*",
  ],
};


