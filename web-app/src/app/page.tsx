"use client";

import { useState } from "react";
import HeroSection from "@/components/sections/HeroSection";
import CategoriesSection from "@/components/sections/CategoriesSection";
import FeaturedEventsSection from "@/components/sections/FeaturedEventsSection";
import CTASection from "@/components/sections/CTASection";
import PartnersSection from "@/components/sections/PartnersSection";
import StatsSection from "@/components/sections/StatsSection";
import NewsletterSection from "@/components/sections/NewsletterSection";
import FAQSection from "@/components/sections/FAQSection";
import UpcomingEventsSection from "@/components/sections/UpcomingEventsSection";

export default function HomePage() {
  const [activeCategory, setActiveCategory] = useState<string | null>(null);
  return (
    <>
      <HeroSection />
      <CategoriesSection active={activeCategory} onChange={setActiveCategory} />
      <FeaturedEventsSection activeCategory={activeCategory} />
      <UpcomingEventsSection />
      <PartnersSection />
      <StatsSection />
      <NewsletterSection />
      <FAQSection />
      <CTASection />
    </>
  );
}


