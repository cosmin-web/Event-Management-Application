import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { jwtDecode } from "jwt-decode"; 
import Navbar from './components/Navbar';

import Login from './pages/Login';
import Register from './pages/Register';
import EventsPage from './pages/EventsPage';
import MyProfile from './pages/MyProfile';
import EditProfile from './pages/EditProfile';
import OwnerDashboard from './pages/OwnerDashboard';
import CreateEvent from './pages/CreateEvent';
import EditEvent from './pages/EditEvent';
import EventClients from './pages/EventClients';
import TicketStats from './pages/TicketStats';
import CreatePackage from './pages/CreatePackage';
import EditPackage from './pages/EditPackage';
import ManagePackage from './pages/ManagePackage';
import PackageClientsPage from './pages/PackageClientsPage';
import AdminDashboard from './pages/AdminDashboard';
import TicketValidator from './pages/TicketValidator';

interface CustomJwtPayload {
    sub: string;
    rol?: string;
    role?: string;
    exp: number;
}

interface ProtectedRouteProps {
    children: React.ReactNode;
    allowedRoles?: string[];
}

const ProtectedRoute = ({ children, allowedRoles }: ProtectedRouteProps) => {
    const token = localStorage.getItem('token');
    
    if (!token) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles) {
        try {
            const decoded = jwtDecode<CustomJwtPayload>(token);
            const userRole = decoded.rol || decoded.role; 

            const currentTime = Date.now() / 1000;
            if (decoded.exp < currentTime) {
                localStorage.removeItem('token');
                return <Navigate to="/login" replace />;
            }

            if (!userRole || !allowedRoles.includes(userRole)) {
                return <Navigate to="/" replace />;
            }

        } catch (error) {
            localStorage.removeItem('token');
            return <Navigate to="/login" replace />;
        }
    }

    return <>{children}</>;
};

function App() {
    return (
        <Router>
            <Navbar />
            <div className="main-content">
                <Routes>
                    <Route path="/" element={<EventsPage />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />

                    <Route path="/profile" element={<ProtectedRoute><MyProfile /></ProtectedRoute>} />
                    <Route path="/edit-profile" element={<ProtectedRoute><EditProfile /></ProtectedRoute>} />

                    <Route path="/owner-dashboard" element={
                        <ProtectedRoute allowedRoles={['OWNER_EVENT', 'ADMIN']}>
                            <OwnerDashboard />
                        </ProtectedRoute>
                    } />
                    <Route path="/create-event" element={
                        <ProtectedRoute allowedRoles={['OWNER_EVENT']}>
                            <CreateEvent />
                        </ProtectedRoute>
                    } />
                    <Route path="/edit-event/:eventId" element={
                        <ProtectedRoute allowedRoles={['OWNER_EVENT']}>
                            <EditEvent />
                        </ProtectedRoute>
                    } />
                    <Route path="/event-clients/:eventId" element={
                        <ProtectedRoute allowedRoles={['OWNER_EVENT']}>
                            <EventClients />
                        </ProtectedRoute>
                    } />
                    
                    <Route path="/ticket-stats/:type/:id" element={
                        <ProtectedRoute allowedRoles={['OWNER_EVENT']}>
                            <TicketStats />
                        </ProtectedRoute>
                    } />
                    
                    <Route path="/create-package" element={
                        <ProtectedRoute allowedRoles={['OWNER_EVENT']}>
                            <CreatePackage />
                        </ProtectedRoute>
                    } />
                    <Route path="/edit-package/:id" element={
                        <ProtectedRoute allowedRoles={['OWNER_EVENT']}>
                            <EditPackage />
                        </ProtectedRoute>
                    } />
                    <Route path="/manage-package/:id" element={
                        <ProtectedRoute allowedRoles={['OWNER_EVENT']}>
                            <ManagePackage />
                        </ProtectedRoute>
                    } />
                    <Route path="/package-clients/:id" element={
                        <ProtectedRoute allowedRoles={['OWNER_EVENT']}>
                            <PackageClientsPage />
                        </ProtectedRoute>
                    } />
                    
                    <Route path="/scan" element={
                        <ProtectedRoute allowedRoles={['OWNER_EVENT', 'ADMIN']}>
                            <TicketValidator />
                        </ProtectedRoute>
                    } />

                    <Route path="/admin-dashboard" element={
                        <ProtectedRoute allowedRoles={['ADMIN']}>
                            <AdminDashboard />
                        </ProtectedRoute>
                    } />

                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </div>
        </Router>
    );
}

export default App;