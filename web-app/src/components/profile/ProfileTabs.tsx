'use client'

import { LucideIcon } from 'lucide-react'
import { cn } from '@/lib/utils/cn'

interface Tab {
  id: string
  label: string
  icon: LucideIcon
  description: string
}

interface ProfileTabsProps {
  tabs: Tab[]
  activeTab: string
  onTabChange: (tabId: string) => void
}

export function ProfileTabs({ tabs, activeTab, onTabChange }: ProfileTabsProps) {
  return (
    <nav className="space-y-2">
      {tabs.map((tab) => {
        const Icon = tab.icon
        const isActive = activeTab === tab.id
        
        return (
          <button
            key={tab.id}
            onClick={() => onTabChange(tab.id)}
            className={cn(
              'w-full flex items-start gap-3 p-3 rounded-lg text-left transition-all duration-200',
              isActive 
                ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600' 
                : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
            )}
          >
            <Icon className={cn(
              'w-5 h-5 mt-0.5 flex-shrink-0',
              isActive ? 'text-blue-600' : 'text-gray-400'
            )} />
            <div className="min-w-0 flex-1">
              <div className={cn(
                'font-medium text-sm',
                isActive ? 'text-blue-900' : 'text-gray-900'
              )}>
                {tab.label}
              </div>
              <div className="text-xs text-gray-500 mt-0.5 leading-relaxed">
                {tab.description}
              </div>
            </div>
          </button>
        )
      })}
    </nav>
  )
}
