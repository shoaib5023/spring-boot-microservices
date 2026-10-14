import axios from "axios";

export const authClient = axios.create({
  baseURL: import.meta.env.VITE_AUTH_API_URL,
  headers: { "Content-Type": "application/json" },
});

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_PRODUCT_API_URL,
  headers: { "Content-Type": "application/json" },
});

apiClient.interceptors.request.use((config) => {
  const accessToken = sessionStorage.getItem("accessToken");

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

let refreshPromise = null;

apiClient.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      originalRequest.url?.includes("/refresh")
    ) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      // Share one refresh request if several APIs fail together.
      if (!refreshPromise) {
        const refreshToken = sessionStorage.getItem("refreshToken");

        if (!refreshToken) {
          throw new Error("No refresh token available");
        }

        refreshPromise = authClient
          .post("/refresh", { refreshToken })
          .then(({ data }) => {
            sessionStorage.setItem("accessToken", data.accessToken);

            // Support refresh-token rotation.
            if (data.refreshToken) {
              sessionStorage.setItem("refreshToken", data.refreshToken);
            }

            return data.accessToken;
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      const newAccessToken = await refreshPromise;

      originalRequest.headers.Authorization =
        `Bearer ${newAccessToken}`;

      return apiClient(originalRequest);
    } catch (refreshError) {
      sessionStorage.removeItem("accessToken");
      sessionStorage.removeItem("refreshToken");
      // Redirect to login only when refresh fails.
      window.location.assign("/login");

      return Promise.reject(refreshError);
    }
  }
);