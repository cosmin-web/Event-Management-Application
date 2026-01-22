import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import api from '../utils/api';

interface CustomJwtPayload {
    sub: string;     
    email?: string;  
    role?: string;   
    rol?: string;    
}

const Navbar = () => {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    
    let role: string | null = null;
    let email: string = '';

    if (token) {
        try {
            const decoded = jwtDecode<CustomJwtPayload>(token);
            role = decoded.role || decoded.rol || null;
            email = decoded.email || decoded.sub || '';
        } catch (e) {
            localStorage.removeItem('token');
        }
    }

    const handleLogout = async () => {
    const token = localStorage.getItem('token');
    
    if (token) {
        try {
                await api.post('/auth/logout', {}, {
                    headers: {
                     Authorization: `Bearer ${token}`
                  }
                });
                console.log("Token invalidat cu succes pe server.");
            } catch (e) {
                console.error("Eroare la delogarea de pe server:", e);
            }
        }

    localStorage.removeItem('token');
    window.location.href = '/login'; 
    };

    return (
        <nav style={{ background: 'white', padding: '15px 20px', borderBottom: '1px solid #e5e7eb', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>


            <Link to="/" style={{ textDecoration: 'none', fontSize: '1.5rem', fontWeight: '800', color: '#4f46e5' }}>
                EVENT MASTER
            </Link>


            <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                

                {role === 'OWNER_EVENT' && (
                    <>
                        <Link to="/owner-dashboard" style={{ textDecoration: 'none', color: '#4b5563', fontWeight: '500' }}>Panou Organizator</Link>
                        <Link to="/create-event" style={{ textDecoration: 'none', color: '#4b5563', fontWeight: '500' }}>+ Eveniment</Link>
                        <Link to="/create-package" style={{ textDecoration: 'none', color: '#4b5563', fontWeight: '500' }}>+ Pachet</Link>
                        <Link to="/scan" style={{ textDecoration: 'none', color: '#4b5563', fontWeight: '500' }}>Scanare</Link>
                    </>
                )}


                {role === 'ADMIN' && (
                    <Link to="/admin-dashboard" style={{ textDecoration: 'none', color: '#4b5563', fontWeight: '500' }}>Panou Admin</Link>
                )}



                {role === 'CLIENT' && (
                    <Link to="/profile" style={{ textDecoration: 'none', color: '#4b5563', fontWeight: '500' }}>Biletele Mele</Link>
                )}



                {token ? (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{ textAlign: 'right', fontSize: '0.8rem', color: '#4f46e5' }}>
                            <div style={{ fontWeight: 'bold' }}>{role}</div>
                            <div>{email}</div>
                        </div>
                        <button onClick={handleLogout} style={{ padding: '6px 12px', background: '#ffe4e6', color: '#e11d48', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' }}>
                            Logout
                        </button>
                    </div>
                ) : (
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <Link to="/login" className="btn btn-secondary btn-sm">Login</Link>
                        <Link to="/register" className="btn btn-primary btn-sm">Sign Up</Link>
                    </div>
                )}
            </div>
        </nav>
    );
};

export default Navbar;