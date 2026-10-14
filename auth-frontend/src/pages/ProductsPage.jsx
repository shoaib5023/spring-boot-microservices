import { useEffect, useState } from "react";
import { getProducts } from "../api/productApi";
import { getApiErrorMessage } from "../utils/apiErrorHandler";

export default function Products() {
  const [products, setProducts] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    getProducts()
      .then(setProducts)
      .catch((error) => {
        const result = getApiErrorMessage(error, "Product Service");
              setError(result.message);
      });
  }, []);


  return (
    <main>
      <h2>Products</h2>
     {error && <p role="alert">{error}</p>}

      {products.map((product) => (
        <div key={product.id}>
          <h3>{product.name}</h3>
          <p>{product.description}</p>
        </div>
      ))}
    </main>
  );
}