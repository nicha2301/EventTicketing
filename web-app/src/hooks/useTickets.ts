'use client'

import { useInitialization } from '@/hooks/useInitialization'
import { ApiResponsePageTicketDto, ApiResponsePageTicketTypeDto, TicketCheckInRequestDto, TicketDtoStatus } from '@/lib/api'
import { checkInTicketWithRequest } from '@/lib/api/generated/client'
import { cancelUserTicket, getEventTickets, getEventTicketTypes, getUserTickets } from '@/lib/api/modules/tickets'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

export function useTickets(status?: TicketDtoStatus, page = 0) {
  const queryClient = useQueryClient()
  const { isAuthenticated } = useInitialization()

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ['my-tickets', status, page],
    queryFn: ({ signal }) => getUserTickets(status, page, 10, signal),
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000,
    retry: 2,
  })

  const cancelTicketMutation = useMutation({
    mutationFn: (ticketId: string) => cancelUserTicket(ticketId),
    onSuccess: () => {
      toast.success('Đã hủy vé thành công')
      queryClient.invalidateQueries({ queryKey: ['my-tickets'] })
    },
    onError: (error: any) => {
      toast.error('Có lỗi xảy ra khi hủy vé')
      console.error('Cancel ticket error:', error)
    }
  })

  const tickets = (data as any)?.data?.content || []
  const totalPages = (data as any)?.data?.totalPages || 0

  return {
    tickets,
    totalPages,
    isLoading,
    isError,
    error,
    refetch,
    cancelTicketMutation,
  }
}

export function useEventTickets(eventId: string, page = 0, size = 10) {
  return useQuery<ApiResponsePageTicketDto>({
    queryKey: ['event-tickets', eventId, page, size],
    enabled: !!eventId,
    queryFn: ({ signal }) => getEventTickets(eventId, page, size, signal),
    retry: 1,
  })
}

export function useEventTicketTypes(eventId: string, page = 0, size = 10) {
  return useQuery<ApiResponsePageTicketTypeDto>({
    queryKey: ['event-ticket-types', eventId, page, size],
    enabled: !!eventId,
    queryFn: ({ signal }) => getEventTicketTypes(eventId, page, size, signal),
    retry: 1,
  })
}

export function useCheckIn() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: TicketCheckInRequestDto) =>
      checkInTicketWithRequest(payload),
    onSuccess: (_res, vars) => {
      if (vars.eventId) {
        queryClient.invalidateQueries({
          queryKey: ['event-tickets', vars.eventId],
        })
      }
    },
  })
}