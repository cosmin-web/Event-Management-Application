import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../utils/api';
import { jwtDecode } from 'jwt-decode';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
        const res = await api.post('/auth/login', { email, password });
        
        const token = res.data.token || (typeof res.data === 'string' ? res.data : null);

        if (token) {
            localStorage.setItem('token', token);
    
            window.location.href = '/';

        } else {
            setError('Eroare: Serverul nu a returnat un token valid.');
        }

    } catch (err: any) {

    } finally {
        setLoading(false);
    }
};

    return (
        <div className="container" style={{ marginTop: '80px', maxWidth: '400px' }}>
            <div className="card narrow" style={{ borderTop: '5px solid #4f46e5' }}>
                <h2 style={{ textAlign: 'center', marginBottom: '30px', color: '#1f2937' }}>Autentificare</h2>
                
                {error && (
                    <div className="alert alert-danger" style={{ padding: '10px', marginBottom: '20px', fontSize: '0.9rem', textAlign: 'center' }}>
                        ⚠️ {error}
                    </div>
                )}

                <form onSubmit={handleLogin}>
                    <div className="form-group">
                        <label>Email</label>
                        <input 
                            type="email" 
                            className="form-control"
                            value={email} 
                            onChange={e => setEmail(e.target.value)} 
                            required 
                            placeholder="ex: user@test.com"
                        />
                    </div>
                    <div className="form-group">
                        <label>Parola</label>
                        <input 
                            type="password" 
                            className="form-control"
                            value={password} 
                            onChange={e => setPassword(e.target.value)} 
                            required 
                            placeholder="••••••"
                        />
                    </div>
                    <button 
                        type="submit" 
                        className="btn btn-primary w-100" 
                        disabled={loading}
                        style={{ marginTop: '10px' }}
                    >
                        {loading ? (
                            <span><span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Se autentifica...</span>
                        ) : 'Intra în cont'}
                    </button>
                </form>
                <p style={{ marginTop: '20px', textAlign: 'center', fontSize: '0.9rem', color: '#6b7280' }}>
                    Nu ai cont? <Link to="/register" style={{ color: '#4f46e5', fontWeight: 'bold', textDecoration: 'none' }}>Inregistreaza-te</Link>
                </p>
            </div>
        </div>
    );
};

export default Login;