import { useState } from "react";
import { registerUser } from "../api/authApi";
import { getApiErrorMessage } from "../utils/apiErrorHandler";

export default function Register() {
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    firstName: "",
    lastName: "",
  });

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");


  const handleChange = (event) => {
    setForm({
      ...form,
      [event.target.name]: event.target.value,
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      const data = await registerUser(form);
      setMessage(data.message || "Registration successful");
    } catch (error) {
       const result = getApiErrorMessage(error, "Auth Service");
      setError(result.message);
      // setError(
      //   error.response?.data?.message ||
      //     "Registration failed. Please try again."
      // );
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Create account</h2>

      {["username", "email", "password", "firstName", "lastName"].map(
        (field) => (
          <input
            key={field}
            name={field}
            type={field === "password" ? "password" : "text"}
            placeholder={field}
            value={form[field]}
            onChange={handleChange}
            required={["username", "email", "password"].includes(field)}
          />
        )
      )}

      <button type="submit">Register</button>

      {message && <p>{message}</p>}
      {error && <p role="alert">{error}</p>}
    </form>
  );
}