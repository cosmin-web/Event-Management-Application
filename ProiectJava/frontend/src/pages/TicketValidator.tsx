import React, { useState, useEffect } from 'react';
import api from '../utils/api';
import { Link } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

interface HateoasLink {
    href: string;
    rel: string;
    method: string;
    label: string;
}

const TicketValidator = () => {
    const [code, setCode] = useState('');
    const [status, setStatus] = useState<'idle' | 'success' | 'error'>('idle');
    const [message, setMessage] = useState('');
    const [ticketData, setTicketData] = useState<any>(null);
    const [parentPath, setParentPath] = useState('/');

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decoded: any = jwtDecode(token);
                const role = decoded.rol || decoded.role;
                if (role === 'ADMIN') {
                    setParentPath('/admin-dashboard');
                } else {
                    setParentPath('/owner-dashboard');
                }
            } catch (e) {
                setParentPath('/');
            }
        }
    }, []);

    const hateoasLinks: Record<string, HateoasLink> = {
        parent: { href: parentPath, rel: 'parent', method: 'GET', label: '🏠 Panou Control' },
        self: { href: '/scan', rel: 'self', method: 'GET', label: 'Check-in Bilete' }
    };

    const validate = async (e?: React.FormEvent) => {
        if (e) e.preventDefault();
        if (!code.trim()) return;

        setStatus('idle');
        setTicketData(null);

        try {
            const res = await api.get(`/event-manager/tickets/${code}`);
            
            setStatus('success');
            setMessage("Bilet VALID");
            setTicketData(res.data.data || res.data);
            
        } catch (err: any) {
            setStatus('error');
            setTicketData(null);
            if (err.response && err.response.status === 404) {
                setMessage("Bilet INVALID sau inexistent");
            } else if (err.response && err.response.status === 403) {
                setMessage("Nu ai permisiunea sa verifici acest bilet (alt organizator)");
            } else {
                setMessage("Eroare la verificare. Incearca din nou.");
            }
        }
    };

    return (
        <div className="container" style={{ marginTop: '30px', maxWidth: '600px' }}>
            
            {/*HATEOAS NAVIGARE*/}
            <nav aria-label="breadcrumb" style={{ marginBottom: '20px' }}>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center', fontSize: '0.9rem', color: '#6b7280' }}>
                    <Link 
                        to={hateoasLinks.parent.href} 
                        style={{ textDecoration: 'none', color: '#4b5563', display: 'flex', alignItems: 'center', gap: '5px' }}
                        title={`Rel: ${hateoasLinks.parent.rel} (${hateoasLinks.parent.method})`}
                    >
                        {hateoasLinks.parent.label}
                    </Link>
                    <span>/</span>
                    <span 
                        style={{ fontWeight: '600', color: '#111827', cursor: 'default' }}
                        title={`Rel: ${hateoasLinks.self.rel} (${hateoasLinks.self.method})`}
                    >
                        {hateoasLinks.self.label}
                    </span>
                </div>
            </nav>

            <div className="card text-center" style={{ padding: '40px', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' }}>
                <div style={{ fontSize: '3rem', marginBottom: '10px' }}>📱</div>
                <h2 style={{ marginBottom: '5px', color: '#1f2937' }}>Check-in Eveniment</h2>
                <p className="text-muted" style={{ marginBottom: '30px' }}>Scaneaza sau introdu codul UUID al biletului.</p>

                <form onSubmit={validate}>
                    <div className="form-group">
                        <label style={{ textAlign: 'left', display: 'block', fontWeight: 'bold', marginBottom: '5px', fontSize: '0.9rem' }}>Cod Bilet</label>
                        <input
                            type="text"
                            className="form-control"
                            placeholder="ex: 123e4567-e89b..."
                            value={code}
                            onChange={(e) => setCode(e.target.value)}
                            style={{ textAlign: 'center', fontFamily: 'monospace', fontSize: '1.1rem', padding: '12px' }}
                        />
                    </div>

                    <button 
                        type="submit" 
                        className="btn btn-primary w-100" 
                        style={{ marginTop: '20px', padding: '12px', fontSize: '1rem', background: '#4f46e5', border: 'none' }}
                    >
                        🔍 Verifica Bilet
                    </button>
                </form>

                {status !== 'idle' && (
                    <div style={{ 
                        marginTop: '30px', 
                        padding: '20px', 
                        borderRadius: '8px', 
                        background: status === 'success' ? '#ecfdf5' : '#fef2f2',
                        border: status === 'success' ? '1px solid #10b981' : '1px solid #ef4444',
                        color: status === 'success' ? '#065f46' : '#991b1b'
                    }}>
                        <h3 style={{ margin: 0, fontSize: '1.3rem' }}>{message}</h3>
                        
                        {status === 'success' && ticketData && (
                            <div style={{ marginTop: '15px', textAlign: 'left', background: 'white', padding: '15px', borderRadius: '6px' }}>
                                <p style={{ margin: '5px 0' }}><strong>Eveniment:</strong> {ticketData.event ? ticketData.event.nume : (ticketData.package ? ticketData.package.nume : 'N/A')}</p>
                                <p style={{ margin: '5px 0' }}><strong>Locatie:</strong> {ticketData.event ? ticketData.event.locatie : ticketData.package?.locatie}</p>
                                <p style={{ margin: '5px 0', fontSize: '0.9rem', color: '#666' }}>Cod: {ticketData.cod}</p>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default TicketValidator;