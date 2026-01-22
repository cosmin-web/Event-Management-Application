import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';
import { getLink } from '../utils/hateoas';

const CreateEvent = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ nume: '', locatie: '', descriere: '', numarLocuri: 0 });

    const hateoasLinks = {
        parent: { href: '/owner-dashboard', rel: 'parent', method: 'GET', label: '🛡️ Panou Organizator' },
        self: { href: '/create-event', rel: 'self', method: 'POST', label: 'Adaugare Eveniment' }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const response = await api.post('/event-manager/events', formData);
            
            const selfLink = getLink(response.data, 'self');
            if (selfLink) {
                console.log(`Resursa creata cu succes la: ${selfLink}`);
            }

            alert("Eveniment creat cu succes!");
            navigate('/owner-dashboard');
        } catch (error: any) { 
            console.error(error);
            let msg = "Eroare la creare eveniment.";
            if (error.response && error.response.data && error.response.data.message) {
                msg = error.response.data.message;
            }
            alert(msg); 
        }
    };

    return (
        <div className="container">
            {/* NAVIGARE HATEOAS*/}
            <nav aria-label="breadcrumb" style={{ marginBottom: '20px', marginTop: '10px' }}>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center', fontSize: '0.9rem', color: '#6b7280' }}>
                    <Link 
                        to={hateoasLinks.parent.href} 
                        style={{ textDecoration: 'none', color: '#4b5563', display: 'flex', alignItems: 'center', gap: '5px' }}
                        title={`Rel: ${hateoasLinks.parent.rel}`}
                    >
                        {hateoasLinks.parent.label}
                    </Link>
                    <span>/</span>
                    <span style={{ fontWeight: '600', color: '#111827', cursor: 'default' }}>
                        {hateoasLinks.self.label}
                    </span>
                </div>
            </nav>

            <div className="card narrow" style={{ borderTop: '5px solid #4f46e5' }}>
                <h2 style={{ marginBottom: '20px', color: '#1f2937' }}>✨ Adauga Eveniment Nou</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Nume Eveniment</label>
                        <input onChange={e => setFormData({ ...formData, nume: e.target.value })} required placeholder="ex: Concert Rock" />
                    </div>
                    <div className="form-group">
                        <label>Locatie</label>
                        <input onChange={e => setFormData({ ...formData, locatie: e.target.value })} required placeholder="ex: Sala Palatului" />
                    </div>
                    <div className="form-group">
                        <label>Descriere</label>
                        <textarea rows={3} onChange={e => setFormData({ ...formData, descriere: e.target.value })} required placeholder="Detalii despre eveniment..." style={{ fontFamily: 'inherit' }} />
                    </div>
                    <div className="form-group">
                        <label>Locuri Totale</label>
                        <input type="number" onChange={e => setFormData({ ...formData, numarLocuri: parseInt(e.target.value) })} required min="1" />
                    </div>
                    <button type="submit" className="btn btn-success w-100" style={{ marginTop: '10px', background: '#10b981', border: 'none' }}>Creeaza Eveniment</button>
                </form>
            </div>
        </div>
    );
};
export default CreateEvent;