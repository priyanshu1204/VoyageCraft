import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { useEffect } from 'react';
import { useAuthStore } from './store/authStore';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import TripsListPage from './pages/TripsListPage';
import TripCreatePage from './pages/TripCreatePage';
import TripDetailPage from './pages/TripDetailPage';
import ItineraryPage from './pages/ItineraryPage';
import TransportPage from './pages/TransportPage';
import StaysPage from './pages/StaysPage';
import ActivitiesPage from './pages/ActivitiesPage';
import BudgetPage from './pages/BudgetPage';
import CollaborationPage from './pages/CollaborationPage';
import PackingPage from './pages/PackingPage';
import OfflinePage from './pages/OfflinePage';
import WeatherPage from './pages/WeatherPage';
import TravelDocumentsPage from './pages/TravelDocumentsPage';
import NavigationPage from './pages/NavigationPage';
import NotificationsPage from './pages/NotificationsPage';
import AnalyticsPage from './pages/AnalyticsPage';
import QuickActionsPage from './pages/QuickActionsPage';
import Layout from './components/layout/Layout';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
}

function App() {
  const { loadFromStorage } = useAuthStore();

  useEffect(() => {
    loadFromStorage();
  }, [loadFromStorage]);

  return (
    <BrowserRouter>
      <Toaster position="top-right" toastOptions={{ className: 'toast-custom', duration: 3000 }} />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
          <Route index element={<Navigate to="/dashboard" />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="trips" element={<TripsListPage />} />
          <Route path="trips/create" element={<TripCreatePage />} />
          <Route path="trips/:id" element={<TripDetailPage />} />
          <Route path="trips/:id/itinerary" element={<ItineraryPage />} />
          <Route path="trips/:id/transport" element={<TransportPage />} />
          <Route path="trips/:id/stays" element={<StaysPage />} />
          <Route path="trips/:id/activities" element={<ActivitiesPage />} />
          <Route path="trips/:id/budget" element={<BudgetPage />} />
          <Route path="trips/:id/collaborate" element={<CollaborationPage />} />
          <Route path="trips/:id/packing" element={<PackingPage />} />
          <Route path="offline" element={<OfflinePage />} />
          <Route path="trips/:id/weather" element={<WeatherPage />} />
          <Route path="trips/:id/documents" element={<TravelDocumentsPage />} />
          <Route path="trips/:id/navigation" element={<NavigationPage />} />
          <Route path="notifications" element={<NotificationsPage />} />
          <Route path="trips/:id/alerts" element={<NotificationsPage />} />
          <Route path="trips/:id/analytics" element={<AnalyticsPage />} />
          <Route path="trips/:id/quick-actions" element={<QuickActionsPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
