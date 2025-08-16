import SectionHeading from "@/components/SectionHeading";
import CategoryPill from "@/components/CategoryPill";
import { useSearchMetadata } from "@/hooks/useSearchMetadata";
import LoadingSpinner from "@/components/ui/LoadingSpinner";

type Props = {
  active: string | null;
  onChange: (value: string | null) => void;
};

export default function CategoriesSection({ active, onChange }: Props) {
  const { categories, isLoading, error } = useSearchMetadata();

  if (isLoading) {
    return (
      <section id="categories" className="py-8 sm:py-12">
        <SectionHeading title="Danh mục nổi bật" subtitle="Chọn danh mục bạn quan tâm" />
        <div className="container-page flex justify-center py-8">
          <div className="flex flex-col items-center space-y-4">
            <LoadingSpinner size="md" />
            <p className="text-gray-600">Đang tải danh mục...</p>
          </div>
        </div>
      </section>
    );
  }

  if (error || !categories || categories.length === 0) {
    return (
      <section id="categories" className="py-8 sm:py-12">
        <SectionHeading title="Danh mục nổi bật" subtitle="Chọn danh mục bạn quan tâm" />
        <div className="container-page flex justify-center py-8">
          <p className="text-gray-600">
            {error ? "Không thể tải danh mục. Vui lòng thử lại sau." : "Chưa có danh mục nào."}
          </p>
        </div>
      </section>
    );
  }

  return (
    <section id="categories" className="py-8 sm:py-12">
      <SectionHeading title="Danh mục nổi bật" subtitle="Chọn danh mục bạn quan tâm" />
      <div className="container-page flex flex-wrap gap-3">
        <CategoryPill label="Tất cả" selected={active === null} onClick={() => onChange(null)} />
        {categories.map((c: any) => (
          <CategoryPill 
            key={c.id || c.name} 
            label={c.name} 
            selected={active === c.name} 
            onClick={() => onChange(active === c.name ? null : c.name)} 
          />
        ))}
      </div>
    </section>
  );
}


