import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';
import { getLink } from '../utils/hateoas';
import { jwtDecode } from 'jwt-decode';

const EditProfile = () => {
    const navigate = useNavigate();
    
    const [email, setEmail] = useState('');
    const [selfLink, setSelfLink] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    const [formData, setFormData] = useState({ 
        nume: '', 
        prenume: '', 
        isPublic: true 
    });

    const hateoasLinks = {
        parent: { href: '/profile', rel: 'parent', label: '👤 Profilul Meu' },
        self: { href: '/edit-profile', rel: 'self', label: 'Editare' }
    };

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decoded: any = jwtDecode(token);
                const userEmail = decoded.email || decoded.sub;
                setEmail(userEmail);
                loadProfile(userEmail);
            } catch (e) {
                navigate('/login');
            }
        } else {
            navigate('/login');
        }
    }, [navigate]);

    const loadProfile = async (userEmail: string) => {
        try {
            const res = await api.get(`/client-service/clients/email/${userEmail}`);
            const data = res.data.data || res.data;

            if (data) {
                const link = getLink(res.data, 'self') || getLink(data, 'self');
                setSelfLink(link);

                setFormData({
                    nume: data.nume || '',
                    prenume: data.prenume || '', 
                    isPublic: data.isPublic !== undefined ? data.isPublic : true
                });
            }
        } catch (err) {
            console.log("Profilul nu exista inca, se va crea unul nou la salvare.");
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
        setFormData({ ...formData, [e.target.name]: value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
        const payload = {
            ...formData,
            email: email
        };

        await api.post(`/client-service/clients`, payload);
        
        alert("Profil salvat cu succes!");
        navigate('/profile');

    } catch (error: any) {
        console.error(error);
        const msg = error.response?.data?.message || "Eroare la salvarea profilului.";
        alert(`${msg}`);
    }
};

    if (loading) return <div className="container" style={{ marginTop: 50, textAlign: 'center' }}>Se incarca datele tale...</div>;

    return (
        <div className="container" style={{ maxWidth: '600px', marginTop: '20px' }}>
            
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

            <div className="card narrow" style={{ borderTop: '5px solid #4f46e5' }}>
                <h2 style={{ marginBottom: '20px', color: '#1f2937' }}>⚙️ Editare Profil</h2>
                
                <div style={{ background: '#f3f4f6', padding: '10px', borderRadius: '6px', marginBottom: '20px', fontSize: '0.9rem', color: '#4b5563' }}>
                    Cont conectat: <strong>{email}</strong>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Nume</label>
                        <input 
                            name="nume" 
                            className="form-control"
                            value={formData.nume} 
                            onChange={handleChange} 
                            placeholder="Numele tau" 
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>Prenume</label>
                        <input 
                            name="prenume" 
                            className="form-control"
                            value={formData.prenume} 
                            onChange={handleChange} 
                            placeholder="Prenumele tau" 
                            required
                        />
                    </div>

                    <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '15px', background: '#fff', padding: '10px', border: '1px solid #e5e7eb', borderRadius: '6px' }}>
                        <input
                            type="checkbox"
                            name="isPublic"
                            id="isPublicCheck"
                            checked={formData.isPublic}
                            onChange={handleChange}
                            style={{ width: '20px', height: '20px', margin: 0, cursor: 'pointer' }}
                        />
                        <label htmlFor="isPublicCheck" style={{ margin: 0, fontWeight: 'normal', cursor: 'pointer', flex: 1 }}>
                            Profil Public <span style={{color: '#9ca3af', fontSize: '0.85rem'}}>(vizibil organizatorilor la evenimente)</span>
                        </label>
                    </div>

                    <div style={{ display: 'flex', gap: '10px', marginTop: '25px' }}>
                        <button type="submit" className="btn btn-primary w-100">💾 Salveaza Modificarile</button>
                        <button type="button" className="btn btn-secondary w-100" onClick={() => navigate('/profile')}>Anuleaza</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EditProfile;