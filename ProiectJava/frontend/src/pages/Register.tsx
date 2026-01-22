import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';

interface RegisterForm {
    email: string;
    parola: string;
    nume: string;
    prenume: string;
    rol: 'CLIENT' | 'OWNER_EVENT';
}

interface HateoasLink {
    href: string;
    rel: string;
    method: string;
    label: string;
}

const Register = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    
    const hateoasLinks: Record<string, HateoasLink> = {
        parent: { href: '/', rel: 'parent', method: 'GET', label: '🏠 Acasa' },
        self: { href: '/register', rel: 'self', method: 'GET', label: 'Inregistrare' },
        login: { href: '/login', rel: 'sibling', method: 'GET', label: 'Logare' } 
    };

    const [formData, setFormData] = useState<RegisterForm>({
        email: '',
        parola: '',
        nume: '',
        prenume: '',
        rol: 'CLIENT'
    });

    const selectRole = (role: 'CLIENT' | 'OWNER_EVENT') => {
        setFormData({ ...formData, rol: role });
        setStep(2);
        setError(null);
    };

    const handleRegister = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            await api.post('/auth/register', {
                email: formData.email,
                parola: formData.parola,
                rol: formData.rol
            });

            const loginRes = await api.post('/auth/login', {
                email: formData.email,
                password: formData.parola
            });

            const token = loginRes.data.token;
            localStorage.setItem('token', token);

            try {
                 await api.post('/client-service/clients', {
                    email: formData.email,
                    nume: formData.nume,
                    prenume: formData.prenume,
                    isPublic: true
                });
            } catch (profileErr) {
                console.warn("Cont creat, dar profilul a esuat sau nu e necesar:", profileErr);
            }

            window.location.href = '/';

        } catch (err: any) {
            console.error(err);
            if (err.response?.status === 409) {
                setError("Acest email este deja utilizat.");
            } else if (err.response?.status === 400) {
                setError("Date invalide. Verifica parola si emailul.");
            } else {
                setError("A aparut o eroare la comunicarea cu serverul.");
            }
            localStorage.removeItem('token');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container" style={{ maxWidth: '600px', marginTop: '40px', paddingBottom: '40px' }}>
            
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

            <div className="card" style={{ borderTop: `5px solid ${formData.rol === 'CLIENT' ? '#4f46e5' : '#f59e0b'}` }}>
                
                <h2 style={{ textAlign: 'center', marginBottom: '10px', color: '#111827' }}>
                    {step === 1 ? "Alege tipul contului" : "Finalizare Inregistrare"}
                </h2>
                <p style={{ textAlign: 'center', color: '#6b7280', marginBottom: '30px' }}>
                    {step === 1 ? "Cum doresti sa folosesti platforma?" : "Completeaza detaliile de mai jos."}
                </p>

                {error && (
                    <div className="alert alert-danger" style={{ background: '#fee2e2', color: '#991b1b', padding: '10px', borderRadius: '6px', marginBottom: '20px', fontSize: '0.9rem', textAlign: 'center' }}>
                        {error}
                    </div>
                )}

                {step === 1 && (
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                        <div 
                            onClick={() => selectRole('CLIENT')}
                            className="role-card"
                            style={{
                                border: '2px solid #e5e7eb', borderRadius: '12px', padding: '30px 20px',
                                cursor: 'pointer', textAlign: 'center', transition: 'all 0.2s ease',
                                background: formData.rol === 'CLIENT' ? '#eff6ff' : 'white',
                                borderColor: formData.rol === 'CLIENT' ? '#4f46e5' : '#e5e7eb',
                                transform: formData.rol === 'CLIENT' ? 'scale(1.02)' : 'scale(1)'
                            }}
                        >
                            <div style={{ fontSize: '3rem', marginBottom: '15px' }}>🎫</div>
                            <h3 style={{ fontSize: '1.2rem', margin: '0 0 5px 0' }}>Client</h3>
                            <p style={{ fontSize: '0.85rem', color: '#6b7280', margin: 0 }}>Vreau sa cumpar bilete la evenimente.</p>
                        </div>

                        <div 
                            onClick={() => selectRole('OWNER_EVENT')}
                            className="role-card"
                            style={{
                                border: '2px solid #e5e7eb', borderRadius: '12px', padding: '30px 20px',
                                cursor: 'pointer', textAlign: 'center', transition: 'all 0.2s ease',
                                background: formData.rol === 'OWNER_EVENT' ? '#fffbeb' : 'white',
                                borderColor: formData.rol === 'OWNER_EVENT' ? '#f59e0b' : '#e5e7eb',
                                transform: formData.rol === 'OWNER_EVENT' ? 'scale(1.02)' : 'scale(1)'
                            }}
                        >
                            <div style={{ fontSize: '3rem', marginBottom: '15px' }}>🎤</div>
                            <h3 style={{ fontSize: '1.2rem', margin: '0 0 5px 0' }}>Organizator</h3>
                            <p style={{ fontSize: '0.85rem', color: '#6b7280', margin: 0 }}>Vreau sa vand bilete si sa gestionez evenimente.</p>
                        </div>
                    </div>
                )}

                {step === 2 && (
                    <form onSubmit={handleRegister}>
                        <div style={{ marginBottom: '25px', textAlign: 'center', background: '#f9fafb', padding: '10px', borderRadius: '8px' }}>
                            <span style={{ fontSize: '0.85rem', color: '#6b7280' }}>Te inregistrezi ca: </span>
                            <strong style={{ color: formData.rol === 'CLIENT' ? '#4f46e5' : '#d97706' }}>
                                {formData.rol === 'CLIENT' ? 'CLIENT' : 'ORGANIZATOR'}
                            </strong>
                            <button 
                                type="button" 
                                onClick={() => setStep(1)} 
                                style={{ background: 'none', border: 'none', color: '#6b7280', textDecoration: 'underline', marginLeft: '15px', fontSize: '0.8rem', cursor: 'pointer' }}
                            >
                                Schimba
                            </button>
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                            <div className="form-group">
                                <label style={{ fontSize: '0.9rem', fontWeight: 600, color: '#374151' }}>Nume</label>
                                <input 
                                    className="form-control"
                                    onChange={e => setFormData({ ...formData, nume: e.target.value })} 
                                    required 
                                    placeholder="Popescu"
                                    style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #d1d5db', marginTop: '5px' }}
                                />
                            </div>
                            <div className="form-group">
                                <label style={{ fontSize: '0.9rem', fontWeight: 600, color: '#374151' }}>Prenume</label>
                                <input 
                                    className="form-control"
                                    onChange={e => setFormData({ ...formData, prenume: e.target.value })} 
                                    required 
                                    placeholder="Andrei"
                                    style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #d1d5db', marginTop: '5px' }}
                                />
                            </div>
                        </div>

                        <div className="form-group" style={{ marginTop: '15px' }}>
                            <label style={{ fontSize: '0.9rem', fontWeight: 600, color: '#374151' }}>Email</label>
                            <input 
                                type="email" 
                                className="form-control"
                                onChange={e => setFormData({ ...formData, email: e.target.value })} 
                                required 
                                placeholder="nume@email.com"
                                style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #d1d5db', marginTop: '5px' }}
                            />
                        </div>

                        <div className="form-group" style={{ marginTop: '15px' }}>
                            <label style={{ fontSize: '0.9rem', fontWeight: 600, color: '#374151' }}>Parola</label>
                            <input 
                                type="password" 
                                className="form-control"
                                onChange={e => setFormData({ ...formData, parola: e.target.value })} 
                                required 
                                minLength={6} 
                                placeholder="******"
                                style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #d1d5db', marginTop: '5px' }}
                            />
                            <small style={{ color: '#9ca3af', fontSize: '0.75rem' }}>Minim 6 caractere.</small>
                        </div>

                        <button 
                            type="submit" 
                            className="btn btn-primary w-100" 
                            style={{ marginTop: '25px', padding: '12px', fontSize: '1rem', background: loading ? '#9ca3af' : '#2563eb', border: 'none', color: 'white', borderRadius: '6px', cursor: loading ? 'not-allowed' : 'pointer' }} 
                            disabled={loading}
                        >
                            {loading ? 'Se creeaza contul...' : 'Inregistreaza-te'}
                        </button>
                    </form>
                )}

                <div style={{ marginTop: '30px', textAlign: 'center', fontSize: '0.9rem', borderTop: '1px solid #f3f4f6', paddingTop: '20px' }}>
                    Ai deja cont? <Link to="/login" style={{ color: '#2563eb', fontWeight: '600', textDecoration: 'none' }}>Logheaza-te aici</Link>
                </div>
            </div>
        </div>
    );
};

export default Register;