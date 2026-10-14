import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../api/authApi";
import { getApiErrorMessage } from "../utils/apiErrorHandler";

export default function Login() {
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    try {
      const data = await loginUser({ username, password });

      sessionStorage.setItem("accessToken", data.accessToken);
      sessionStorage.setItem("refreshToken", data.refreshToken);

      navigate("/products");
    } catch (error) {
      // setError(
      //   err.response?.data?.message ||
      //     "Login failed. Check your credentials."
      // );
      const result = getApiErrorMessage(error, "Auth Service");
            setError(result.message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Login</h2>

      <input
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        placeholder="Username"
        autoComplete="username"
        required
      />

      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Password"
        autoComplete="current-password"
        required
      />

      <button type="submit">Login</button>

      {error && <p role="alert">{error}</p>}
    </form>
  );
}