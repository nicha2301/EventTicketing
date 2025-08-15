"use client";

import Link from "next/link";
import Image from "next/image";
import { LogOut, Menu, User, Ticket, ChevronDown, CreditCard } from "lucide-react";
import { useState, useEffect, useRef } from "react";
import { cn } from "@/lib/utils/cn";
import { useAuthStore } from "@/store/auth";
import { useLogout } from "@/hooks/useAuth";
import { useAuthModal } from "@/hooks/useAuthModal";
import { useAuthHydration } from "@/hooks/useAuthHydration";
import LoginModal from "@/components/auth/LoginModal";
import RegisterModal from "@/components/auth/RegisterModal";
import EmailVerificationModal from "@/components/auth/EmailVerificationModal";

// Loading skeleton component for auth button
function AuthButtonSkeleton() {
  return (
    <div className="h-10 w-24 bg-gray-200 rounded-xl animate-pulse"></div>
  );
}

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const { currentUser, isLoading } = useAuthStore();
  const userMenuRef = useRef<HTMLDivElement>(null);
  const isHydrated = useAuthHydration();
  const logoutMutation = useLogout();
  const {
    isLoginOpen,
    isRegisterOpen,
    isEmailVerificationOpen,
    registeredEmail,
    openLogin,
    openRegister,
    closeLogin,
    closeRegister,
    closeEmailVerification,
    switchToRegister,
    switchToLogin,
  } = useAuthModal();
  
  // Close user menu when clicking outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (userMenuRef.current && !userMenuRef.current.contains(event.target as Node)) {
        setUserMenuOpen(false);
      }
    }
    
    if (userMenuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [userMenuOpen]);
  
  const onLogout = () => {
    if (!confirm("Bạn chắc chắn muốn đăng xuất?")) return;
    logoutMutation.mutate();
  };

  return (
    <header className="sticky top-0 z-40 w-full border-b border-slate-200 bg-white/80 backdrop-blur">
      <div className="container-page grid grid-cols-3 h-16 items-center">
        {/* Left: Logo */}
        <div className="flex items-center gap-3">
          <Link href="/" className="flex items-center gap-2">
            <Image src="/next.svg" alt="logo" width={28} height={28} />
            <span className="font-semibold">EventTicketing</span>
          </Link>
        </div>
        
        {/* Center: Navigation - Fixed position */}
        <nav className="hidden items-center justify-center gap-6 text-sm font-medium md:flex">
          <Link href="#events" className="text-slate-600 hover:text-slate-900 transition-colors">Sự kiện</Link>
          <Link href="#categories" className="text-slate-600 hover:text-slate-900 transition-colors">Danh mục</Link>
          <Link href="#about" className="text-slate-600 hover:text-slate-900 transition-colors">Về chúng tôi</Link>
        </nav>
        
        {/* Right: Auth button - Fixed width */}
        <div className="flex justify-end items-center">
          {!isHydrated ? (
            <div className="hidden md:block">
              <div className="h-10 w-24 bg-gray-200 rounded-xl animate-pulse"></div>
            </div>
          ) : (
            <div className="hidden md:block">
              {currentUser ? (
                <div className="relative" ref={userMenuRef}>
                  <button
                    onClick={() => setUserMenuOpen(!userMenuOpen)}
                    className="inline-flex items-center gap-2 rounded-xl border border-gray-200 px-3 py-2 text-sm hover:bg-gray-50 transition-colors"
                  >
                    <User className="h-4 w-4" />
                    <span className="max-w-[100px] truncate">{currentUser.fullName || currentUser.email}</span>
                    <ChevronDown className="h-4 w-4" />
                  </button>
                  
                  {userMenuOpen && (
                    <div className="absolute right-0 top-full mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg py-1 z-50">
                      <Link
                        href="/profile"
                        onClick={() => setUserMenuOpen(false)}
                        className="flex items-center gap-2 px-3 py-2 text-sm hover:bg-gray-50"
                      >
                        <User className="h-4 w-4" />
                        Tài khoản
                      </Link>
                      <Link
                        href="/my-tickets"
                        onClick={() => setUserMenuOpen(false)}
                        className="flex items-center gap-2 px-3 py-2 text-sm hover:bg-gray-50"
                      >
                        <Ticket className="h-4 w-4" />
                        Vé của tôi
                      </Link>
                      <Link
                        href="/payment-history"
                        onClick={() => setUserMenuOpen(false)}
                        className="flex items-center gap-2 px-3 py-2 text-sm hover:bg-gray-50"
                      >
                        <CreditCard className="h-4 w-4" />
                        Lịch sử thanh toán
                      </Link>
                      <hr className="my-1" />
                      <button
                        onClick={() => {
                          setUserMenuOpen(false);
                          onLogout();
                        }}
                        className="w-full flex items-center gap-2 px-3 py-2 text-sm text-red-600 hover:bg-red-50"
                        disabled={isLoading || logoutMutation.isPending}
                      >
                        <LogOut className="h-4 w-4" />
                        Đăng xuất
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                <button 
                  onClick={openLogin}
                  className="rounded-xl bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-black transition-colors"
                >
                  Đăng nhập
                </button>
              )}
            </div>
          )}
          
          {/* Mobile menu button */}
          <button
            className="inline-flex items-center justify-center rounded-md p-2 md:hidden"
            onClick={() => setOpen((v) => !v)}
            aria-label="Toggle Menu"
          >
            <Menu className="h-6 w-6" />
          </button>
        </div>
      </div>
      
      {/* Mobile menu */}
      <div className={cn("md:hidden", open ? "block" : "hidden")}> 
        <div className="container-page space-y-4 pb-4 pt-2">
          <nav className="flex flex-col gap-2">
            <Link href="#events" className="py-1 text-slate-700">Sự kiện</Link>
            <Link href="#categories" className="py-1 text-slate-700">Danh mục</Link>
            <Link href="#about" className="py-1 text-slate-700">Về chúng tôi</Link>
          </nav>
          
          {/* Mobile auth buttons - simplified */}
          {!isHydrated ? (
            <div className="h-10 w-full bg-gray-200 rounded-xl animate-pulse"></div>
          ) : (
            <div>
              {currentUser ? (
                <div className="space-y-2">
                  <Link 
                    href="/profile"
                    className="flex items-center gap-2 rounded-xl border border-gray-200 px-3 py-2 text-sm font-medium"
                  >
                    <User className="h-4 w-4" />
                    Tài khoản
                  </Link>
                  <Link 
                    href="/my-tickets"
                    className="flex items-center gap-2 rounded-xl border border-gray-200 px-3 py-2 text-sm font-medium"
                  >
                    <Ticket className="h-4 w-4" />
                    Vé của tôi
                  </Link>
                  <button 
                    onClick={onLogout} 
                    className="w-full flex items-center gap-2 rounded-xl bg-red-600 px-3 py-2 text-sm text-white font-medium disabled:opacity-50 transition-opacity"
                    disabled={isLoading || logoutMutation.isPending}
                  >
                    <LogOut className="h-4 w-4" />
                    Đăng xuất
                  </button>
                </div>
              ) : (
                <button 
                  onClick={openLogin}
                  className="w-full rounded-xl bg-slate-900 px-3 py-2 text-center text-sm text-white font-medium hover:bg-black transition-colors"
                >
                  Đăng nhập
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Auth Modals */}
      <LoginModal 
        isOpen={isLoginOpen} 
        onClose={closeLogin} 
        onSwitchToRegister={switchToRegister}
      />
      <RegisterModal 
        isOpen={isRegisterOpen} 
        onClose={closeRegister} 
        onSwitchToLogin={switchToLogin}
      />
      <EmailVerificationModal
        isOpen={isEmailVerificationOpen}
        onClose={closeEmailVerification}
        email={registeredEmail}
      />
    </header>
  );
}


