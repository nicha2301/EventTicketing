"use client";

import { cn } from "@/lib/utils/cn";

interface SkeletonProps {
  className?: string;
}

export function Skeleton({ className }: SkeletonProps) {
  return (
    <div
      className={cn(
        "animate-pulse rounded-md bg-gray-200",
        className
      )}
    />
  );
}

// Common skeleton patterns
export function ButtonSkeleton({ className }: SkeletonProps) {
  return <Skeleton className={cn("h-10 w-20", className)} />;
}

export function TextSkeleton({ className }: SkeletonProps) {
  return <Skeleton className={cn("h-4 w-32", className)} />;
}

export function AvatarSkeleton({ className }: SkeletonProps) {
  return <Skeleton className={cn("h-8 w-8 rounded-full", className)} />;
}

export function CardSkeleton({ className }: SkeletonProps) {
  return (
    <div className={cn("space-y-3", className)}>
      <Skeleton className="h-48 w-full rounded-xl" />
      <div className="space-y-2">
        <Skeleton className="h-4 w-3/4" />
        <Skeleton className="h-4 w-1/2" />
      </div>
    </div>
  );
}
