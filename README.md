# Event Management Application

Această aplicație reprezintă o soluție distribuită pentru gestionarea evenimentelor și a rezervărilor de bilete, implementată utilizând o arhitectură bazată pe microservicii. 

## Arhitectura Sistemului

Sistemul este compus din servicii autonome, decuplate, care comunică între ele prin protocoale standardizate (REST și gRPC). Arhitectura pune accent pe separarea responsabilităților și utilizarea tehnologiei de stocare adecvate pentru fiecare tip de dată.

### Diagrama de Comunicare
`Frontend` -> `API Gateway` -> `Microservicii` <-> `Persistență (MariaDB & MongoDB)`

### Componente Software

1.  **Gateway Service**
    * Punctul unic de intrare în sistem (API Gateway).
    * Rutarea cererilor HTTP către microserviciile interne.

2.  **IDM Service (Identity Management)**
    * Gestionează autentificarea și autorizarea utilizatorilor.
    * Emite și validează token-uri JWT.
    * Comunică prin **gRPC** cu celelalte servicii pentru verificarea rapidă a permisiunilor.

3.  **Event Service**
    * Gestionează logica de business pentru organizatori și evenimente.
    * Interacționează cu baza de date relațională (MariaDB) pentru a asigura integritatea datelor tranzacționale.

4.  **Client Service**
    * Gestionează profilele utilizatorilor de tip client.
    * Interacționează cu baza de date orientată pe documente (MongoDB).

## Persistența Datelor

Proiectul utilizează două sisteme de baze de date distincte, alese în funcție de natura datelor gestionate:

1.  **MariaDB (SQL)**
    * Utilizată pentru datele care necesită o structură rigidă și relații complexe.
    * Stochează: **Evenimente**, **Pachete promoționale** și **Bilete**.
    * Asigură consistența tranzacțională (ACID) pentru procesul de emitere a biletelor.

2.  **MongoDB (NoSQL)**
    * Utilizată pentru date cu structură flexibilă.
    * Stochează: **Datele Clienților** și detaliile profilelor acestora.
    * Permite o scalare rapidă și modificarea structurii profilului de client fără migrări complexe de schemă.

## Tehnologii Utilizate

### Backend
* **Java 21** & **Spring Boot**
* **Spring Data JPA (Hibernate)** - Pentru interacțiunea cu MariaDB.
* **Spring Data MongoDB** - Pentru interacțiunea cu MongoDB.
* **gRPC & Protocol Buffers** - Comunicare eficientă inter-servicii.
* **REST API** - Expunere resurse către frontend.

### Frontend
* **React.js** cu **TypeScript** - Interfață Single Page Application (SPA).
* **Vite** - Build tool.
* **Axios** - Client HTTP.

### Infrastructură
* **Docker & Docker Compose** - Containerizarea serviciilor backend și a bazelor de date.
* **MariaDB & MongoDB** - Containere pentru persistență.

## Configurare și Rulare

Backend-ul și bazele de date rulează în containere Docker, în timp ce Frontend-ul este pornit local pentru dezvoltare.

### Pre-rechizite
* Docker & Docker Compose.
* Node.js și npm (pentru rularea frontend-ului).
* Git.

### Pași de instalare:

1.  **Clonare repository**
    ```bash
    git clone [https://github.com/cosmin-web/Event-Management-Application.git](https://github.com/cosmin-web/Event-Management-Application.git)
    cd Event-Management-Application
    ```

2.  **Pornire Backend și Baze de Date (Docker)**
    În directorul rădăcină (unde se află `docker-compose.yml`), rulați:
    ```bash
    docker-compose up --build
    ```
    Această comandă va porni:
    * Containerul MariaDB (port 3306)
    * Containerul MongoDB (port 27017)
    * Microserviciile Backend (Gateway, IDM, Event, Client)

3.  **Pornire Frontend (Local)**
    Deschideți un terminal nou, navigați în folderul frontend și instalați dependențele:
    ```bash
    cd frontend
    npm install
    npm run dev
    ```

4.  **Accesare**
    * Interfața Web: `http://localhost:5173`
    * API Gateway: `http://localhost:8080`

## Capturi de Ecran

### 1. Autentificare
<img width="1919" height="914" alt="Screenshot From 2026-01-22 21-56-54" src="https://github.com/user-attachments/assets/a7c2a1d9-044c-4a3e-a490-fecb4a4b30b5" />

### 2. Lista Evenimente
<img width="1919" height="914" alt="Screenshot From 2026-01-22 21-59-02" src="https://github.com/user-attachments/assets/f15747aa-409c-4ef7-83ac-48b8626f0f6b" />


## Documentație API

Specificațiile OpenAPI sunt disponibile la:

* **Event Service:** `http://localhost:8083/swagger-ui.html`
* **Client Service:** `http://localhost:8082/swagger-ui.html`
* **IDM Service:** `http://localhost:8081/swagger-ui.html`

## Funcționalități Cheie

* **Stocare Hibridă:** Combinarea SQL și NoSQL pentru performanță optimă.
* **Securitate:** Autentificare centralizată și validare token prin gRPC.
* **Scalabilitate:** Arhitectura de microservicii permite scalarea independentă a modulelor.

---
© 2026 Cosmin. Proiect educațional POS.
