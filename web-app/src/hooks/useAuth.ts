import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuthStore } from "@/store/auth";
import { authApi, type LoginRequest, type RegisterRequest, type GoogleLoginRequest } from "@/lib/api/auth";
import { toast } from "sonner";
import { useRouter } from "next/navigation";

export const useLogin = () => {
  const { setSession, setLoading, clearSession } = useAuthStore();
  const queryClient = useQueryClient();
  const router = useRouter();

  return useMutation({
    mutationFn: authApi.login,
    onMutate: () => {
      clearSession();
      setLoading(true);
    },
    onSuccess: async (data) => {
      setSession(data.token, {
        id: data.id,
        email: data.email,
        fullName: data.fullName,
        role: data.role,
        profilePictureUrl: data.profilePictureUrl,
      }, data.refreshToken);

      try { await fetch("/api/auth/role", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ role: data.role }) }); } catch {}
      
      toast.success("Đăng nhập thành công!");
      queryClient.invalidateQueries(); 

      try {
        const returnTo = typeof window !== 'undefined' ? sessionStorage.getItem('returnTo') : null;
        if (returnTo) {
          sessionStorage.removeItem('returnTo');
          router.push(returnTo);
          return;
        }
      } catch {}
      
      if (data.role === "ADMIN") {
        router.push("/admin");
      } else if (data.role === "ORGANIZER") {
        router.push("/organizer/events");
      } else {
        router.push("/");
      }
    },
    onError: (error: any) => {
      clearSession(); 
      const message = error?.response?.data?.message || "Đăng nhập thất bại";
      toast.error(message);
    },
    onSettled: () => {
      setLoading(false);
    },
  });
};

export const useRegister = () => {
  const { setLoading } = useAuthStore();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: authApi.register,
    onMutate: () => {
      setLoading(true);
    },
    onSuccess: (data) => {
      toast.success("Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
      queryClient.invalidateQueries();
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || "Đăng ký thất bại";
      toast.error(message);
    },
    onSettled: () => {
      setLoading(false);
    },
  });
};

export const useGoogleLogin = () => {
  const { setSession, setLoading } = useAuthStore();
  const queryClient = useQueryClient();
  const router = useRouter();

  return useMutation({
    mutationFn: authApi.loginWithGoogle,
    onMutate: () => {
      setLoading(true);
    },
    onSuccess: async (data) => {
      setSession(data.token, {
        id: data.id,
        email: data.email,
        fullName: data.fullName,
        role: data.role,
        profilePictureUrl: data.profilePictureUrl,
      }, data.refreshToken);
      
      try { await fetch("/api/auth/role", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ role: data.role }) }); } catch {}
      
      toast.success("Đăng nhập Google thành công!");
      queryClient.invalidateQueries();

      try {
        const returnTo = typeof window !== 'undefined' ? sessionStorage.getItem('returnTo') : null;
        if (returnTo) {
          sessionStorage.removeItem('returnTo');
          router.push(returnTo);
          return;
        }
      } catch {}
      
      if (data.role === "ADMIN") {
        router.push("/admin");
      } else if (data.role === "ORGANIZER") {
        router.push("/organizer/events");
      } else {
        router.push("/");
      }
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || "Đăng nhập Google thất bại";
      toast.error(message);
    },
    onSettled: () => {
      setLoading(false);
    },
  });
};

export const useLogout = () => {
  const { clearSession, setLoading } = useAuthStore();
  const queryClient = useQueryClient();
  const router = useRouter();

  return useMutation({
    mutationFn: async () => {
      const token = useAuthStore.getState().currentUser ? localStorage.getItem("accessToken") || "" : "";
      if (token) {
        await authApi.logout({ token });
      }
    },
    onMutate: () => {
      setLoading(true);
    },
    onSuccess: async () => {
      clearSession();
      queryClient.clear();
      try { await fetch("/api/auth/role", { method: "DELETE" }); } catch {}
      toast.success("Đăng xuất thành công!");
      router.push("/");
    },
    onError: async (error: any) => {
      clearSession();
      queryClient.clear();
      try { await fetch("/api/auth/role", { method: "DELETE" }); } catch {}
      const message = error?.response?.data?.message || "Đăng xuất thành công!";
      toast.success(message);
      router.push("/");
    },
    onSettled: () => {
      setLoading(false);
    },
  });
};

export const useForgotPassword = () => {
  return useMutation({
    mutationFn: authApi.forgotPassword,
    onSuccess: () => {
      toast.success("Email khôi phục mật khẩu đã được gửi!");
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || "Gửi email thất bại";
      toast.error(message);
    },
  });
};

export const useResetPassword = () => {
  const router = useRouter();

  return useMutation({
    mutationFn: authApi.resetPassword,
    onSuccess: () => {
      toast.success("Đặt lại mật khẩu thành công!");
      router.push("/?login=1");
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || "Đặt lại mật khẩu thất bại";
      toast.error(message);
    },
  });
};

export const useResendActivationEmail = () => {
  return useMutation({
    mutationFn: authApi.resendActivationEmail,
    onSuccess: () => {
      toast.success("Email xác nhận đã được gửi lại!");
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || "Gửi lại email thất bại";
      toast.error(message);
    },
  });
};
