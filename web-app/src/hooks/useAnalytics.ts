"use client";

import { useQuery } from "@tanstack/react-query";
import {
  getKPIDashboard,
  getEventPerformance,
  getDailyRevenue,
  getTicketSalesByType,
  getPaymentMethodsAnalysis,
  getAttendeeAnalytics,
  getRegistrationTimeline,
} from "@/lib/api/modules/events";

export function useKpiDashboard(eventId: string) {
  return useQuery({
    queryKey: ["analytics", "kpi", eventId],
    enabled: !!eventId,
    queryFn: async ({ signal }) => {
      const response = await getKPIDashboard(eventId, signal);
      return response.data || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useEventPerformance(eventId: string) {
  return useQuery({
    queryKey: ["analytics", "performance", eventId],
    enabled: !!eventId,
    queryFn: async ({ signal }) => {
      const response = await getEventPerformance(eventId, signal);
      return response.data || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useDailyRevenue(eventId: string) {
  return useQuery({
    queryKey: ["analytics", "daily-revenue", eventId],
    enabled: !!eventId,
    queryFn: async ({ signal }) => {
      const response = await getDailyRevenue({ eventId }, signal);
      return response.data || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useSalesByType(eventId: string) {
  return useQuery({
    queryKey: ["analytics", "sales-by-type", eventId],
    enabled: !!eventId,
    queryFn: async ({ signal }) => {
      const response = await getTicketSalesByType(eventId, signal);
      return response.data || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function usePaymentMethodsAnalysis(eventId: string) {
  return useQuery({
    queryKey: ["analytics", "payment-methods", eventId],
    enabled: !!eventId,
    queryFn: async ({ signal }) => {
      const response = await getPaymentMethodsAnalysis(eventId, signal);
      return response.data || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useAttendeeAnalytics(eventId: string) {
  return useQuery({
    queryKey: ["analytics", "attendees", eventId],
    enabled: !!eventId,
    queryFn: async ({ signal }) => {
      const response = await getAttendeeAnalytics(eventId, signal);
      return response.data || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useRegistrationTimeline(eventId: string) {
  return useQuery({
    queryKey: ["analytics", "registration-timeline", eventId],
    enabled: !!eventId,
    queryFn: async ({ signal }) => {
      const response = await getRegistrationTimeline(eventId, signal);
      return response.data || {};
    },
    placeholderData: (prev) => prev,
  });
}
