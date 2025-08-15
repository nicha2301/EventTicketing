"use client";

import { createContext, useContext, useState, ReactNode } from "react";

interface AuthModalContextType {
  isLoginOpen: boolean;
  isRegisterOpen: boolean;
  isEmailVerificationOpen: boolean;
  registeredEmail: string;
  openLogin: () => void;
  openRegister: () => void;
  openEmailVerification: (email: string) => void;
  closeLogin: () => void;
  closeRegister: () => void;
  closeEmailVerification: () => void;
  switchToRegister: () => void;
  switchToLogin: () => void;
}

const AuthModalContext = createContext<AuthModalContextType | undefined>(undefined);

export function AuthModalProvider({ children }: { children: ReactNode }) {
  const [isLoginOpen, setIsLoginOpen] = useState(false);
  const [isRegisterOpen, setIsRegisterOpen] = useState(false);
  const [isEmailVerificationOpen, setIsEmailVerificationOpen] = useState(false);
  const [registeredEmail, setRegisteredEmail] = useState("");

  const openLogin = () => {
    setIsLoginOpen(true);
    setIsRegisterOpen(false);
    setIsEmailVerificationOpen(false);
  };

  const openRegister = () => {
    setIsRegisterOpen(true);
    setIsLoginOpen(false);
    setIsEmailVerificationOpen(false);
  };

  const openEmailVerification = (email: string) => {
    setRegisteredEmail(email);
    setIsEmailVerificationOpen(true);
    setIsLoginOpen(false);
    setIsRegisterOpen(false);
  };

  const closeLogin = () => setIsLoginOpen(false);
  const closeRegister = () => setIsRegisterOpen(false);
  const closeEmailVerification = () => {
    setIsEmailVerificationOpen(false);
    setRegisteredEmail("");
  };

  const switchToRegister = () => {
    setIsLoginOpen(false);
    setIsRegisterOpen(true);
    setIsEmailVerificationOpen(false);
  };

  const switchToLogin = () => {
    setIsRegisterOpen(false);
    setIsLoginOpen(true);
    setIsEmailVerificationOpen(false);
  };

  return (
    <AuthModalContext.Provider
      value={{
        isLoginOpen,
        isRegisterOpen,
        isEmailVerificationOpen,
        registeredEmail,
        openLogin,
        openRegister,
        openEmailVerification,
        closeLogin,
        closeRegister,
        closeEmailVerification,
        switchToRegister,
        switchToLogin,
      }}
    >
      {children}
    </AuthModalContext.Provider>
  );
}

export function useAuthModal() {
  const context = useContext(AuthModalContext);
  if (context === undefined) {
    throw new Error("useAuthModal must be used within an AuthModalProvider");
  }
  return context;
}
