import { cn } from "@/lib/utils/cn";

interface LoadingProps {
  className?: string;
  size?: "sm" | "md" | "lg";
}

export function Loading({ className, size = "md" }: LoadingProps) {
  const sizeClasses = {
    sm: "h-4 w-4",
    md: "h-8 w-8", 
    lg: "h-12 w-12",
  };

  return (
    <div className={cn("flex items-center justify-center", className)}>
      <div
        className={cn(
          "animate-spin rounded-full border-2 border-slate-300 border-t-slate-900",
          sizeClasses[size]
        )}
      />
    </div>
  );
}

export function EventCardSkeleton() {
  return (
    <div className="h-64 animate-pulse rounded-xl border border-slate-200 bg-slate-100">
      <div className="h-32 rounded-t-xl bg-slate-200" />
      <div className="p-4">
        <div className="h-4 rounded bg-slate-200" />
        <div className="mt-2 h-3 w-2/3 rounded bg-slate-200" />
        <div className="mt-3 flex justify-between">
          <div className="h-3 w-16 rounded bg-slate-200" />
          <div className="h-3 w-12 rounded bg-slate-200" />
        </div>
      </div>
    </div>
  );
}
