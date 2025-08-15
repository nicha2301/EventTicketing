"use client";

import { ReactNode, useEffect, useState } from "react";
import { X } from "lucide-react";
import { cn } from "@/lib/utils/cn";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  children: ReactNode;
  title?: string;
  className?: string;
}

export default function Modal({ isOpen, onClose, children, title, className }: ModalProps) {
  const [isVisible, setIsVisible] = useState(false);
  const [isAnimating, setIsAnimating] = useState(false);

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      setIsVisible(true);
      setTimeout(() => setIsAnimating(true), 10);
    } else {
      setIsAnimating(false);
      setTimeout(() => {
        setIsVisible(false);
        document.body.style.overflow = 'unset';
      }, 150);
    }

    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
    };
  }, [isOpen, onClose]);

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  if (!isVisible) return null;

  return (
    <div 
      className={cn(
        "fixed inset-0 z-50 flex items-center justify-center p-4 transition-opacity duration-200",
        "min-h-screen overflow-y-auto",
        isAnimating ? "opacity-100" : "opacity-0"
      )}
      onClick={handleBackdropClick}
    >
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" />
      
      {/* Modal */}
      <div 
        className={cn(
          "relative bg-white rounded-2xl shadow-2xl border border-gray-100 max-h-[85vh] overflow-y-auto",
          "w-full max-w-md mx-auto transform transition-all duration-200 ease-out",
          // Animation states
          isAnimating 
            ? "scale-100 translate-y-0 opacity-100" 
            : "scale-95 translate-y-2 opacity-0",
          className
        )}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Close button always visible */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 z-10 p-2 hover:bg-gray-100 rounded-full transition-colors"
        >
          <X className="w-5 h-5 text-gray-500" />
        </button>
        
        {/* Header */}
        {title && (
          <div className="px-6 pt-6 pb-4">
            <h2 className="text-xl font-semibold text-gray-900 pr-8">{title}</h2>
          </div>
        )}
        
        {/* Content */}
        <div className={cn("px-6 pb-6", title ? "pt-0" : "pt-6")}>
          {children}
        </div>
      </div>
    </div>
  );
}
