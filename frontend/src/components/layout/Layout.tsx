import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { LayoutDashboard, Map, PlusCircle, LogOut, Compass, Database, Bell } from 'lucide-react';
import SyncStatusIndicator from '../SyncStatusIndicator';

export default function Layout() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const initials = user ? `${user.firstName[0]}${user.lastName[0]}` : 'U';

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <Compass size={28} style={{ color: '#6366f1' }} />
          <h1>VoyageCraft</h1>
        </div>

        <nav className="sidebar-nav">
          <NavLink to="/dashboard" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            <LayoutDashboard size={18} /> Dashboard
          </NavLink>
          <NavLink to="/trips" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            <Map size={18} /> My Trips
          </NavLink>
          <NavLink to="/trips/create" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            <PlusCircle size={18} /> Create Trip
          </NavLink>
          <NavLink to="/offline" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            <Database size={18} /> Offline Mode
          </NavLink>
          <NavLink to="/notifications" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            <Bell size={18} /> Notifications
          </NavLink>
        </nav>

        <div style={{ padding: '8px 16px' }}>
          <SyncStatusIndicator />
        </div>

        <div className="sidebar-footer">
          <div className="sidebar-user">
            <div className="avatar">{initials}</div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: '14px', fontWeight: 600 }}>{user?.firstName} {user?.lastName}</div>
              <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{user?.email}</div>
            </div>
          </div>
          <button className="sidebar-link" onClick={handleLogout} style={{ marginTop: '8px', color: 'var(--danger)' }}>
            <LogOut size={18} /> Logout
          </button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
