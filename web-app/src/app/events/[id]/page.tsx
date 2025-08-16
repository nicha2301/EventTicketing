'use client'

import { Suspense, useEffect, useState } from 'react'
import { notFound } from 'next/navigation'
import { fetchEventById } from '@/lib/api/events'
import { EventDto } from '@/lib/api/generated/client'
import { EventDetailContent } from '@/components/events/EventDetailContent'
import { EventDetailSkeleton } from '@/components/events/EventDetailSkeleton'

interface EventDetailPageProps {
  params: Promise<{ id: string }>
}

function EventDetailPageContent({ eventId }: { eventId: string }) {
  const [event, setEvent] = useState<EventDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadEvent() {
      try {
        setLoading(true)
        const eventData = await fetchEventById(eventId)
        
        if (!eventData) {
          notFound()
        }
        
        setEvent(eventData)
      } catch (error) {
        console.error('Failed to fetch event:', error)
        
        // If event not found or access denied, show 404
        if (error instanceof Error && (
          error.message.includes('404') || 
          error.message.includes('401') ||
          error.message.includes('403')
        )) {
          notFound()
        }
        
        setError('Không thể tải thông tin sự kiện')
      } finally {
        setLoading(false)
      }
    }

    loadEvent()
  }, [eventId])

  if (loading) {
    return <EventDetailSkeleton />
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Lỗi tải dữ liệu</h1>
          <p className="text-gray-600">{error}</p>
        </div>
      </div>
    )
  }

  if (!event) {
    notFound()
  }

  return <EventDetailContent event={event} />
}

export default function EventDetailPage({ params }: EventDetailPageProps) {
  const [eventId, setEventId] = useState<string | null>(null)

  useEffect(() => {
    params.then(({ id }) => setEventId(id))
  }, [params])

  if (!eventId) {
    return <EventDetailSkeleton />
  }

  return <EventDetailPageContent eventId={eventId} />
}
