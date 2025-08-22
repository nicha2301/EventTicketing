"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import {
  generateSalesReport as generateSalesReportApi,
  generateRevenueReport as generateRevenueReportApi,
  generateAttendanceReport as generateAttendanceReportApi,
  exportReportToPdf as exportReportToPdfApi,
  exportReportToExcel as exportReportToExcelApi,
  getReportsByCurrentUser,
  getReportById,
  getReportsByEvent,
  getReportsByType,
} from "@/lib/api/modules/events";
import type { ReportRequest } from "@/lib/api";

type BaseReportInput = {
  eventId: string;
  name: string;
  description?: string;
  startDate?: string;
  endDate?: string; 
};

function buildReportRequest(input: BaseReportInput, type: "SALES" | "REVENUE" | "ATTENDANCE"): ReportRequest {
  const { eventId, name, description, startDate, endDate } = input;
  const parameters = (startDate || endDate)
    ? { startDate, endDate } as Record<string, unknown>
    : undefined;

  return {
    name,
    type,
    description,
    eventId,
    parameters,
  };
}

export function useGenerateSalesReport() {
  return useMutation({
    mutationKey: ["reports", "generate", "sales"],
    mutationFn: async (input: BaseReportInput) => {
      const body = buildReportRequest(input, "SALES");
      return generateSalesReportApi(body);
    },
  });
}

export function useGenerateRevenueReport() {
  return useMutation({
    mutationKey: ["reports", "generate", "revenue"],
    mutationFn: async (input: BaseReportInput) => {
      const body = buildReportRequest(input, "REVENUE");
      return generateRevenueReportApi(body);
    },
  });
}

export function useGenerateAttendanceReport() {
  return useMutation({
    mutationKey: ["reports", "generate", "attendance"],
    mutationFn: async (input: BaseReportInput) => {
      const body = buildReportRequest(input, "ATTENDANCE");
      return generateAttendanceReportApi(body);
    },
  });
}

export function useExportPdf() {
  return useMutation({
    mutationKey: ["reports", "export", "pdf"],
    mutationFn: async (reportId: number) => exportReportToPdfApi(reportId),
  });
}

export function useExportExcel() {
  return useMutation({
    mutationKey: ["reports", "export", "excel"],
    mutationFn: async (reportId: number) => exportReportToExcelApi(reportId),
  });
}

export function useMyReports(page = 0, size = 10) {
  return useQuery({
    queryKey: ["reports", "mine", page, size],
    queryFn: async ({ signal }) => {
      const res = await getReportsByCurrentUser(page, size, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useEventReports(eventId: string, page = 0, size = 10) {
  return useQuery({
    queryKey: ["reports", "event", eventId, page, size],
    enabled: !!eventId,
    queryFn: async ({ signal }) => {
      const res = await getReportsByEvent(eventId, page, size, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useReportById(reportId?: number) {
  return useQuery({
    queryKey: ["reports", "detail", reportId],
    enabled: typeof reportId === "number",
    queryFn: async ({ signal }) => {
      const res = await getReportById(reportId as number, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useReportsByType(type: "REVENUE" | "SALES" | "ATTENDANCE", page = 0, size = 10) {
  return useQuery({
    queryKey: ["reports", "type", type, page, size],
    enabled: !!type,
    queryFn: async ({ signal }) => {
      const res = await getReportsByType(type, page, size, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}


