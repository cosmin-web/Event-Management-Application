import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';
import { getLink } from '../utils/hateoas';

const EditPackage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    
    const [formData, setFormData] = useState<any>({});
    const [loading, setLoading] = useState(true);

    const [selfLink, setSelfLink] = useState<string | null>(null);

    const hateoasLinks = {
        parent: { href: '/owner-dashboard', rel: 'parent', label: '🛡️ Panou Organizator' },
        self: { href: `/edit-package/${id}`, rel: 'self', label: `Editare Pachet #${id}` }
    };

    useEffect(() => {
        api.get(`/event-manager/event-packets/${id}`)
            .then(res => {
                const data = res.data.data || res.data;
                setFormData(data);
                
                const link = getLink(res.data, 'self') || getLink(data, 'self');
                setSelfLink(link);
            })
            .catch(() => navigate('/owner-dashboard'))
            .finally(() => setLoading(false));
    }, [id, navigate]);

    const handleSave = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            if (selfLink) {
                let urlToUse = selfLink;
                try {
                    const urlObj = new URL(selfLink);
                    urlToUse = urlObj.pathname + urlObj.search;
                } catch(err) {  }
                
                await api.put(urlToUse, formData);
            } else {
                await api.put(`/event-manager/event-packets/${id}`, formData);
            }

            navigate('/owner-dashboard');
        } catch (e) {
            alert("Eroare la salvare.");
        }
    };

    if (loading) return <div className="container" style={{ textAlign: 'center', marginTop: 50 }}>Se incarca...</div>;

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

            <div className="card narrow" style={{ borderTop: '5px solid #8b5cf6' }}>
                <h2 style={{ marginBottom: '20px', color: '#1f2937' }}>✏️ Editeaza Pachet</h2>
                <form onSubmit={handleSave}>
                    <div className="form-group">
                        <label>Nume Pachet</label>
                        <input value={formData.nume || ''} onChange={e => setFormData({ ...formData, nume: e.target.value })} />
                    </div>
                    <div className="form-group">
                        <label>Locatie Generala</label>
                        <input value={formData.locatie || ''} onChange={e => setFormData({ ...formData, locatie: e.target.value })} />
                    </div>
                    <div className="form-group">
                        <label>Descriere</label>
                        <textarea rows={3} value={formData.descriere || ''} onChange={e => setFormData({ ...formData, descriere: e.target.value })} style={{ fontFamily: 'inherit' }} />
                    </div>
                    <div className="form-group">
                        <label>Locuri Totale</label>
                        <input type="number" value={formData.numarLocuri || 0} onChange={e => setFormData({ ...formData, numarLocuri: parseInt(e.target.value) })} />
                    </div>
                    <div style={{ display: 'flex', gap: '15px', marginTop: '30px' }}>
                        <button type="submit" className="btn btn-primary w-100" style={{ background: '#8b5cf6', border: 'none' }}>Salveaza</button>
                        <button type="button" onClick={() => navigate('/owner-dashboard')} className="btn btn-secondary w-100">Anuleaza</button>
                    </div>
                </form>
            </div>
        </div>
    );
};
export default EditPackage;