
import axios from "axios";

const productApi = axios.create({
  baseURL: import.meta.env.VITE_PRODUCT_API_URL,
});

export const addProduct = (product, accessToken) => {
  return productApi.post("/products", product, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
};

export default productApi;