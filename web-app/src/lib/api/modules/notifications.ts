import {
    countUnreadNotifications,
    deleteAllNotifications as deleteAllNotificationsAPI,
    deleteNotification as deleteNotificationAPI,
    getNotificationPreferences as getNotificationPreferencesAPI,
    markAllAsRead,
    markAsRead,
    NotificationResponse,
    updateNotificationPreferences as updateNotificationPreferencesAPI
} from '../generated/client';
import { http } from "../http";

export async function getUserNotifications(
  page = 0,
  size = 50,
  signal?: AbortSignal
) {
  try {
    const params = new URLSearchParams();
    params.set('page', page.toString());
    params.set('size', size.toString());
    
    const response = await http<[NotificationResponse[]]>({
      url: `/api/notifications?${params.toString()}`,
      method: 'GET',
      signal
    });
    return response;
  } catch (error) {
    throw error;
  }
}

export async function getUnreadNotificationsCount() {
  try {
    const response = await countUnreadNotifications();
    return response;
  } catch (error) {
    console.error('Error fetching unread notifications count:', error);
    throw error;
  }
}

export async function markNotificationAsRead(notificationId: string) {
  try {
    const response = await markAsRead(notificationId);
    return response;
  } catch (error) {
    console.error('Error marking notification as read:', error);
    throw error;
  }
}

export async function markAllNotificationsAsRead() {
  try {
    const response = await markAllAsRead();
    return response;
  } catch (error) {
    console.error('Error marking all notifications as read:', error);
    throw error;
  }
}

export async function deleteNotification(notificationId: string) {
  try {
    const response = await deleteNotificationAPI(notificationId);
    return response;
  } catch (error) {
    console.error('Error deleting notification:', error);
    throw error;
  }
}

export async function deleteAllNotifications() {
  try {
    const response = await deleteAllNotificationsAPI();
    return response;
  } catch (error) {
    console.error('Error deleting all notifications:', error);
    throw error;
  }
}

export async function getNotificationPreferences() {
  try {
    const response = await getNotificationPreferencesAPI();
    return response;
  } catch (error) {
    console.error('Error fetching notification preferences:', error);
    throw error;
  }
}

export async function updateNotificationPreferences(preferences: any) {
  try {
    const response = await updateNotificationPreferencesAPI(preferences);
    return response;
  } catch (error) {
    console.error('Error updating notification preferences:', error);
    throw error;
  }
}
