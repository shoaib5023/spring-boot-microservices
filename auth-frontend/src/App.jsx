import { BrowserRouter, Routes, Route, Link } from "react-router-dom";

import ProtectedRoute from "./components/ProtectedRoute";
import Register from "./pages/RegisterPage";
import Login from "./pages/LoginPage";
import Products from "./pages/ProductsPage";

export default function App() {
  return (
    <BrowserRouter>
      <nav>
        <Link to="/register">Register</Link>{" | "}
        <Link to="/login">Login</Link>{" | "}
        <Link to="/products">Products</Link>
      </nav>

      <Routes>
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />

        <Route element={<ProtectedRoute />}>
          <Route path="/products" element={<Products />} />
        </Route>

        <Route path="*" element={<Login />} />
      </Routes>
    </BrowserRouter>
  );
}