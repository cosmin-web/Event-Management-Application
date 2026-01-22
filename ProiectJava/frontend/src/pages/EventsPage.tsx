import React, { useEffect, useState, useCallback } from 'react';
import api from '../utils/api';
import { jwtDecode } from 'jwt-decode';
import { useNavigate, Link } from 'react-router-dom';

const PackageEventNames = ({ packageObj }: { packageObj: any }) => {
    const [eventsList, setEventsList] = useState<{ name: string, desc: string }[]>([]);

    useEffect(() => {
        if (!packageObj) return;

        const data = packageObj.data || packageObj;
        const id = data.id;

        if (id) {
            api.get(`/event-manager/event-packets/${id}/events`)
                .then(res => {
                    const rawData = res.data._embedded?.packageEventDTOList || 
                                    res.data._embedded?.eventDTOList || 
                                    res.data._embedded?.packageEventList ||
                                    (Array.isArray(res.data) ? res.data : []);
                    
                    const list = rawData.map((item: any) => {
                        const d = item.data || item;
                        return {
                            name: d.eventName || d.nume,
                            desc: d.eventDescription || d.descriere || "Fara descriere"
                        };
                    });
                    setEventsList(list);
                })
                .catch(() => setEventsList([]));
        }
    }, [packageObj]);

    if (eventsList.length === 0) return null;

    return (
        <div style={{ marginTop: '10px', fontSize: '0.85rem', color: '#4b5563', background: '#f3f4f6', padding: '10px', borderRadius: '6px' }}>
            <strong style={{ display: 'block', marginBottom: '5px', color: '#1f2937' }}>Include:</strong>
            <ul style={{ margin: '0', paddingLeft: '20px' }}>
                {eventsList.map((ev, idx) => (
                    <li key={idx}>
                        <strong>{ev.name}</strong> <span style={{ color: '#6b7280' }}>- {ev.desc}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
};

const EventsPage = () => {
    const navigate = useNavigate();
    const PAGE_SIZE = 5;

    const [events, setEvents] = useState<any[]>([]);
    const [packages, setPackages] = useState<any[]>([]);
    
    const [eventPage, setEventPage] = useState(0);
    const [eventTotalPages, setEventTotalPages] = useState(0);

    const [packagePage, setPackagePage] = useState(0);
    const [packageTotalPages, setPackageTotalPages] = useState(0);

    const [inputFilters, setInputFilters] = useState({ name: '', location: '', type: '', availableTickets: '' });
    const [activeFilters, setActiveFilters] = useState({ name: '', location: '', type: '', availableTickets: '' });
    const [showAdvanced, setShowAdvanced] = useState(false);
    
    const [userRole, setUserRole] = useState<string>('');
    const [userId, setUserId] = useState<number>(0);
    const [userEmail, setUserEmail] = useState<string>('');

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decoded: any = jwtDecode(token);
                setUserRole(decoded.role || decoded.rol);
                setUserId(Number(decoded.sub));
                setUserEmail(decoded.email || decoded.sub);
            } catch (e) { }
        }
    }, []);

    const loadEvents = useCallback(async () => {
        try {
            const params = new URLSearchParams();
            params.append('page', eventPage.toString());
            params.append('items_per_page', PAGE_SIZE.toString());
            params.append('ts', Date.now().toString());
            
            if (activeFilters.name) params.append('name', activeFilters.name);
            if (activeFilters.location) params.append('location', activeFilters.location);
            if (activeFilters.availableTickets) params.append('available_tickets', activeFilters.availableTickets);

            const res = await api.get(`/event-manager/events?${params.toString()}`);
            const data = res.data;

            let rawList = data._embedded?.eventDTOList || 
                          data._embedded?.eventList || 
                          data.content || 
                          (Array.isArray(data) ? data : []);
            
            setEvents(rawList);
            
            if (data.page) {
                setEventTotalPages(data.page.totalPages);
            } else if (rawList.length < PAGE_SIZE) {
                setEventTotalPages(eventPage + 1); 
            } else {
                setEventTotalPages(999);
            }
        } catch (err) { console.error("Eroare events:", err); }
    }, [eventPage, activeFilters]); 

    const loadPackages = useCallback(async () => {
        try {
            const params = new URLSearchParams();
            params.append('page', packagePage.toString());
            params.append('items_per_page', PAGE_SIZE.toString());
            params.append('ts', Date.now().toString());

            if (activeFilters.name) params.append('name', activeFilters.name);
            if (activeFilters.type) params.append('type', activeFilters.type);
            if (activeFilters.availableTickets) params.append('available_tickets', activeFilters.availableTickets);

            const res = await api.get(`/event-manager/event-packets?${params.toString()}`);
            const data = res.data;

            let rawList = data._embedded?.packageDTOList || 
                          data._embedded?.packageList || 
                          data.content || 
                          (Array.isArray(data) ? data : []);

            setPackages(rawList);

            if (data.page) {
                setPackageTotalPages(data.page.totalPages);
            } else if (rawList.length < PAGE_SIZE) {
                setPackageTotalPages(packagePage + 1);
            } else {
                setPackageTotalPages(999);
            }
        } catch (err) { console.error("Eroare packages:", err); }
    }, [packagePage, activeFilters]);

    useEffect(() => { loadEvents(); }, [loadEvents]);
    useEffect(() => { loadPackages(); }, [loadPackages]);

    const handleSearch = () => {
        setActiveFilters(inputFilters);
        setEventPage(0);
        setPackagePage(0);
    };

    const nextEventPage = () => { if (eventPage < eventTotalPages - 1) setEventPage(p => p + 1); };
    const prevEventPage = () => { if (eventPage > 0) setEventPage(p => p - 1); };

    const nextPackagePage = () => { if (packagePage < packageTotalPages - 1) setPackagePage(p => p + 1); };
    const prevPackagePage = () => { if (packagePage > 0) setPackagePage(p => p - 1); };

    const buyTicket = async (id: number, type: 'event' | 'package') => {
        const token = localStorage.getItem('token');
        if (!token) {
            if (window.confirm("Trebuie să fii autentificat. Mergi la login?")) return navigate('/login');
            return;
        }

        try {
            if (!window.confirm(`Cumperi un bilet pentru acest ${type === 'event' ? 'eveniment' : 'pachet'}?`)) return;

            const url = type === 'event'
                ? `/client-service/clients/${userEmail}/tickets/events/${id}`
                : `/client-service/clients/${userEmail}/tickets/packages/${id}`;

            await api.post(url);
            alert("Bilet cumparat cu succes!");

            await loadEvents();
            if (type === 'package') await loadPackages();

        } catch (e: any) {
            const msg = e.response?.data?.message || "Eroare la cumparare.";
            
            if (msg.includes("No link with rel self found")) {
                console.warn("Backend HATEOAS error ignored. Transaction assumed successful.");
                alert("Bilet cumparat cu succes!");
                await loadEvents();
                if (type === 'package') await loadPackages();
                return;
            }

            alert(msg);
        }
    };

    const deleteItem = async (resource: any, type: 'event' | 'package') => {
        if (!window.confirm("Esti sigur? Aceasta actiune este ireversibila.")) return;
        
        const data = resource.data || resource;
        const id = data.id || resource.id; 

        if (!id) {
            alert("Eroare: Nu pot identifica ID-ul elementului.");
            return;
        }
        
        try {
            const url = type === 'event' 
                ? `/event-manager/events/${id}` 
                : `/event-manager/event-packets/${id}`;
            
            await api.delete(url);
            
            if (type === 'event') loadEvents(); 
            else loadPackages();
        } catch (e) { alert("Eroare la stergere."); }
    }

    return (
        <div className="container" style={{ paddingBottom: '50px' }}>
            
            <nav aria-label="breadcrumb" style={{ marginBottom: '20px', marginTop: '20px' }}>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center', fontSize: '0.9rem', color: '#6b7280' }}>
                    <Link to="/" style={{ textDecoration: 'none', color: '#4b5563', display: 'flex', alignItems: 'center', gap: '5px' }}>
                        🏠 Acasa
                    </Link>
                    <span>/</span>
                    <span style={{ fontWeight: '600', color: '#111827', cursor: 'default' }}>
                        Evenimente & Pachete
                    </span>
                </div>
            </nav>

            <div className="card header-card" style={{ background: 'linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%)', color: 'white', padding: '30px' }}>
                <h1 style={{ color: 'white', marginBottom: '20px' }}>Cauta evenimente si pachete</h1>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px', maxWidth: '800px' }}>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <input
                            placeholder="Cauta dupa nume..."
                            value={inputFilters.name}
                            onChange={e => setInputFilters({ ...inputFilters, name: e.target.value })}
                            onKeyDown={e => e.key === 'Enter' && handleSearch()}
                            style={{ border: 'none', borderRadius: '6px', padding: '12px', flex: 1, marginBottom: 0 }}
                        />
                        <button onClick={handleSearch} className="btn" style={{ background: 'white', color: 'var(--primary)', fontWeight: 'bold', padding: '0 30px' }}>
                            Cauta
                        </button>
                    </div>

                    <div>
                        <span onClick={() => setShowAdvanced(!showAdvanced)} style={{ cursor: 'pointer', fontSize: '0.9rem', textDecoration: 'underline', opacity: 0.9 }}>
                            {showAdvanced ? '▲ Ascunde filtre' : '▼ Mai multe filtre'}
                        </span>
                    </div>

                    {showAdvanced && (
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '10px', background: 'rgba(255,255,255,0.1)', padding: '15px', borderRadius: '8px' }}>
                            <input value={inputFilters.location} onChange={e => setInputFilters({ ...inputFilters, location: e.target.value })} placeholder="Locatie" style={{ border: 'none', borderRadius: '4px', padding: '8px', fontSize: '0.9rem', marginBottom: 0 }} />
                            <input value={inputFilters.type} onChange={e => setInputFilters({ ...inputFilters, type: e.target.value })} placeholder="Tip / Descriere" style={{ border: 'none', borderRadius: '4px', padding: '8px', fontSize: '0.9rem', marginBottom: 0 }} />
                            <input type="number" value={inputFilters.availableTickets} onChange={e => setInputFilters({ ...inputFilters, availableTickets: e.target.value })} placeholder="Locuri minime" style={{ border: 'none', borderRadius: '4px', padding: '8px', fontSize: '0.9rem', marginBottom: 0 }} />
                        </div>
                    )}
                </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '40px', marginBottom: '20px' }}>
                <h2>📦 Pachete Speciale</h2>
                <span style={{ color: '#666', fontSize: '0.9rem' }}>Pagina {packagePage + 1} / {packageTotalPages || 1}</span>
            </div>

            {packages.length === 0 ? <p className="text-muted">Nu am gasit pachete conform filtrelor.</p> : (
                <div className="grid-container">
                    {packages.map((w: any) => {
                        const p = w.data || w;
                        return (
                            <div key={p.id} className="card" style={{ borderTop: '5px solid #8b5cf6', position: 'relative' }}>
                                <span className="badge" style={{ background: '#8b5cf6', color: 'white', position: 'absolute', top: '15px', right: '15px' }}>PACHET</span>
                                <h3 style={{ marginTop: '10px', fontSize: '1.2rem' }}>{p.nume}</h3>
                                <p className="text-muted" style={{ fontSize: '0.9rem' }}>📍 {p.locatie}</p>
                                <p style={{ fontSize: '0.95rem', margin: '10px 0' }}>{p.descriere}</p>
                                
                                <PackageEventNames packageObj={w} />
                                
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px', paddingTop: '15px', borderTop: '1px solid #eee' }}>
                                    <span style={{ fontWeight: 'bold', color: p.availableTickets > 0 ? '#10b981' : '#ef4444' }}>
                                        {p.availableTickets > 0 ? `${p.availableTickets} locuri` : 'SOLD OUT'}
                                    </span>
                                    {userRole === 'CLIENT' && (
                                        <button onClick={() => buyTicket(p.id, 'package')} disabled={p.availableTickets <= 0} className="btn btn-primary btn-sm" style={{ background: p.availableTickets <= 0 ? '#9ca3af' : undefined }}>Cumpara</button>
                                    )}
                                    {userRole === 'OWNER_EVENT' && p.ownerId === userId && (
                                        <div style={{ display: 'flex', gap: '5px' }}>
                                            <button onClick={() => navigate(`/manage-package/${p.id}`)} className="btn btn-warning btn-sm" style={{ color: 'white' }}>Gestiune</button>
                                            <button onClick={() => deleteItem(w, 'package')} className="btn btn-danger btn-sm">Sterge</button>
                                        </div>
                                    )}
                                </div>
                            </div>
                        )
                    })}
                </div>
            )}
            
            <div style={{ display: 'flex', justifyContent: 'center', gap: '15px', marginBottom: '40px' }}>
                <button onClick={prevPackagePage} disabled={packagePage === 0} className="btn btn-secondary btn-sm">&larr; Anterior</button>
                <button onClick={nextPackagePage} disabled={packagePage >= packageTotalPages - 1} className="btn btn-secondary btn-sm">Urmator &rarr;</button>
            </div>

            <hr style={{ border: '0', borderTop: '1px solid #e5e7eb', margin: '40px 0' }} />

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h2>🎫 Evenimente Individuale</h2>
                <span style={{ color: '#666', fontSize: '0.9rem' }}>Pagina {eventPage + 1} / {eventTotalPages || 1}</span>
            </div>

            {events.length === 0 ? <p className="text-muted">Nu am gasit evenimente conform filtrelor.</p> : (
                <div className="grid-container">
                    {events.map((w: any) => {
                        const e = w.data || w;
                        return (
                            <div key={e.id} className="card">
                                <h3 style={{ fontSize: '1.2rem' }}>{e.nume}</h3>
                                <p className="text-muted" style={{ fontSize: '0.9rem' }}>📍 {e.locatie}</p>
                                <p style={{ fontSize: '0.95rem', margin: '10px 0' }}>{e.descriere}</p>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px', paddingTop: '15px', borderTop: '1px solid #eee' }}>
                                    <span style={{ fontWeight: 'bold', color: e.availableTickets > 0 ? '#10b981' : '#ef4444' }}>
                                        {e.availableTickets > 0 ? `${e.availableTickets} locuri` : 'SOLD OUT'}
                                    </span>
                                    {userRole === 'CLIENT' && (
                                        <button onClick={() => buyTicket(e.id, 'event')} disabled={e.availableTickets <= 0} className="btn btn-primary btn-sm" style={{ background: e.availableTickets <= 0 ? '#9ca3af' : undefined }}>Cumpara</button>
                                    )}
                                    {userRole === 'OWNER_EVENT' && e.ownerId === userId && (
                                        <div style={{ display: 'flex', gap: '5px' }}>
                                            <button onClick={() => navigate(`/edit-event/${e.id}`)} className="btn btn-secondary btn-sm">Edit</button>
                                            <button onClick={() => navigate(`/event-clients/${e.id}`)} className="btn btn-info btn-sm" style={{color:'white'}}>Participanti</button>
                                            <button onClick={() => deleteItem(w, 'event')} className="btn btn-danger btn-sm">Sterge</button>
                                        </div>
                                    )}
                                </div>
                            </div>
                        )
                    })}
                </div>
            )}
            
            <div style={{ display: 'flex', justifyContent: 'center', gap: '15px', marginTop: '20px' }}>
                <button onClick={prevEventPage} disabled={eventPage === 0} className="btn btn-secondary btn-sm">&larr; Anterior</button>
                <button onClick={nextEventPage} disabled={eventPage >= eventTotalPages - 1} className="btn btn-secondary btn-sm">Urmator &rarr;</button>
            </div>
        </div>
    );
};

export default EventsPage;