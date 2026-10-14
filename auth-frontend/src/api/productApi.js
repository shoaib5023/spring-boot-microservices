import { apiClient } from "./axiosClient";

export const getProducts = async () => {
  const response = await apiClient.get("/products");
  console.log("response:",response.data)
  return response.data;
};


// import axios from "axios";

// const productApi = axios.create({
//   baseURL: import.meta.env.VITE_PRODUCT_API_URL,
// });

// productApi.interceptors.request.use(
//   (config) => {
//     const token = sessionStorage.getItem("accessToken");

//     console.log("Access token exists:", Boolean(token));

//     if (token) {
//       config.headers.Authorization = `Bearer ${token}`;
//     }

//     return config;
//   },
//   (error) => Promise.reject(error)
// );

// export default productApi;