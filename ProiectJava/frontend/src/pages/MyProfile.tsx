import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';
import { getLink } from '../utils/hateoas'; 
import { jwtDecode } from 'jwt-decode';

interface EventDetails {
    id?: number;
    nume: string;
    locatie: string;
    dataInceput?: string;
    dataSfarsit?: string;
}

interface Ticket {
    id: string | number;
    cod: string;
    event?: EventDetails;
    package?: EventDetails;
    status?: string;
    pret?: number;
    _links?: any; 
    selfUrl?: string;
}

interface UserProfile {
    nume: string;
    prenume: string;
    email: string;
    telefon?: string;
    _links?: any;
}

const MyProfile = () => {
    const navigate = useNavigate();
    
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [tickets, setTickets] = useState<Ticket[]>([]);
    
    const [ticketsCollectionUrl, setTicketsCollectionUrl] = useState<string | null>(null);

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loadingProfile, setLoadingProfile] = useState(true);
    const [loadingTickets, setLoadingTickets] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);

    const hateoasLinks = {
        home: { href: '/', rel: 'home', label: '🏠 Acasa' },
        self: { href: '/profile', rel: 'self', label: 'Profilul Meu' }
    };

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/login');
            return;
        }

        try {
            const decoded: any = jwtDecode(token);
            const userEmail = decoded.email || decoded.sub;
            initData(userEmail);
        } catch (e) {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
    }, [navigate]);

    const initData = async (email: string) => {
        try {
            const res = await api.get(`/client-service/clients/email/${email}`);
            const profileData = res.data.data || res.data;
            setProfile(profileData);

            let ticketsLink = getLink(profileData, 'tickets');

            if (ticketsLink) {
                try {
                    const u = new URL(ticketsLink);
                    ticketsLink = u.pathname;
                } catch (e) {}
            } else {
                ticketsLink = `/client-service/clients/${email}/tickets`;
            }

            setTicketsCollectionUrl(ticketsLink);
            
            if (ticketsLink) {
                await loadTickets(0, ticketsLink);
            } else {
                setLoadingTickets(false);
            }

        } catch (e) {
            console.error("Nu s-a putut incarca profilul", e);
            setLoadingTickets(false);
        } finally {
            setLoadingProfile(false);
        }
    };

    const loadTickets = async (pageToLoad: number, baseUrl: string | null = ticketsCollectionUrl) => {
        if (!baseUrl) return;

        if (pageToLoad === 0) setLoadingTickets(true);
        else setLoadingMore(true);

        try {
            const separator = baseUrl.includes('?') ? '&' : '?';
            const url = `${baseUrl}${separator}page=${pageToLoad}&size=5&sort=id,desc`;

            const res = await api.get(url);
            const data = res.data;
            
            const content = data._embedded?.ticketDataList || 
                            data._embedded?.ticketList || 
                            data.content || 
                            (Array.isArray(data) ? data : []);
            
            const normalized = content.map((item: any) => {
                const ticketData = item.data || item;
                
                let selfLink = getLink(item, 'self') || getLink(ticketData, 'self');
                if (selfLink) {
                    try { const u = new URL(selfLink); selfLink = u.pathname; } catch(e) {}
                }

                return {
                    ...ticketData,
                    selfUrl: selfLink 
                };
            });

            if (pageToLoad === 0) {
                setTickets(normalized);
            } else {
                setTickets(prev => [...prev, ...normalized]);
            }

            if (data.page) {
                setTotalPages(data.page.totalPages);
            } else {
                setTotalPages(normalized.length < 5 ? pageToLoad + 1 : 999);
            }
            setPage(pageToLoad);

        } catch (e) {
            console.error("Eroare la incarcarea biletelor:", e);
        } finally {
            setLoadingTickets(false);
            setLoadingMore(false);
        }
    };

    return (
        <div className="container" style={{ maxWidth: '800px', margin: '0 auto', paddingBottom: '50px' }}>
            
            <div style={{ margin: '20px 0' }}>
                <nav aria-label="breadcrumb">
                    <div style={{ display: 'flex', gap: '10px', alignItems: 'center', fontSize: '0.9rem', color: '#6b7280' }}>
                        <Link to={hateoasLinks.home.href} style={{ textDecoration: 'none', color: '#4b5563', display: 'flex', alignItems: 'center', gap: '5px' }}>
                            {hateoasLinks.home.label}
                        </Link>
                        <span>/</span>
                        <span style={{ fontWeight: '600', color: '#111827' }}>{hateoasLinks.self.label}</span>
                    </div>
                </nav>
            </div>



            <div className="card" style={{ marginBottom: '40px', background: 'linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%)', color: 'white', border: 'none', borderRadius: '12px', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }}>
                {loadingProfile ? (
                    <div style={{ padding: '40px', textAlign: 'center' }}>Se incarca profilul...</div>
                ) : (
                    <div style={{ padding: '30px', display: 'flex', alignItems: 'center', gap: '25px', flexWrap: 'wrap' }}>
                        <div style={{ 
                            width: '80px', height: '80px', borderRadius: '50%', background: 'white', color: '#4f46e5',
                            display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '2rem', fontWeight: 'bold', boxShadow: '0 4px 6px rgba(0,0,0,0.2)'
                        }}>
                            {profile?.nume?.charAt(0) || 'U'}
                        </div>
                        <div style={{ flex: 1 }}>
                            <h2 style={{ margin: 0, fontSize: '1.8rem', fontWeight: '800' }}>{profile?.prenume} {profile?.nume}</h2>
                            <p style={{ margin: '5px 0 0 0', opacity: 0.9, fontSize: '1rem' }}>{profile?.email}</p>
                            {profile?.telefon && <p style={{ margin: '5px 0 0 0', opacity: 0.8, fontSize: '0.9rem' }}>📞 {profile?.telefon}</p>}
                        </div>
                        <button 
                            onClick={() => navigate('/edit-profile')} 
                            className="btn btn-sm" 
                            style={{ background: 'rgba(255,255,255,0.2)', color: 'white', border: '1px solid rgba(255,255,255,0.3)', backdropFilter: 'blur(8px)', padding: '8px 16px', borderRadius: '8px' }}
                        >
                            ⚙️ Setari Profil
                        </button>
                    </div>
                )}
            </div>



            <h3 style={{ marginBottom: '25px', borderBottom: '2px solid #f3f4f6', paddingBottom: '15px', color: '#111827', display: 'flex', alignItems: 'center', gap: '10px' }}>
                🎫 Biletele Mele 
                {tickets.length > 0 && <span style={{fontSize: '0.9rem', color:'#6b7280', fontWeight:'500', background:'#f3f4f6', padding:'2px 10px', borderRadius:'12px'}}>{tickets.length} total</span>}
            </h3>

            {loadingTickets ? (
                <div style={{ textAlign: 'center', padding: '60px' }}>
                    <div className="spinner-border text-primary" role="status"></div>
                    <p style={{marginTop: '15px', color: '#6b7280'}}>Se preiau biletele tale...</p>
                </div>
            ) : tickets.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '60px', background: '#f9fafb', borderRadius: '16px', border: '2px dashed #e5e7eb' }}>
                    <div style={{fontSize: '3rem', marginBottom: '10px'}}>📩</div>
                    <p style={{ fontSize: '1.2rem', color: '#4b5563', marginBottom: '20px' }}>Nu ai cumparat niciun bilet inca.</p>
                    <button onClick={() => navigate('/')} className="btn btn-primary px-4 py-2" style={{borderRadius: '8px', fontWeight: '600'}}>Exploreaza Evenimente</button>
                </div>
            ) : (
                <div style={{ display: 'grid', gap: '20px' }}>
                    {tickets.map((t, index) => {
                        const isEvent = !!t.event; 
                        const entity = t.event || t.package;

                        return (
                            <div key={index} className="card shadow-sm" style={{ 
                                padding: '0', border: '1px solid #e5e7eb', borderRadius: '12px', overflow: 'hidden', display: 'flex', flexDirection: 'row', alignItems: 'stretch'
                            }}>
                                <div style={{ width: '8px', background: isEvent ? '#4f46e5' : '#f59e0b' }}></div>

                                <div style={{ padding: '25px', flex: 1 }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                                        <span style={{ 
                                            fontSize: '0.75rem', fontWeight: '800', textTransform: 'uppercase', letterSpacing: '0.5px',
                                            color: isEvent ? '#4f46e5' : '#b45309', 
                                            background: isEvent ? '#e0e7ff' : '#fef3c7', 
                                            padding: '4px 12px', borderRadius: '6px'
                                        }}>
                                            {isEvent ? 'EVENIMENT' : 'PACHET'}
                                        </span>
                                        <span style={{ fontSize: '0.85rem', color: '#9ca3af', fontWeight: '500' }}>#{t.id}</span>
                                    </div>
                                    
                                    <h4 style={{ margin: '0 0 8px 0', fontSize: '1.3rem', color: '#111827', fontWeight: '700' }}>{entity?.nume}</h4>
                                    
                                    <p style={{ color: '#4b5563', margin: 0, fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                        <i className="bi bi-geo-alt"></i> 📍 {entity?.locatie || 'Locatie nespecificata'}
                                    </p>
                                    
                                    {entity?.dataInceput && (
                                        <p style={{ color: '#6b7280', margin: '8px 0 0 0', fontSize: '0.9rem', fontWeight: '500' }}>
                                            📅 {new Date(entity.dataInceput).toLocaleDateString('ro-RO', { day: 'numeric', month: 'long', year: 'numeric' })}
                                        </p>
                                    )}
                                </div>

                                <div style={{ 
                                    background: '#f8fafc', borderLeft: '1px dashed #cbd5e1', padding: '25px', 
                                    display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', minWidth: '180px'
                                }}>
                                    <div style={{ fontSize: '0.75rem', color: '#64748b', textTransform: 'uppercase', fontWeight: '700', marginBottom: '8px', letterSpacing: '1px' }}>COD ACCES</div>
                                    <div style={{ fontFamily: '"Courier New", Courier, monospace', fontSize: '1.1rem', fontWeight: '800', color: '#0f172a', letterSpacing: '1px', background: 'white', padding: '10px', borderRadius: '8px', border: '1px solid #e2e8f0', textAlign: 'center', wordBreak: 'break-all' }}>
                                        {t.cod}
                                    </div>
                                </div>
                            </div>
                        );
                    })}

                    {page < totalPages - 1 && (
                        <div style={{ textAlign: 'center', marginTop: '30px' }}>
                            <button onClick={() => loadTickets(page + 1)} className="btn btn-outline-primary shadow-sm" disabled={loadingMore} style={{ minWidth: '220px', fontWeight: '600', borderRadius: '10px' }}>
                                {loadingMore ? 'Se incarca...' : 'Vezi mai multe bilete'}
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default MyProfile;