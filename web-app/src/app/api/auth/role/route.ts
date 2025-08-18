import { NextResponse, type NextRequest } from "next/server";

export async function POST(req: NextRequest) {
  try {
    const { role } = await req.json();
    if (!role) {
      return NextResponse.json({ error: "Missing role" }, { status: 400 });
    }
    const res = NextResponse.json({ ok: true });
    res.cookies.set("role", String(role), {
      path: "/",
      httpOnly: false,
      sameSite: "lax",
      secure: process.env.NODE_ENV === "production",
      maxAge: 60 * 60 * 24 * 7,
    });
    return res;
  } catch {
    return NextResponse.json({ error: "Bad Request" }, { status: 400 });
  }
}

export async function DELETE() {
  const res = NextResponse.json({ ok: true });
  res.cookies.set("role", "", { path: "/", maxAge: 0 });
  return res;
}


