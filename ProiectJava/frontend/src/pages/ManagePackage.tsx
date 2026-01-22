import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';
import { getLink } from '../utils/hateoas';
import { jwtDecode } from 'jwt-decode';

const ManagePackage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    
    const [pkg, setPkg] = useState<any>(null);
    const [packageEvents, setPackageEvents] = useState<any[]>([]);
    const [myEvents, setMyEvents] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [processing, setProcessing] = useState<number | string | null>(null);

    const hateoasLinks = {
        parent: { href: '/owner-dashboard', rel: 'parent', label: '🛡️ Panou Organizator' },
        self: { href: `/manage-package/${id}`, rel: 'self', label: 'Gestionare Continut Pachet' }
    };

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decoded: any = jwtDecode(token);
                const userRole = decoded.rol || decoded.role;
                if (userRole !== 'OWNER_EVENT' && userRole !== 'ADMIN') {
                    navigate('/');
                    return;
                }
                loadData(Number(decoded.sub));
            } catch (e) { navigate('/login'); }
        } else {
            navigate('/login');
        }
    }, [id, navigate]);

    const loadData = async (currentUserId: number) => {
        try {
            const resPkg = await api.get(`/event-manager/event-packets/${id}`);
            const pkgData = resPkg.data.data || resPkg.data;
            setPkg(pkgData);

            let eventsUrl = getLink(pkgData, 'events');
            
            if (eventsUrl) {
                try {
                    const u = new URL(eventsUrl);
                    eventsUrl = u.pathname + u.search;
                } catch (e) {}
            }

            const urlToFetchEvents = eventsUrl || `/event-manager/event-packets/${id}/events`;
            
            const resPE = await api.get(urlToFetchEvents);
            const rawPE = resPE.data._embedded?.packageEventDTOList || 
                          resPE.data._embedded?.packageEventList || 
                          resPE.data.content || 
                          (Array.isArray(resPE.data) ? resPE.data : []);
                          
            setPackageEvents(rawPE);

            const resME = await api.get('/event-manager/events?items_per_page=1000'); 
            const rawME = resME.data._embedded?.eventDTOList || 
                          resME.data._embedded?.eventList || 
                          resME.data.content || [];
            
            const filteredMyEvents = rawME
                .map((x: any) => x.data || x)
                .filter((e: any) => Number(e.ownerId) === Number(currentUserId));

            setMyEvents(filteredMyEvents);

        } catch (err) {
            console.error("Eroare la incarcare date:", err);
        } finally { 
            setLoading(false); 
        }
    };

    const add = async (eventId: number) => {
        setProcessing(eventId);
        try {
            const payload = {
                packageId: Number(id),
                eventId: eventId
            };

            let url = getLink(pkg, 'events');
            
            if (url) {
                try {
                    const urlObj = new URL(url);
                    url = urlObj.pathname;
                } catch(e) {}
                if (url.endsWith('/')) url = url.slice(0, -1);
                url = `${url}/${eventId}`;
            } else {
                url = `/event-manager/event-packets/${id}/events/${eventId}`;
            }

            await api.post(url, payload);
            
            const token = localStorage.getItem('token');
            if(token) {
                 const decoded: any = jwtDecode(token);
                 await loadData(Number(decoded.sub));
            }
        } catch (e: any) {
            const msg = e.response?.data?.message || "Eroare la adaugare.";
            alert(`Eroare: ${msg}`);
        } finally {
            setProcessing(null);
        }
    };

    const remove = async (wrapper: any) => {
        const data = wrapper.data || wrapper;
        const uniqueId = data.id || data.eventId; 
        setProcessing(uniqueId);

        try {
            let url = getLink(wrapper, 'self');
            if (!url) url = getLink(data, 'self');
            
            if (url) {
                try {
                    const urlObj = new URL(url);
                    url = urlObj.pathname + urlObj.search;
                } catch(e) { }
            }

            if (!url && data.eventId) {
                url = `/event-manager/event-packets/${id}/events/${data.eventId}`;
            }

            if (url) {
                await api.delete(url);
                const token = localStorage.getItem('token');

                if(token) {
                     const decoded: any = jwtDecode(token);
                     await loadData(Number(decoded.sub));
                }
            } else {
                alert("Nu am putut gasi link-ul de stergere (self).");
            }
        } catch (e: any) {
            const msg = e.response?.data?.message || "Eroare la eliminare.";
            alert(`Eroare: ${msg}`);
        } finally {
            setProcessing(null);
        }
    };

    const available = myEvents.filter(e => {
        const isAlreadyInPackage = packageEvents.some(pe => {
            const peData = pe.data || pe;
            const pkgEventId = peData.eventId || peData.id;
            return Number(pkgEventId) === Number(e.id);
        });
        return !isAlreadyInPackage;
    });

    if (loading) return <div className="container" style={{ marginTop: 50, textAlign: 'center' }}>Se incarca pachetul...</div>;
    if (!pkg) return <div className="container">Pachetul nu a fost gasit.</div>;

    return (
        <div className="container" style={{ marginTop: '20px', maxWidth: '1000px' }}>
             {/*NAVIGARE HATEOAS*/}
             <nav aria-label="breadcrumb" style={{ marginBottom: '20px' }}>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center', fontSize: '0.9rem', color: '#6b7280' }}>
                    <Link to={hateoasLinks.parent.href} style={{ textDecoration: 'none', color: '#4b5563', display: 'flex', alignItems: 'center', gap: '5px' }}>
                        {hateoasLinks.parent.label}
                    </Link>
                    <span>/</span>
                    <span style={{ fontWeight: '600', color: '#111827' }}>{hateoasLinks.self.label}</span>
                </div>
            </nav>

            <div className="card" style={{ borderTop: '5px solid #f59e0b' }}>
                <h2 style={{ marginBottom: '5px' }}>⚙️ Gestionare Pachet: <span style={{ color: '#f59e0b' }}>{pkg.nume}</span></h2>
                <p className="text-muted">Gestioneaza relatiile dintre pachetul tau si evenimente.</p>

                <div style={{ marginTop: '30px', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '30px' }}>
                    


                    <div style={{ background: '#fff1f2', padding: '20px', borderRadius: '12px', border: '1px solid #fda4af' }}>
                        <h3 style={{ color: '#be185d', marginTop: 0, borderBottom: '1px solid #fda4af', paddingBottom: '10px', fontSize: '1.2rem' }}>
                            ✅ Incluse ({packageEvents.length})
                        </h3>
                        {packageEvents.length === 0 && <p className="text-muted" style={{ fontStyle: 'italic' }}>Niciun eveniment in pachet.</p>}
                        
                        <ul style={{ listStyle: 'none', padding: 0 }}>
                            {packageEvents.map((w: any, i) => {
                                const d = w.data || w;
                                const uniqueId = d.id || d.eventId;
                                return (
                                    <li key={i} style={{ background: 'white', padding: '12px', borderRadius: '8px', marginBottom: '8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 1px 2px rgba(0,0,0,0.05)' }}>
                                        <div>
                                            <span style={{ fontWeight: 'bold', display: 'block' }}>{d.eventName || d.nume}</span>
                                            <span style={{ fontSize: '0.8rem', color: '#666' }}>{d.eventLocation || d.locatie}</span>
                                        </div>
                                        <button 
                                            onClick={() => remove(w)} 
                                            className="btn btn-danger btn-sm"
                                            disabled={processing === uniqueId}
                                        >
                                            {processing === uniqueId ? '...' : 'Elimina'}
                                        </button>
                                    </li>
                                )
                            })}
                        </ul>
                    </div>



                    <div style={{ background: '#ecfdf5', padding: '20px', borderRadius: '12px', border: '1px solid #6ee7b7' }}>
                        <h3 style={{ color: '#047857', marginTop: 0, borderBottom: '1px solid #6ee7b7', paddingBottom: '10px', fontSize: '1.2rem' }}>
                            ➕ Disponibile ({available.length})
                        </h3>
                        {available.length === 0 && <p className="text-muted" style={{ fontStyle: 'italic' }}>Nu mai ai alte evenimente de adaugat.</p>}

                        <ul style={{ listStyle: 'none', padding: 0 }}>
                            {available.map((e: any) => {
                                return (
                                    <li key={e.id} style={{ background: 'white', padding: '12px', borderRadius: '8px', marginBottom: '8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 1px 2px rgba(0,0,0,0.05)' }}>
                                        <div>
                                            <span style={{ fontWeight: 'bold', display: 'block' }}>{e.nume}</span>
                                            <span style={{ fontSize: '0.8rem', color: '#666' }}>{e.locatie}</span>
                                        </div>
                                        <button 
                                            onClick={() => add(e.id)} 
                                            className="btn btn-success btn-sm"
                                            disabled={processing === e.id}
                                        >
                                            {processing === e.id ? '...' : 'Adauga'}
                                        </button>
                                    </li>
                                )
                            })}
                        </ul>
                    </div>

                </div>
            </div>
        </div>
    );
};
export default ManagePackage;