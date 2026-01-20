import { BrowserRouter as Router, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Profile from './pages/settings/Profile';
import Organization from './pages/settings/Organization';
import Security from './pages/settings/Security';
import Apps from './pages/Apps';
import Invitations from './pages/Invitations';
import DashboardLayout from './components/layout/DashboardLayout';
import { useAuth } from './context/AuthContext';

// Protected Route Wrapper
function ProtectedRoute() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return <div className="flex h-screen items-center justify-center">Loading...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Wrap content in Dashboard Layout
  return (
    <DashboardLayout>
      <Outlet />
    </DashboardLayout>
  );
}

import { Toaster } from './components/ui/Toaster';

function App() {
  return (
    <Router>
      <Toaster />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/invitations" element={<Invitations />} />

        {/* Protected Dashboard Routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<Navigate to="/settings/profile" replace />} />
          <Route path="/apps" element={<Apps />} />
          <Route path="/settings/profile" element={<Profile />} />
          <Route path="/settings/organization" element={<Organization />} /> {/* Added Organization Route */}
          <Route path="/settings/security" element={<Security />} />
          {/* Fallback */}
          <Route path="*" element={<Navigate to="/settings/profile" replace />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
