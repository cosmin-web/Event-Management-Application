import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../utils/api';

interface Ticket {
    id?: number;
    cod: string;
    status?: string;
}

interface HateoasLink {
    href: string;
    rel: string;
    method: string;
    label: string;
}

const CopyableCode = ({ code }: { code: string }) => {
    const [copied, setCopied] = useState(false);

    const handleCopy = () => {
        navigator.clipboard.writeText(code);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <code style={{ background: '#eff6ff', color: '#1d4ed8', padding: '4px 8px', borderRadius: '4px', fontSize: '0.9rem', border: '1px solid #dbeafe' }}>
                {code}
            </code>
            <button 
                onClick={handleCopy} 
                title="Copiază codul"
                style={{ 
                    background: 'none', border: 'none', cursor: 'pointer', 
                    fontSize: '1rem', padding: '0', opacity: 0.7 
                }}>
                {copied ? '✅' : '📋'}
            </button>
        </div>
    );
};

const TicketStats = () => {
    const { type, id } = useParams();
    
    const [tickets, setTickets] = useState<Ticket[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const hateoasLinks: Record<string, HateoasLink> = {
        parent: { href: '/owner-dashboard', rel: 'parent', method: 'GET', label: '🏠 Dashboard' },
        self: { href: `/ticket-stats/${type}/${id}`, rel: 'self', method: 'GET', label: `Statistici ${type === 'event' ? 'Eveniment' : 'Pachet'}` }
    };

    useEffect(() => {
        fetchTickets();
    }, [id, type]);

    const fetchTickets = async () => {
        setLoading(true);
        setError(null);
        
        const endpoint = type === 'event'
            ? `/event-manager/events/${id}/tickets`
            : `/event-manager/event-packets/${id}/tickets`;

        try {
            const res = await api.get(endpoint);
            
            const rawList = res.data._embedded?.ticketList || res.data._embedded?.ticketDTOList || res.data.content || res.data || [];
            
            const cleanList = Array.isArray(rawList) 
                ? rawList.map((item: any) => item.data || item) 
                : [];
                
            setTickets(cleanList);
        } catch (err) {
            console.error(err);
            setError("Nu s-au putut incarca biletele. Verifica conexiunea.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container" style={{ maxWidth: '1000px', marginTop: '30px' }}>
            
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

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <div>
                    <h2 style={{ margin: 0 }}>Statistici Vanzari</h2>
                    <span className="text-muted" style={{ fontSize: '0.85rem' }}>
                        ID Referinta: #{id}
                    </span>
                </div>
                <button onClick={fetchTickets} className="btn btn-primary btn-sm" disabled={loading}>
                    {loading ? '...' : '🔄 Reincarca'}
                </button>
            </div>

            {error && (
                <div className="alert alert-danger" style={{ background: '#fef2f2', color: '#991b1b', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>
                    ⚠️ {error}
                </div>
            )}

            <div className="card" style={{ padding: '0', overflow: 'hidden', border: '1px solid #e5e7eb' }}>
                <div style={{ padding: '20px', background: '#f8fafc', borderBottom: '1px solid #e5e7eb', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#334155' }}>🎫 Lista Bilete Vandute</h3>
                    <span className="badge badge-blue" style={{ fontSize: '0.9rem', padding: '5px 10px' }}>
                        Total: <strong>{tickets.length}</strong>
                    </span>
                </div>

                {loading ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>Se incarca datele...</div>
                ) : tickets.length === 0 ? (
                    <div style={{ padding: '50px', textAlign: 'center', color: '#6b7280' }}>
                        <div style={{ fontSize: '2rem', marginBottom: '10px' }}>🏷️</div>
                        <p style={{ fontSize: '1.1rem', margin: 0 }}>Nu s-a vandut niciun bilet momentan.</p>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                            <thead>
                                <tr style={{ background: '#f1f5f9', color: '#475569', textAlign: 'left', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                                    <th style={{ padding: '15px', borderBottom: '2px solid #e2e8f0', width: '50px' }}>#</th>
                                    <th style={{ padding: '15px', borderBottom: '2px solid #e2e8f0' }}>Cod Unic (UUID)</th>
                                    <th style={{ padding: '15px', borderBottom: '2px solid #e2e8f0' }}>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {tickets.map((t, idx) => (
                                    <tr key={t.id || t.cod || idx} style={{ borderBottom: '1px solid #f1f5f9', background: 'white' }}>
                                        <td style={{ padding: '15px', color: '#94a3b8', fontWeight: 'bold' }}>{idx + 1}</td>
                                        <td style={{ padding: '15px' }}>
                                            <CopyableCode code={t.cod} />
                                        </td>
                                        <td style={{ padding: '15px' }}>
                                            <span style={{ 
                                                background: '#dcfce7', 
                                                color: '#166534', 
                                                padding: '4px 10px', 
                                                borderRadius: '20px', 
                                                fontSize: '0.75rem', 
                                                fontWeight: 'bold',
                                                border: '1px solid #bbf7d0'
                                            }}>
                                                CONFIRMAT
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default TicketStats;