# NotaBene

Progetto di Ingegneria del Software - Informatica per il Management a.a. 2024/2025

## Prerequisiti

- Java 8 o superiore
- Maven 3.6+
- Browser web con JavaScript abilitato

## Avvio dell'Applicazione

### 1. Avvio del Code Server GWT (Frontend)
```bash
cd gwt-notabene-maven
mvn -U -e gwt:codeserver -pl notabene-client -am
```

### 2. Avvio del Server Backend (nuovo terminale)
```bash
cd gwt-notabene-maven
mvn -U jetty:run -pl notabene-server -am -Denv=dev
```

### 3. Accesso
Aprire il browser su: `http://localhost:8080/`

**Utenti attivi**:
- username: mdinelli , password: 1234
- username: fverdi , password: 4321

## Sviluppo

- **Modifiche frontend**: Refresh del browser per aggiornare
- **Modifiche backend**: Ricompilare con:
  ```bash
  mvn -U compile -pl *-server -am
  ```

## Gestione Processi

### Windows
```cmd
netstat -ano | findstr :8080
taskkill /PID <PID_NUMBER> /F
```

### macOS/Linux
```bash
lsof -ti:8080
kill -9 $(lsof -ti:8080)
```
