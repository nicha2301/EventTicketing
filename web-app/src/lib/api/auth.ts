import { httpClient } from "./http";

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phoneNumber: string; 
  role?: "USER" | "ORGANIZER" | "ADMIN";
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface GoogleLoginRequest {
  idToken: string;
  email: string;
  name: string;
  profilePictureUrl?: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface LogoutRequest {
  token: string;
}

export interface PasswordResetRequest {
  token: string;
  newPassword: string;
}

// Response types
export interface AuthResponse {
  id: string;
  email: string;
  fullName: string;
  role: "USER" | "ORGANIZER" | "ADMIN";
  token: string;
  refreshToken?: string;
  profilePictureUrl?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

// Auth API functions
export const authApi = {
  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await httpClient.post<ApiResponse<AuthResponse>>(
      "/api/auth/register",
      data
    );
    return response.data.data;
  },

  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await httpClient.post<ApiResponse<AuthResponse>>(
      "/api/auth/login", 
      data
    );
    return response.data.data;
  },

  async loginWithGoogle(data: GoogleLoginRequest): Promise<AuthResponse> {
    const response = await httpClient.post<ApiResponse<AuthResponse>>(
      "/api/auth/google",
      data
    );
    return response.data.data;
  },

  async refreshToken(data: RefreshTokenRequest): Promise<AuthResponse> {
    const response = await httpClient.post<ApiResponse<AuthResponse>>(
      "/api/auth/refresh-token",
      data
    );
    return response.data.data;
  },

  async logout(data: LogoutRequest): Promise<void> {
    await httpClient.post<ApiResponse<null>>("/api/auth/logout", data);
  },

  async activateAccount(token: string): Promise<void> {
    await httpClient.get<ApiResponse<null>>(`/api/auth/activate?token=${token}`);
  },

  async forgotPassword(email: string): Promise<void> {
    await httpClient.post<ApiResponse<null>>(
      `/api/auth/password/forgot?email=${email}`
    );
  },

  async resetPassword(data: PasswordResetRequest): Promise<void> {
    await httpClient.post<ApiResponse<null>>("/api/auth/password/reset", data);
  },

  async healthCheck(): Promise<{ status: string }> {
    const response = await httpClient.get<ApiResponse<{ status: string }>>(
      "/api/auth/health"
    );
    return response.data.data;
  },

  async resendActivationEmail(email: string): Promise<void> {
    await httpClient.post<ApiResponse<null>>(
      `/api/auth/resend-activation?email=${email}`
    );
  },
};
