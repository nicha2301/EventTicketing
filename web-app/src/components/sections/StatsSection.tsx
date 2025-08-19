import SectionHeading from "@/components/SectionHeading";

const stats = [
  { label: "Sự kiện", value: "+1,200" },
  { label: "Người dùng", value: "+150K" },
  { label: "Vé đã bán", value: "+2.5M" },
  { label: "Đối tác", value: "+300" },
];

export default function StatsSection() {
  return (
    <section className="py-8 sm:py-12">
      <SectionHeading title="Những con số ấn tượng" />
      <div className="container-page grid grid-cols-2 gap-5 sm:grid-cols-4">
        {stats.map((s) => (
          <div key={s.label} className="rounded-2xl border border-slate-200 bg-white p-6 text-center">
            <div className="text-2xl font-bold tracking-tight sm:text-3xl">{s.value}</div>
            <div className="mt-1 text-sm text-slate-600">{s.label}</div>
          </div>
        ))}
      </div>
    </section>
  );
}




