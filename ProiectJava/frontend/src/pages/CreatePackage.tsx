import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';
import { getIdFromSelf } from '../utils/hateoas';

const CreatePackage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ nume: '', locatie: '', descriere: '', numarLocuri: 0 });

    const hateoasLinks = {
        parent: { href: '/owner-dashboard', rel: 'parent', label: '🛡️ Panou Organizator' },
        self: { href: '/create-package', rel: 'self', label: 'Creeaza Pachet' }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const res = await api.post('/event-manager/event-packets', formData);
            
            const idFromLink = getIdFromSelf(res.data);

            if (idFromLink) {
                navigate(`/manage-package/${idFromLink}`);
            } else {
                const fallbackId = res.data.data?.id || res.data.id;
                navigate(`/manage-package/${fallbackId}`);
            }
        } catch (error: any) { 
            console.error(error);
            let msg = "Eroare la creare pachet.";
            if (error.response && error.response.data && error.response.data.message) {
                msg = error.response.data.message;
            }
            alert(msg); 
        }
    };

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
                <h2 style={{ marginBottom: '20px', color: '#1f2937' }}>📦 Creeaza Pachet Nou</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Nume Pachet</label>
                        <input onChange={e => setFormData({ ...formData, nume: e.target.value })} required />
                    </div>
                    <div className="form-group">
                        <label>Locatie Generala</label>
                        <input onChange={e => setFormData({ ...formData, locatie: e.target.value })} required />
                    </div>
                    <div className="form-group">
                        <label>Descriere</label>
                        <textarea rows={3} onChange={e => setFormData({ ...formData, descriere: e.target.value })} required style={{ fontFamily: 'inherit' }} />
                    </div>
                    <div className="form-group">
                        <label>Numar Pachete Disponibile</label>
                        <input type="number" onChange={e => setFormData({ ...formData, numarLocuri: parseInt(e.target.value) })} required min="1" />
                    </div>
                    <button type="submit" className="btn btn-primary w-100" style={{ marginTop: '10px', background: '#8b5cf6', border: 'none' }}>Continua &rarr;</button>
                </form>
            </div>
        </div>
    );
};
export default CreatePackage;