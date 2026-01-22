import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../utils/api';

interface Client {
    id?: number;
    nume: string;
    prenume: string;
    email: string;
    telefon?: string;
}

const PackageClientsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    
    const [clients, setClients] = useState<Client[]>([]);
    
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true); 
    const [loadingMore, setLoadingMore] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const PAGE_SIZE = 12; 

    useEffect(() => {
        setClients([]);
        setPage(0);
        loadClients(0);
    }, [id]);

    const loadClients = async (pageToLoad: number) => {
        if (pageToLoad === 0) {
            setLoading(true);
            setError(null);
        } else {
            setLoadingMore(true);
        }

        try {
            const res = await api.get(`/client-service/clients/public/by-package/${id}?page=${pageToLoad}&size=${PAGE_SIZE}&sort=nume,asc`);
            const data = res.data;
            
            const raw = data._embedded?.clientDTOList || 
                        data._embedded?.clientList || 
                        data.content || 
                        (Array.isArray(data) ? data : []);
            
            const normalizedClients = raw.map((i: any) => i.data || i);
            
            if (pageToLoad === 0) {
                setClients(normalizedClients);
            } else {
                setClients(prev => [...prev, ...normalizedClients]);
            }

            if (data.page) {
                setTotalPages(data.page.totalPages);
            } else {
                setTotalPages(normalizedClients.length < PAGE_SIZE ? pageToLoad + 1 : 999);
            }
            
            setPage(pageToLoad);

        } catch (err) {
            console.error(err);
            if (pageToLoad === 0) setError("Nu s-au putut incarca datele clientilor.");
        } finally {
            setLoading(false);
            setLoadingMore(false);
        }
    };

    const getInitials = (c: Client) => {
        const first = c.nume ? c.nume[0] : '';
        const second = c.prenume ? c.prenume[0] : '';
        return (first + second).toUpperCase() || 'U';
    };

    return (
        <div className="container" style={{ maxWidth: '1000px', paddingBottom: '50px' }}>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px', borderBottom: '1px solid #eee', paddingBottom: '15px', marginTop: '30px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                    <button onClick={() => navigate(-1)} className="btn btn-secondary btn-sm">
                        &larr; Inapoi
                    </button>
                    <div>
                        <h2 style={{ margin: 0, color: '#1f2937' }}>Clienti Pachet</h2>
                        <span className="text-muted" style={{ fontSize: '0.9rem' }}>
                            ID Pachet: <span style={{fontFamily:'monospace'}}>#{id}</span> • Total afisati: <strong>{clients.length}</strong>
                        </span>
                    </div>
                </div>
                <button onClick={() => loadClients(0)} className="btn btn-primary btn-sm" disabled={loading || loadingMore}>
                    {loading ? '...' : '🔄 Reimprospateaza'}
                </button>
            </div>

            {error && (
                <div className="alert alert-danger" style={{ background: '#fef2f2', color: '#991b1b', padding: '15px', borderRadius: '8px', border: '1px solid #fecaca', marginBottom: '20px' }}>
                    ⚠️ {error}
                </div>
            )}

            <div className="card" style={{ minHeight: '200px', border: 'none', boxShadow: 'none' }}>
                {loading ? (
                    <div style={{ textAlign: 'center', padding: '60px', color: '#666' }}>
                        <div className="spinner-border text-primary" role="status"></div>
                        <p style={{marginTop:'15px'}}>Se cauta clientii...</p>
                    </div>
                ) : clients.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '60px', background: '#f9fafb', borderRadius: '12px', border: '1px dashed #d1d5db', color: '#6b7280' }}>
                        <p style={{fontSize: '1.2rem', margin: 0}}>Nu exista clienti asociati acestui pachet momentan.</p>
                    </div>
                ) : (
                    <>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
                            {clients.map((c, idx) => (
                                <div key={idx} style={{ 
                                    padding: '20px', 
                                    border: '1px solid #e5e7eb', 
                                    borderRadius: '12px', 
                                    background: 'white', 
                                    display: 'flex', 
                                    alignItems: 'center',
                                    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)',
                                    transition: 'transform 0.2s'
                                }}>
                                    <div style={{ 
                                        width: '56px', 
                                        height: '56px', 
                                        borderRadius: '50%', 
                                        background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)', 
                                        color: 'white', 
                                        display: 'flex', 
                                        alignItems: 'center', 
                                        justifyContent: 'center', 
                                        fontWeight: 'bold', 
                                        fontSize: '1.3rem',
                                        marginRight: '15px',
                                        flexShrink: 0,
                                        boxShadow: '0 2px 4px rgba(79, 70, 229, 0.3)'
                                    }}>
                                        {getInitials(c)}
                                    </div>
                                    
                                    <div style={{ overflow: 'hidden', flex: 1 }}>
                                        <div style={{ fontWeight: 'bold', color: '#111827', fontSize: '1.1rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                            {c.nume} {c.prenume}
                                        </div>
                                        <div style={{ fontSize: '0.85rem', color: '#6b7280', marginTop: '2px', display: 'flex', alignItems: 'center', gap: '5px' }}>
                                            ✉️ <a href={`mailto:${c.email}`} style={{ color: '#6b7280', textDecoration: 'none' }} title={c.email}>{c.email}</a>
                                        </div>
                                        {c.telefon && (
                                            <div style={{ fontSize: '0.8rem', color: '#9ca3af', marginTop: '2px' }}>
                                                📞 {c.telefon}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>

                        {page < totalPages - 1 && (
                            <div style={{ textAlign: 'center', marginTop: '30px' }}>
                                <button 
                                    onClick={() => loadClients(page + 1)} 
                                    className="btn btn-light btn-lg" 
                                    disabled={loadingMore}
                                    style={{ border: '1px solid #d1d5db', color: '#374151', minWidth: '200px' }}
                                >
                                    {loadingMore ? 'Se incarca...' : '👇 Incarca mai multi clienti'}
                                </button>
                            </div>
                        )}
                    </>
                )}
            </div>
            
            
        </div>
    );
};

export default PackageClientsPage;