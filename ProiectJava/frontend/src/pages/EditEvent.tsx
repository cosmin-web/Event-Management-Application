import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';
import { getLink } from '../utils/hateoas';

const EditEvent = () => {
    const { eventId } = useParams();
    const navigate = useNavigate();
    
    const [formData, setFormData] = useState<any>({});
    const [loading, setLoading] = useState(true);
    const [ticketsSold, setTicketsSold] = useState(0);
    
    const [selfLink, setSelfLink] = useState<string | null>(null);

    const hateoasLinks = {
        parent: { href: '/owner-dashboard', rel: 'parent', label: '🛡️ Panou Organizator' },
        self: { href: `/edit-event/${eventId}`, rel: 'self', label: `Editare #${eventId}` }
    };

    useEffect(() => {
        api.get(`/event-manager/events/${eventId}`)
            .then(res => {
                const data = res.data.data || res.data;
                setFormData(data);
                
                const link = getLink(res.data, 'self') || getLink(data, 'self');
                setSelfLink(link);

                const sold = data.ticketsSold !== undefined 
                    ? data.ticketsSold 
                    : (data.numarLocuri - (data.availableTickets || 0));
                setTicketsSold(sold);
            })
            .catch(() => navigate('/owner-dashboard'))
            .finally(() => setLoading(false));
    }, [eventId, navigate]);

    const handleSave = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            let urlToUse = `/event-manager/events/${eventId}`;

            if (selfLink) {
                if (selfLink.startsWith('http')) {
                    try {
                        const urlObj = new URL(selfLink);
                        urlToUse = urlObj.pathname;
                    } catch(err) { }
                } else {
                    urlToUse = selfLink;
                }
            }

            if (urlToUse.startsWith('/api/')) {
                urlToUse = urlToUse.replace('/api', '');
            }

            await api.put(urlToUse, formData);

            alert("Actualizat cu succes!");
            navigate('/owner-dashboard');
        } catch (e: any) { 
            console.error(e);
            let msg = "Eroare la salvare.";
            if (e.response?.data?.message) {
                msg = e.response.data.message;
            } else if (typeof e.response?.data === 'string') {
                msg = e.response.data;
            }
            alert(msg); 
        }
    };

    if (loading) return <div className="container" style={{ marginTop: 50, textAlign: 'center' }}>Se incarca...</div>;
    
    const disabledStyle = { backgroundColor: '#f3f4f6', color: '#9ca3af', cursor: 'not-allowed', borderColor: '#e5e7eb' };

    return (
        <div className="container">
            <nav aria-label="breadcrumb" style={{ marginBottom: '20px', marginTop: '10px' }}>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center', fontSize: '0.9rem', color: '#6b7280' }}>
                    <Link to={hateoasLinks.parent.href} style={{ textDecoration: 'none', color: '#4b5563', display: 'flex', alignItems: 'center', gap: '5px' }}>
                        {hateoasLinks.parent.label}
                    </Link>
                    <span>/</span>
                    <span style={{ fontWeight: '600', color: '#111827' }}>{hateoasLinks.self.label}</span>
                </div>
            </nav>

            <div className="card narrow" style={{ borderTop: '5px solid #4f46e5' }}>
                <h2 style={{ marginBottom: '20px', color: '#1f2937' }}>✏️ Editeaza Eveniment</h2>
                
                {ticketsSold > 0 && (
                    <div style={{ background: '#fff7ed', borderLeft: '4px solid #f97316', padding: '10px', marginBottom: '20px', fontSize: '0.9rem', color: '#9a3412' }}>
                        ⚠️ Deoarece s-au vandut <strong>{ticketsSold}</strong> bilete, poti modifica doar descrierea.
                    </div>
                )}

                <form onSubmit={handleSave}>
                    <div className="form-group">
                        <label>Nume</label>
                        <input value={formData.nume || ''} onChange={e => setFormData({ ...formData, nume: e.target.value })} disabled={ticketsSold > 0} style={ticketsSold > 0 ? disabledStyle : {}} />
                    </div>
                    <div className="form-group">
                        <label>Locatie</label>
                        <input value={formData.locatie || ''} onChange={e => setFormData({ ...formData, locatie: e.target.value })} disabled={ticketsSold > 0} style={ticketsSold > 0 ? disabledStyle : {}} />
                    </div>
                    <div className="form-group">
                        <label>Descriere</label>
                        <textarea rows={3} value={formData.descriere || ''} onChange={e => setFormData({ ...formData, descriere: e.target.value })} style={{ fontFamily: 'inherit' }} />
                    </div>
                    <div className="form-group">
                        <label>Locuri Totale</label>
                        <input type="number" value={formData.numarLocuri || 0} onChange={e => setFormData({ ...formData, numarLocuri: parseInt(e.target.value) })} disabled={ticketsSold > 0} style={ticketsSold > 0 ? disabledStyle : {}} />
                    </div>
                    <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                        <button type="submit" className="btn btn-primary w-100">Salveaza</button>
                        <button type="button" onClick={() => navigate('/owner-dashboard')} className="btn btn-secondary w-100">Anuleaza</button>
                    </div>
                </form>
            </div>
        </div>
    );
};
export default EditEvent;