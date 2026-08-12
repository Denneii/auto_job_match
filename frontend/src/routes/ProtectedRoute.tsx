import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const ProtectedRoute = () => {
  const { token } = useAuth();

  // Se não houver token, redireciona para o login
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // Se houver token, renderiza a rota filha
  return <Outlet />;
};