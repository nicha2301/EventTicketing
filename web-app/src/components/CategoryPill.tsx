import { cn } from "@/lib/utils";

type Props = {
  label: string;
  selected?: boolean;
  onClick?: () => void;
};

export default function CategoryPill({ label, selected, onClick }: Props) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "rounded-full border px-4 py-1.5 text-sm",
        selected
          ? "border-slate-900 bg-slate-900 text-white"
          : "border-slate-300 bg-white text-slate-700 hover:border-slate-400"
      )}
    >
      {label}
    </button>
  );
}





