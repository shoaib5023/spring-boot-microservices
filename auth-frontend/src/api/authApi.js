import { authClient } from "./axiosClient";

export const registerUser = async (userData) => {
  const response = await authClient.post("/register", userData);
  return response.data;
};

export const loginUser = async (credentials) => {
  const response = await authClient.post("/login", credentials);
  return response.data;
};

export const refreshAccessToken = async (refreshToken) => {
  const response = await authClient.post("/refresh", {
    refreshToken,
  });

  return response.data;
};

export const logoutUser = async (refreshToken) => {
  return authClient.post("/logout", { refreshToken });
};