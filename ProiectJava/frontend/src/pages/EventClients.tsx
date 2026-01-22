import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';

const EventClients = () => {
    const { eventId } = useParams();
    const navigate = useNavigate();
    const [clients, setClients] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    const hateoasLinks = {
        parent: { href: '/owner-dashboard', rel: 'parent', label: '🛡️ Panou Organizator' },
        self: { href: `/event-clients/${eventId}`, rel: 'self', label: 'Participanti' }
    };

    useEffect(() => {
        api.get(`/client-service/clients/public/by-event/${eventId}?size=100`)
            .then(res => {
                let raw = [];
                if (res.data._embedded) {
                    raw = res.data._embedded.clientList || res.data._embedded.clientDTOList || [];
                } else if (res.data.content) {
                    raw = res.data.content;
                } else {
                    raw = Array.isArray(res.data) ? res.data : (res.data.data || []);
                }
                setClients(raw.map((i: any) => i.data || i));
            })
            .catch((err) => {
                console.error("Eroare incarcare participanti", err);
            })
            .finally(() => setLoading(false));
    }, [eventId]);

    return (
        <div className="container">
             {/*NAVIGARE HATEOAS*/}
             <nav aria-label="breadcrumb" style={{ marginBottom: '20px', marginTop: '10px' }}>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center', fontSize: '0.9rem', color: '#6b7280' }}>
                    <Link to={hateoasLinks.parent.href} style={{ textDecoration: 'none', color: '#4b5563', display: 'flex', alignItems: 'center', gap: '5px' }}>
                        {hateoasLinks.parent.label}
                    </Link>
                    <span>/</span>
                    <span style={{ fontWeight: '600', color: '#111827' }}>{hateoasLinks.self.label}</span>
                </div>
            </nav>

            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '20px' }}>
                <h2 style={{ margin: 0, color: '#1f2937' }}>👥 Participanti (Publici)</h2>
            </div>

            <div className="card">
                {loading && <div style={{textAlign: 'center', padding: '20px'}}>Se incarca...</div>}

                {!loading && clients.length === 0 && (
                    <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
                        <p>Nu exista participanti cu profil public la acest eveniment.</p>
                        <small>Nota: Participantii care nu au bifat "Profil Public" nu apar aici.</small>
                    </div>
                )}

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '15px' }}>
                    {clients.map((c, idx) => (
                        <div key={idx} style={{ padding: '15px', border: '1px solid #e5e7eb', borderRadius: '10px', background: 'white', display: 'flex', alignItems: 'center', boxShadow: '0 1px 2px rgba(0,0,0,0.05)' }}>
                            <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: '#e0e7ff', color: '#4f46e5', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', marginRight: '15px', fontSize: '1.1rem' }}>
                                {(c.nume?.[0] || 'U').toUpperCase()}
                            </div>
                            <div style={{ overflow: 'hidden' }}>
                                <div style={{ fontWeight: 'bold', color: '#1f2937', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                    {c.nume || 'Utilizator'} {c.prenume || ''}
                                </div>
                                <div style={{ fontSize: '0.85rem', color: '#6b7280', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                    {c.email}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};
export default EventClients;