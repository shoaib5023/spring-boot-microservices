import { Navigate, Outlet } from "react-router-dom";

export default function ProtectedRoute() {
  const accessToken = sessionStorage.getItem("accessToken");

  return accessToken
    ? <Outlet />
    : <Navigate to="/login" replace />;
}