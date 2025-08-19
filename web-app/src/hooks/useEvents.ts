"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { 
  cancelEvent as cancelEventApi,
  getEventById,
  getEventImages,
  publishEvent as publishEventApi,
  saveCloudinaryImage as saveCloudinaryImageApi,
  updateEvent as updateEventApi,
  uploadEventImage as uploadEventImageApi,
  deleteEvent as deleteEventApi,
  deleteEventImage as deleteEventImageApi,
} from "@/lib/api/modules/events";
import type { CloudinaryImageRequest, EventUpdateDto, UploadEventImageBody, UploadEventImageParams } from "@/lib/api";

export function useEventDetail(eventId: string) {
  return useQuery({
    queryKey: ["event-detail", eventId],
    enabled: !!eventId,
    queryFn: async () => await getEventById(eventId),
    placeholderData: (prev) => prev,
  });
}

export function useEventImages(eventId: string) {
  return useQuery({
    queryKey: ["event-images", eventId],
    enabled: !!eventId,
    queryFn: async ({ signal }) => await getEventImages(eventId, signal),
    placeholderData: (prev) => prev,
  });
}

export function usePublishEvent() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (eventId: string) => publishEventApi(eventId),
    onSuccess: (_res, eventId) => {
      if (eventId) qc.invalidateQueries({ queryKey: ["event-detail", eventId] });
      qc.invalidateQueries({ queryKey: ["organizer-events"] });
    },
  });
}

export function useCancelEvent() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (payload: { eventId: string; reason: string }) => cancelEventApi(payload.eventId, { reason: payload.reason } as any),
    onSuccess: (_res, vars) => {
      if (vars?.eventId) qc.invalidateQueries({ queryKey: ["event-detail", vars.eventId] });
      qc.invalidateQueries({ queryKey: ["organizer-events"] });
    },
  });
}

export function useUpdateEvent(eventId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (dto: EventUpdateDto) => updateEventApi(eventId, dto),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["event-detail", eventId] });
      qc.invalidateQueries({ queryKey: ["organizer-events"] });
    },
  });
}

export function useUploadEventImage(eventId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (payload: { file: File; isPrimary?: boolean }) => {
      const body: UploadEventImageBody = { image: payload.file } as any;
      const params: UploadEventImageParams = payload.isPrimary ? { isPrimary: true } : {};
      return uploadEventImageApi(eventId, body, params);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["event-detail", eventId] });
      qc.invalidateQueries({ queryKey: ["event-images", eventId] });
    },
  });
}

export function useSaveCloudinaryImage(eventId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (req: CloudinaryImageRequest) => saveCloudinaryImageApi(eventId, req),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["event-detail", eventId] });
      qc.invalidateQueries({ queryKey: ["event-images", eventId] });
    },
  });
}

export function useDeleteEvent() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (eventId: string) => deleteEventApi(eventId),
    onSuccess: (_res, eventId) => {
      qc.invalidateQueries({ queryKey: ["organizer-events"] });
      qc.removeQueries({ queryKey: ["event-detail", eventId] });
      qc.removeQueries({ queryKey: ["event-images", eventId] });
    },
  });
}

export function useDeleteEventImage(eventId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (imageId: string) => deleteEventImageApi(eventId, imageId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["event-images", eventId] });
    },
  });
}


