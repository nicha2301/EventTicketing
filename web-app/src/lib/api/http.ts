import axios, { type AxiosRequestConfig, type AxiosResponse } from "axios";
import { getAccessToken, clearAccessToken } from "@/lib/auth/token";

const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";

export const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

export const httpClient = api;

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers = config.headers ?? {};
    (config.headers as Record<string, string>)["Authorization"] = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    if (error?.response?.status === 401 && !original._retry) {
      original._retry = true;
      try {
        await axios.post(`${baseURL}/api/auth/refresh-token`, {
        }, { withCredentials: true });
        return api(original);
      } catch (refreshError) {
        clearAccessToken();
        if (typeof window !== 'undefined') {
          const event = new CustomEvent('auth-clear');
          window.dispatchEvent(event);
        }
      }
    }
    return Promise.reject(error);
  }
);

export function http<T = unknown>(config: AxiosRequestConfig): Promise<AxiosResponse<T>> {
  return api.request<T>(config);
}


