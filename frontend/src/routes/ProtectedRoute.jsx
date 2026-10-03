import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../store/AuthContext'

export default function ProtectedRoute({ children }) {
  const { daDangNhap } = useAuth()
  const location = useLocation()

  if (!daDangNhap) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  return children
}