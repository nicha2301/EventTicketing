import Image from "next/image";
import SectionHeading from "@/components/SectionHeading";

const partners = [
  { name: "Next.js", logo: "/next.svg" },
  { name: "Vercel", logo: "/vercel.svg" },
  { name: "Globe", logo: "/globe.svg" },
  { name: "Window", logo: "/window.svg" },
];

export default function PartnersSection() {
  return (
    <section className="py-8 sm:py-12">
      <SectionHeading title="Đối tác & công nghệ" subtitle="Hạ tầng hiện đại, tin cậy" />
      <div className="container-page grid grid-cols-2 items-center gap-6 sm:grid-cols-4">
        {partners.map((p) => (
          <div key={p.name} className="flex items-center justify-center rounded-xl border border-slate-200 bg-white p-6">
            <Image src={p.logo} alt={p.name} width={120} height={32} />
          </div>
        ))}
      </div>
    </section>
  );
}



