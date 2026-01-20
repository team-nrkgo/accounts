import { BrowserRouter as Router, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Profile from './pages/settings/Profile';
import Organization from './pages/settings/Organization';
import Security from './pages/settings/Security';
import Apps from './pages/Apps';
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

function DashboardPlaceholder() {
  return <div className="p-10"><h1>Dashboard (Coming Soon)</h1></div>
}

import { Toaster } from './components/ui/Toaster';

function App() {
  return (
    <Router>
      <Toaster />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />

        {/* Protected Dashboard Routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<DashboardPlaceholder />} />
          <Route path="/apps" element={<Apps />} />
          <Route path="/settings/profile" element={<Profile />} />
          <Route path="/settings/organization" element={<Organization />} /> {/* Added Organization Route */}
          <Route path="/settings/security" element={<Security />} />
          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
