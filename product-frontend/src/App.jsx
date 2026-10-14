
import { useState } from "react";
import axios from "axios";
import "./App.css";
import { addProduct } from "./api/ProductsApi";

import { getApiErrorMessage } from "./utils/apiErrorHandler";

const AUTH_API_URL = import.meta.env.VITE_AUTH_API_URL;

function App() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [accessToken, setAccessToken] = useState("");

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [price, setPrice] = useState("");

  const [message, setMessage] = useState("");
  const [error,setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (event) => {
    event.preventDefault();
    setMessage("");
    setLoading(true);

    try {
      const response = await axios.post(
        `${AUTH_API_URL}/login`,
        { username, password }
      );

      setAccessToken(response.data.accessToken);
      setMessage("Login successful. You can add products now.");
    } catch (error) {
      const result = getApiErrorMessage(error, "Auth Service");
      setError(result.message);
      setMessage(
        error.response?.data?.message ||
        "Login failed. Check your credentials and API."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleAddProduct = async (event) => {
    event.preventDefault();
    setMessage("");

    if (!accessToken) {
      setMessage("Please log in first.");
      return;
    }

    setLoading(true);

    try {
      const response = await addProduct(
        {
          name,
          description,
          price: Number(price),
        },
        accessToken
      );

      setMessage(
        `Product added successfully! Product ID: ${response.data.id}`
      );

      setName("");
      setDescription("");
      setPrice("");

      setError("");
    } catch (error) {
        const result = getApiErrorMessage(error, "Product Service");
        setError(result.message);
      // if (error.response?.status === 401) {
      //   setMessage("Unauthorized. Please log in again.");
      // } else {
      //   setMessage(
      //     error.response?.data?.message ||
      //     "Could not add product. Check the backend logs."
      //   );
      // }
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    setAccessToken("");
    setUsername("");
    setPassword("");
    setMessage("Logged out successfully.");
  };

  return (

    <main className="page">
      <div className="container">
        <h1>Product Management</h1>
        <p className="subtitle">
          Sign in and add products to your catalogue.
        </p>
        {error && <p role="alert">{error}</p>}
        {!accessToken ? (
          <form className="panel" onSubmit={handleLogin}>
            <h2>Login</h2>

            <label>Username</label>
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoComplete="username"
            />

            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
            />

            <button disabled={loading}>
              {loading ? "Signing in..." : "Login"}
            </button>
          </form>
        ) : (
          <>
            <div className="panel">
              <h2>Add Product</h2>

              <form onSubmit={handleAddProduct}>
                <label>Product Name</label>
                <input
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. TravelHub"
                  required
                />

                <label>Description</label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Enter product description"
                  required
                />

                <label>Price</label>
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  placeholder="2499.00"
                  required
                />

                <button disabled={loading}>
                  {loading ? "Saving..." : "Add Product"}
                </button>
              </form>
            </div>

            <button className="logout" onClick={handleLogout}>
              Logout
            </button>
          </>
        )}

        {message && (
          <p className="message" role="status">
            {message}
          </p>
        )}
      </div>
    </main>
  );
}

export default App;