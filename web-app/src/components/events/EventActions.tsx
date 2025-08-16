"use client";

import { useState } from "react";
import { Heart, Share2, Bookmark } from "lucide-react";
import { Button } from "@/components/ui/button";

interface EventActionsProps {
  event: any;
  isLiked: boolean;
  onLikeChange: (liked: boolean) => void;
}

export function EventActions({ event, isLiked, onLikeChange }: EventActionsProps) {
  const [isSaved, setIsSaved] = useState(false);
  const [isSharing, setIsSharing] = useState(false);

  const handleLike = () => {
    onLikeChange(!isLiked);
    // TODO: API call to like/unlike event
  };

  const handleSave = () => {
    setIsSaved(!isSaved);
    // TODO: API call to save/unsave event
  };

  const handleShare = async () => {
    setIsSharing(true);
    try {
      if (navigator.share) {
        await navigator.share({
          title: event.title,
          text: event.shortDescription,
          url: window.location.href,
        });
      } else {
        // Fallback: copy to clipboard
        await navigator.clipboard.writeText(window.location.href);
        // TODO: Show toast notification
      }
    } catch (error) {
      console.error('Error sharing:', error);
    } finally {
      setIsSharing(false);
    }
  };

  return (
    <div className="flex items-center gap-2">
      <Button
        variant="secondary"
        size="sm"
        onClick={handleLike}
        className={`h-10 w-10 p-0 rounded-full ${
          isLiked 
            ? 'bg-red-100 text-red-600 hover:bg-red-200' 
            : 'bg-white/90 backdrop-blur-sm hover:bg-white'
        }`}
      >
        <Heart className={`w-5 h-5 ${isLiked ? 'fill-current' : ''}`} />
      </Button>

      <Button
        variant="secondary"
        size="sm"
        onClick={handleSave}
        className={`h-10 w-10 p-0 rounded-full ${
          isSaved 
            ? 'bg-blue-100 text-blue-600 hover:bg-blue-200' 
            : 'bg-white/90 backdrop-blur-sm hover:bg-white'
        }`}
      >
        <Bookmark className={`w-5 h-5 ${isSaved ? 'fill-current' : ''}`} />
      </Button>

      <Button
        variant="secondary"
        size="sm"
        onClick={handleShare}
        disabled={isSharing}
        className="h-10 w-10 p-0 rounded-full bg-white/90 backdrop-blur-sm hover:bg-white"
      >
        <Share2 className="w-5 h-5" />
      </Button>
    </div>
  );
}
