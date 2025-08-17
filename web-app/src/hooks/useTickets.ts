'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { getUserTickets, cancelUserTicket } from '@/lib/api/modules/tickets'
import { useInitialization } from '@/hooks/useInitialization'
import { TicketDtoStatus } from '@/lib/api'

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
