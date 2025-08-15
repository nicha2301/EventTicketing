import { http } from "../http";
import type { LoginRequestDto, UserCreateDto, UserAuthResponseDto } from "../generated/client";

export interface LoginResponse {
  token: string;
  id: string;
  email: string;
  role: string;
  fullName: string;
}

export interface RegisterResponse {
  token: string;
  id: string;
  email: string;
  role: string;
  fullName: string;
}

export const login = async (credentials: LoginRequestDto): Promise<LoginResponse> => {
  const response = await http<{ data: UserAuthResponseDto }>({
    url: "/api/auth/login",
    method: "POST",
    data: credentials,
  });

  const { data } = response.data;
  if (!data) {
    throw new Error("Login failed");
  }

  return {
    token: data.token,
    id: data.id,
    email: data.email,
    role: data.role,
    fullName: data.fullName,
  };
};

export const register = async (userData: UserCreateDto): Promise<RegisterResponse> => {
  const response = await http<{ data: UserAuthResponseDto }>({
    url: "/api/auth/register",
    method: "POST",
    data: userData,
  });

  const { data } = response.data;
  if (!data) {
    throw new Error("Registration failed");
  }

  return {
    token: data.token,
    id: data.id,
    email: data.email,
    role: data.role,
    fullName: data.fullName,
  };
};

export const logout = async (): Promise<void> => {
  await http({
    url: "/api/auth/logout",
    method: "POST",
  });
};

export const refreshToken = async (): Promise<void> => {
  await http({
    url: "/api/auth/refresh-token",
    method: "POST",
  });
};
