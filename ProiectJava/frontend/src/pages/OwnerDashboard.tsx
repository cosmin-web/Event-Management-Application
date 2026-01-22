import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';
import { jwtDecode } from 'jwt-decode';
import { getLink } from '../utils/hateoas';

interface HateoasLink {
    href: string;
    rel: string;
    method: string;
    label: string;
}

const OwnerDashboard = () => {
    const navigate = useNavigate();
    
    const [allMyEvents, setAllMyEvents] = useState<any[]>([]);
    const [allMyPackages, setAllMyPackages] = useState<any[]>([]);
    
    const PAGE_SIZE = 5;
    const [eventPage, setEventPage] = useState(0);
    const [packagePage, setPackagePage] = useState(0);

    const [loading, setLoading] = useState(true);
    const [userName, setUserName] = useState('');
    const [userId, setUserId] = useState<number>(0);

    const hateoasLinks: Record<string, HateoasLink> = {
        parent: { href: '/', rel: 'parent', method: 'GET', label: '🏠 Acasa' },
        self: { href: '/owner-dashboard', rel: 'self', method: 'GET', label: 'Panou Control' }
    };

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/login');
            return;
        }

        try {
            const decoded: any = jwtDecode(token);
            const uId = Number(decoded.sub);
            setUserId(uId);
            setUserName(decoded.email || 'Organizator');
            
            const role = decoded.rol || decoded.role;
            if (role !== 'OWNER_EVENT' && role !== 'ADMIN') {
                navigate('/');
                return;
            }

            loadData(uId);
        } catch (e) {
            navigate('/login');
        }
    }, [navigate]);

    const loadData = async (currentUserId: number) => {
        try {
            setLoading(true);
            
            const resEvents = await api.get('/event-manager/events?items_per_page=1000');
            const rawEvents = resEvents.data._embedded?.eventDTOList || 
                              resEvents.data._embedded?.eventList || 
                              resEvents.data.content || 
                              (Array.isArray(resEvents.data) ? resEvents.data : []);
            
            const myEvents = rawEvents.filter((wrapper: any) => {
                const data = wrapper.data || wrapper;
                return Number(data.ownerId) === currentUserId;
            });
            setAllMyEvents(myEvents);

            const resPackages = await api.get('/event-manager/event-packets?items_per_page=1000');
            const rawPackages = resPackages.data._embedded?.packageDTOList || 
                                resPackages.data._embedded?.packageList || 
                                resPackages.data.content || 
                                (Array.isArray(resPackages.data) ? resPackages.data : []);
            
            const myPackages = rawPackages.filter((wrapper: any) => {
                const data = wrapper.data || wrapper;
                return Number(data.ownerId) === currentUserId;
            });
            setAllMyPackages(myPackages);

        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (wrapper: any, type: 'event' | 'package') => {
        if (!confirm("Esti sigur ca vrei sa stergi acest element?")) return;

        const data = wrapper.data || wrapper;
        
        let url = getLink(wrapper, 'self');
        if (!url) url = getLink(data, 'self');

        if (url) {
            try {
                if (url.startsWith('http')) {
                    const urlObj = new URL(url);
                    url = urlObj.pathname;
                }
            } catch(e) { }
        }

        if (!url) {
             const id = data.id;
             url = type === 'event' 
                ? `/event-manager/events/${id}` 
                : `/event-manager/event-packets/${id}`;
        }

        if (url.startsWith('/api/')) {
            url = url.replace('/api', '');
        }

        try {
            await api.delete(url);
            loadData(userId);
        } catch (e: any) {
            let msg = "Eroare la stergere.";
            if (e.response && e.response.data) {
                if (typeof e.response.data === 'object' && e.response.data.message) {
                    msg = e.response.data.message;
                } else if (typeof e.response.data === 'string') {
                    msg = e.response.data;
                }
            }
            alert(msg);
        }
    };

    const totalEventPages = Math.ceil(allMyEvents.length / PAGE_SIZE);
    const totalPackagePages = Math.ceil(allMyPackages.length / PAGE_SIZE);

    const visibleEvents = allMyEvents.slice(eventPage * PAGE_SIZE, (eventPage + 1) * PAGE_SIZE);
    const visiblePackages = allMyPackages.slice(packagePage * PAGE_SIZE, (packagePage + 1) * PAGE_SIZE);

    const nextEventPage = () => { if (eventPage < totalEventPages - 1) setEventPage(p => p + 1); };
    const prevEventPage = () => { if (eventPage > 0) setEventPage(p => p - 1); };

    const nextPackagePage = () => { if (packagePage < totalPackagePages - 1) setPackagePage(p => p + 1); };
    const prevPackagePage = () => { if (packagePage > 0) setPackagePage(p => p - 1); };

    if (loading) return <div className="container text-center mt-5">Se incarca panoul...</div>;

    return (
        <div className="container" style={{ marginTop: '30px', maxWidth: '1200px', paddingBottom: '50px' }}>
            <nav aria-label="breadcrumb" style={{ marginBottom: '20px' }}>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center', fontSize: '0.9rem', color: '#6b7280' }}>
                    <Link 
                        to={hateoasLinks.parent.href} 
                        style={{ textDecoration: 'none', color: '#4b5563', display: 'flex', alignItems: 'center', gap: '5px' }}
                    >
                        {hateoasLinks.parent.label}
                    </Link>
                    <span>/</span>
                    <span style={{ fontWeight: '600', color: '#111827', cursor: 'default' }}>
                        {hateoasLinks.self.label}
                    </span>
                </div>
            </nav>

            <div className="card" style={{ border: 'none', background: 'transparent', boxShadow: 'none', padding: 0, marginBottom: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', borderBottom: '1px solid #e5e7eb', paddingBottom: '15px' }}>
                    <div>
                        <h1 style={{ fontSize: '1.8rem', color: '#1f2937', margin: 0 }}>
                            📊 Panou Organizator
                        </h1>
                        <p className="text-muted" style={{ margin: '5px 0 0 0' }}>
                            Bun venit, {userName}
                        </p>
                    </div>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(450px, 1fr))', gap: '30px' }}>
                
                <div className="card" style={{ padding: '0', borderTop: '4px solid #4f46e5' }}> 
                    <div style={{ padding: '20px', background: '#f9fafb', borderBottom: '1px solid #eee', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h3 style={{ margin: 0, color: '#4f46e5', fontSize: '1.2rem' }}>
                            📅 Evenimente ({allMyEvents.length})
                        </h3>
                        <div style={{display:'flex', alignItems:'center', gap:'15px'}}>
                             <span style={{ fontSize: '0.8rem', color: '#6b7280' }}>
                                Pagina {eventPage + 1} / {totalEventPages || 1}
                            </span>
                            <button onClick={() => navigate('/create-event')} className="btn btn-primary btn-sm" style={{ background: '#4f46e5' }}>+ Adauga</button>
                        </div>
                    </div>

                    <div style={{ padding: '20px' }}>
                        {allMyEvents.length === 0 ? (
                            <p className="text-muted text-center">Nu ai creat niciun eveniment.</p>
                        ) : (
                            <>
                                <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                                    {visibleEvents.map((w: any) => {
                                        const e = w.data || w; 
                                        return (
                                            <li key={e.id} style={{ 
                                                background: 'white', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '15px', marginBottom: '10px',
                                                display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 1px 2px rgba(0,0,0,0.05)'
                                            }}>
                                                <div>
                                                    <strong style={{ fontSize: '1rem', color: '#1f2937' }}>{e.nume}</strong>
                                                    <div style={{ fontSize: '0.85rem', color: '#6b7280' }}>📍 {e.locatie}</div>
                                                    <div style={{ fontSize: '0.8rem', marginTop: '4px' }}>
                                                        Stoc: <span style={{ fontWeight: 'bold', color: e.availableTickets > 0 ? 'green' : 'red' }}>{e.availableTickets}</span> / {e.numarLocuri}
                                                    </div>
                                                </div>
                                                <div style={{ display: 'flex', gap: '5px', flexDirection: 'column' }}>
                                                    <button onClick={() => navigate(`/edit-event/${e.id}`)} className="btn btn-outline-secondary btn-sm" style={{ fontSize: '0.8rem' }}>✏️ Edit</button>
                                                    
                                                    <button onClick={() => handleDelete(w, 'event')} className="btn btn-danger btn-sm" style={{ fontSize: '0.8rem' }}>🗑️ Sterge</button>
                                                </div>
                                            </li>
                                        )
                                    })}
                                </ul>

                                {(totalEventPages > 1) && (
                                    <div style={{ display: 'flex', justifyContent: 'center', gap: '15px', marginTop: '20px' }}>
                                        <button onClick={prevEventPage} disabled={eventPage === 0} className="btn btn-secondary btn-sm">&larr; Anterior</button>
                                        <button onClick={nextEventPage} disabled={eventPage >= totalEventPages - 1} className="btn btn-secondary btn-sm">Urmator &rarr;</button>
                                    </div>
                                )}
                            </>
                        )}
                    </div>
                </div>

                <div className="card" style={{ padding: '0', borderTop: '4px solid #d97706' }}>
                    <div style={{ padding: '20px', background: '#fffbeb', borderBottom: '1px solid #fde68a', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h3 style={{ margin: 0, color: '#d97706', fontSize: '1.2rem' }}>
                            📦 Pachete Promo ({allMyPackages.length})
                        </h3>
                         <div style={{display:'flex', alignItems:'center', gap:'15px'}}>
                             <span style={{ fontSize: '0.8rem', color: '#6b7280' }}>
                                Pagina {packagePage + 1} / {totalPackagePages || 1}
                            </span>
                            <button onClick={() => navigate('/create-package')} className="btn btn-warning btn-sm" style={{ color: 'white' }}>+ Adauga</button>
                        </div>
                    </div>

                    <div style={{ padding: '20px' }}>
                        {allMyPackages.length === 0 ? (
                            <p className="text-muted text-center">Nu ai creat niciun pachet.</p>
                        ) : (
                            <>
                                <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                                    {visiblePackages.map((w: any) => {
                                        const p = w.data || w;
                                        return (
                                            <li key={p.id} style={{ 
                                                background: 'white', border: '1px solid #fcd34d', borderRadius: '8px', padding: '15px', marginBottom: '10px',
                                                display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 1px 2px rgba(0,0,0,0.05)'
                                            }}>
                                                <div>
                                                    <strong style={{ fontSize: '1rem', color: '#1f2937' }}>{p.nume}</strong>
                                                    <div style={{ fontSize: '0.85rem', color: '#6b7280' }}>📍 {p.locatie}</div>
                                                    <div style={{ fontSize: '0.8rem', marginTop: '4px' }}>
                                                        Evenimente incluse: <strong>{p.numberOfEvents || 0}</strong>
                                                    </div>
                                                </div>
                                                <div style={{ display: 'flex', gap: '5px', flexDirection: 'column' }}>
                                                    <button onClick={() => navigate(`/manage-package/${p.id}`)} className="btn btn-primary btn-sm" style={{ fontSize: '0.8rem', background: '#d97706', border: 'none' }}>⚙️ Gestiune</button>
                                                    <button onClick={() => handleDelete(w, 'package')} className="btn btn-danger btn-sm" style={{ fontSize: '0.8rem' }}>🗑️ Sterge</button>
                                                </div>
                                            </li>
                                        )
                                    })}
                                </ul>

                                {(totalPackagePages > 1) && (
                                    <div style={{ display: 'flex', justifyContent: 'center', gap: '15px', marginTop: '20px' }}>
                                        <button onClick={prevPackagePage} disabled={packagePage === 0} className="btn btn-secondary btn-sm">&larr; Anterior</button>
                                        <button onClick={nextPackagePage} disabled={packagePage >= totalPackagePages - 1} className="btn btn-secondary btn-sm">Urmator &rarr;</button>
                                    </div>
                                )}
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OwnerDashboard;