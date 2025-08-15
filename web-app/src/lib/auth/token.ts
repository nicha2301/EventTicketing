let accessToken: string | null = null;

export const getAccessToken = (): string | null => {
  if (typeof window !== 'undefined') {
    if (!accessToken) {
      accessToken = localStorage.getItem('accessToken');
    }
  }
  return accessToken;
};

export const setAccessToken = (token: string): void => {
  accessToken = token;
  if (typeof window !== 'undefined') {
    localStorage.setItem('accessToken', token);
  }
};

export const clearAccessToken = (): void => {
  accessToken = null;
  if (typeof window !== 'undefined') {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken'); 
    localStorage.removeItem('auth-storage');
  }
};

export const getRefreshToken = (): string | null => {
  return null;
};



