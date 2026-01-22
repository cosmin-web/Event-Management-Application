import React, { useEffect, useState } from 'react';
import api from '../utils/api';
import { useNavigate, Link } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

interface HateoasLink {
    href: string;
    rel: string;
    method: string;
    label: string;
}

const AdminDashboard = () => {
    const navigate = useNavigate();
    const [users, setUsers] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    const [collectionLinks, setCollectionLinks] = useState<any>(null);

    const [showModal, setShowModal] = useState(false);
    const [editMode, setEditMode] = useState(false);
    
    const [editingUserLink, setEditingUserLink] = useState<string | null>(null);

    const [formData, setFormData] = useState({
        email: '',
        parola: '',
        role: 'CLIENT'
    });

    const hateoasLinks: Record<string, HateoasLink> = {
        parent: { href: '/', rel: 'parent', method: 'GET', label: '🏠 Acasa' },
        self: { href: '/admin-dashboard', rel: 'self', method: 'GET', label: 'Panou Admin' }
    };

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/login');
            return;
        }
        try {
            const decoded: any = jwtDecode(token);
            if (decoded.role !== 'ADMIN' && decoded.rol !== 'ADMIN') {
                navigate('/');
                return;
            }
            loadUsers();
        } catch (e) {
            navigate('/login');
        }
    }, [navigate]);

    const loadUsers = async () => {
        try {
            const res = await api.get('/event-manager/users');
            const list = res.data._embedded?.userDTOList || res.data._embedded?.userList || res.data.content || [];
            setUsers(list);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    const openCreateModal = () => {
        setEditMode(false);
        setEditingUserLink(null);
        setFormData({ email: '', parola: '', role: 'CLIENT' });
        setShowModal(true);
    };

    const openEditModal = (user: any) => {
        setEditMode(true);
        const selfLink = user._links?.self?.href;
        setEditingUserLink(selfLink);

        setFormData({
            email: user.email,
            parola: '',
            role: user.role || user.rol || 'CLIENT'
        });
        setShowModal(true);
    };

    const handleSave = async () => {
        try {
            if (!formData.email) {
                alert("Email-ul este obligatoriu!");
                return;
            }

        const currentUser = users.find(u => u._links?.self?.href === editingUserLink);
        const oldEmail = currentUser?.email;

        const payload: any = {
            email: formData.email,
            rol: formData.role
        };

        if (formData.parola) payload.parola = formData.parola;

        if (editMode && editingUserLink) {

            let urlToUse = editingUserLink;
            if (urlToUse.includes('http')) {
                const urlObj = new URL(urlToUse);
                urlToUse = urlObj.pathname; 
            }
            
            const finalUrl = urlToUse.startsWith('/api') ? urlToUse.replace('/api', '') : urlToUse;

            await api.put(finalUrl, payload);

            if (oldEmail && oldEmail !== formData.email) {
                try {
                    await api.put('/client-service/clients/sync-email', {
                        oldEmail: oldEmail,
                        newEmail: formData.email
                    });
                } catch (syncErr) {
                    console.warn("Sincronizare sarita (profil inexistent).");
                }
            }
        } else {
            await api.post('/event-manager/users', payload);
        }

        setShowModal(false);
        loadUsers();
        alert("Salvare reusita!");
    } catch (e: any) {
        console.error("Eroare detaliata:", e);
        alert(`Eroare la salvare: ${e.response?.data?.message || e.message}`);
    }
};

    const styles = {
        container: {
            padding: '40px 20px',
            maxWidth: '1000px',
            margin: '0 auto',
            fontFamily: "'Inter', 'Segoe UI', sans-serif",
            color: '#1e293b'
        },
        header: {
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: '35px'
        },
        title: {
            margin: 0,
            fontSize: '1.8rem',
            fontWeight: '700',
            color: '#0f172a',
            letterSpacing: '-0.5px'
        },
        primaryButton: {
            backgroundColor: '#4f46e5',
            color: 'white',
            border: 'none',
            padding: '12px 24px',
            borderRadius: '12px',
            fontWeight: '600',
            cursor: 'pointer',
            boxShadow: '0 4px 6px -1px rgba(79, 70, 229, 0.2)',
            transition: 'all 0.2s'
        },
        card: {
            backgroundColor: 'white',
            borderRadius: '16px',
            boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.02)',
            overflow: 'hidden',
            border: '1px solid #f1f5f9'
        },
        table: {
            width: '100%',
            borderCollapse: 'collapse' as const,
        },
        th: {
            textAlign: 'left' as const,
            padding: '18px 24px',
            backgroundColor: '#f8fafc',
            color: '#64748b',
            fontWeight: '600',
            fontSize: '0.8rem',
            textTransform: 'uppercase' as const,
            letterSpacing: '0.05em',
            borderBottom: '1px solid #e2e8f0'
        },
        td: {
            padding: '20px 24px',
            borderBottom: '1px solid #f1f5f9',
            verticalAlign: 'middle',
            color: '#334155'
        },
        badge: (role: string) => {
            let bg = '#f1f5f9'; 
            let color = '#475569';
            if (role === 'ADMIN') { bg = '#dbeafe'; color = '#1e40af'; }
            if (role === 'OWNER_EVENT') { bg = '#dcfce7'; color = '#15803d'; }
            if (role === 'CLIENT') { bg = '#f3f4f6'; color = '#374151'; }
            return {
                backgroundColor: bg,
                color: color,
                padding: '6px 14px',
                borderRadius: '999px',
                fontSize: '0.75rem',
                fontWeight: '700',
                textTransform: 'uppercase' as const,
                letterSpacing: '0.5px',
                display: 'inline-block'
            };
        },
        avatar: {
            width: '38px',
            height: '38px',
            borderRadius: '50%',
            backgroundColor: '#e0e7ff',
            color: '#4f46e5',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: '700',
            fontSize: '0.9rem',
            marginRight: '15px'
        },
        actionBtn: {
            padding: '8px 24px',
            borderRadius: '10px',
            fontSize: '0.85rem',
            fontWeight: '600',
            cursor: 'pointer',
            border: 'none',
            transition: 'background 0.2s'
        },
        input: {
            width: '100%',
            padding: '12px 16px',
            borderRadius: '12px',
            border: '1px solid #e2e8f0',
            marginBottom: '20px',
            fontSize: '0.95rem',
            backgroundColor: '#f8fafc',
            outline: 'none',
            color: '#1e293b'
        },
        label: {
            display: 'block',
            marginBottom: '8px',
            fontWeight: '600',
            color: '#475569',
            fontSize: '0.85rem'
        }
    };

    if (loading) return <div style={{textAlign: 'center', marginTop: '50px', color: '#64748b'}}>Se incarca datele...</div>;

    return (
        <div style={styles.container}>
            
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



            <div style={styles.header}>
                <div>
                    <h2 style={styles.title}>Administrare Utilizatori</h2>
                    <p style={{ margin: '5px 0 0 0', color: '#64748b' }}>Gestioneaza conturile din sistem</p>
                </div>
                <button 
                    onClick={openCreateModal}
                    style={styles.primaryButton}
                    onMouseOver={(e) => e.currentTarget.style.transform = 'translateY(-1px)'}
                    onMouseOut={(e) => e.currentTarget.style.transform = 'translateY(0)'}
                >
                    + Utilizator Nou
                </button>
            </div>

            <div style={styles.card}>
                <table style={styles.table}>
                    <thead>
                        <tr>
                            <th style={styles.th}>Utilizator / Email</th>
                            <th style={styles.th}>ID</th>
                            <th style={styles.th}>Rol</th>
                            <th style={{...styles.th, textAlign: 'right'}}>Actiuni</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((u: any, idx) => (
                            <tr key={idx} style={{ transition: 'background 0.2s' }} onMouseOver={e => e.currentTarget.style.backgroundColor = '#f8fafc'} onMouseOut={e => e.currentTarget.style.backgroundColor = 'white'}>
                                <td style={styles.td}>
                                    <div style={{ display: 'flex', alignItems: 'center' }}>
                                        <div style={styles.avatar}>
                                            {u.email ? u.email.charAt(0).toUpperCase() : '?'}
                                        </div>
                                        <div style={{ fontWeight: '600', color: '#0f172a' }}>
                                            {u.email}
                                        </div>
                                    </div>
                                </td>
                                <td style={styles.td}>
                                    <span style={{color: '#94a3b8', fontSize: '0.9rem'}}>#{u.id}</span>
                                </td>
                                <td style={styles.td}>
                                    <span style={styles.badge(u.role || u.rol)}>
                                        {u.role || u.rol}
                                    </span>
                                </td>
                                <td style={{...styles.td, textAlign: 'right'}}>
                                    <button 
                                        onClick={() => openEditModal(u)}
                                        style={{...styles.actionBtn, backgroundColor: '#eff6ff', color: '#4f46e5'}}
                                        onMouseOver={e => e.currentTarget.style.backgroundColor = '#e0e7ff'}
                                        onMouseOut={e => e.currentTarget.style.backgroundColor = '#eff6ff'}
                                    >
                                        Editare
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {showModal && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
                    backgroundColor: 'rgba(15, 23, 42, 0.6)', 
                    backdropFilter: 'blur(4px)', 
                    zIndex: 1050,
                    display: 'flex', alignItems: 'center', justifyContent: 'center'
                }}>
                    <div style={{
                        backgroundColor: '#fff', 
                        padding: '35px', 
                        borderRadius: '24px', 
                        width: '450px',
                        maxWidth: '90%',
                        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)'
                    }}>
                        <h3 style={{ marginTop: 0, marginBottom: '25px', color: '#0f172a', fontWeight: '700' }}>
                            {editMode ? '✏️ Editare Cont' : '✨ Cont Nou'}
                        </h3>

                        <div>
                            <label style={styles.label}>Adresa Email</label>
                            <input 
                                style={styles.input}
                                value={formData.email} 
                                onChange={e => setFormData({...formData, email: e.target.value})} 
                                placeholder="utilizator@exemplu.com"
                            />
                        </div>

                        <div>
                            <label style={styles.label}>Rol Utilizator</label>
                            <select 
                                style={{...styles.input, appearance: 'none', backgroundImage: 'url("data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%23007CB2%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E")', backgroundRepeat: 'no-repeat', backgroundPosition: 'right .7em top 50%', backgroundSize: '.65em auto'}}
                                value={formData.role} 
                                onChange={e => setFormData({...formData, role: e.target.value})}
                            >
                                <option value="CLIENT">Client</option>
                                <option value="OWNER_EVENT">Owner Event</option>
                                <option value="ADMIN">Admin</option>
                            </select>
                        </div>

                        <div>
                            <label style={styles.label}>
                                Parola {editMode && <span style={{fontWeight:'400', color:'#94a3b8'}}>(Lasa gol pentru a pastra)</span>}
                            </label>
                            <input 
                                type="password" 
                                style={styles.input}
                                value={formData.parola} 
                                onChange={e => setFormData({...formData, parola: e.target.value})} 
                                placeholder="••••••••"
                            />
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '15px' }}>
                            <button 
                                onClick={() => setShowModal(false)}
                                style={{
                                    padding: '12px 24px', borderRadius: '12px', border: 'none',
                                    backgroundColor: '#f1f5f9', color: '#64748b', cursor: 'pointer', fontWeight: '600',
                                    transition: 'background 0.2s'
                                }}
                            >
                                Anuleaza
                            </button>
                            <button 
                                onClick={handleSave}
                                style={{...styles.primaryButton, padding: '12px 30px', boxShadow: '0 10px 15px -3px rgba(79, 70, 229, 0.3)'}}
                            >
                                Salveaza
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminDashboard;