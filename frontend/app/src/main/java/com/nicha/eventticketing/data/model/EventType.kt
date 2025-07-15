package com.nicha.eventticketing.data.model

enum class EventType {
    MUSIC, SPORT, SPORTS, EDUCATION, ART, ARTS, BUSINESS, FOOD, TECHNOLOGY, OTHER;
    
    companion object {
        fun getIcon(type: EventType): String {
            return when (type) {
                MUSIC -> "🎵"
                SPORT, SPORTS -> "⚽"
                EDUCATION -> "📚"
                ART, ARTS -> "🎨"
                BUSINESS -> "💼"
                FOOD -> "🍽️"
                TECHNOLOGY -> "💻"
                OTHER -> "🎪"
            }
        }
        
        fun getColor(type: EventType): Int {
            return when (type) {
                MUSIC -> 0xFF6200EA.toInt()
                SPORT, SPORTS -> 0xFF2962FF.toInt()
                EDUCATION -> 0xFF00C853.toInt()
                ART, ARTS -> 0xFFD50000.toInt()
                BUSINESS -> 0xFF00C853.toInt()
                FOOD -> 0xFFFF6D00.toInt()
                TECHNOLOGY -> 0xFF304FFE.toInt()
                OTHER -> 0xFF757575.toInt()
            }
        }
    }
} 