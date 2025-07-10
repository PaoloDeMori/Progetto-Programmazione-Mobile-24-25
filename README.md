# APPranzo

## Progetto-Programmazione-Mobile-24-25
- **Francesco Barzanti** (0001078545) - francesco.barzanti@studio.unibo.it
- **Paolo De Mori** (0001071000) - paolo.demori@studio.unibo.it

---

## Scopo del progetto
L’obiettivo di **APPranzo** è offrire un’esperienza completa e coinvolgente per chi desidera scoprire e valorizzare la cucina locale: dall’esplorazione di ristoranti consigliati in base alle proprie preferenze fino alla creazione di recensioni con foto e al salvataggio dei propri locali preferiti. Grazie a mappe interattive, funzionalità social di condivisione e amicizie, e un sistema di gamification con badge e obiettivi da sbloccare, l’app guida l’utente passo dopo passo alla scoperta dei migliori locali.

---

## Struttura del Progetto
"Appranzo" è un applicativo che ha necessità di un backend per poter svolgere le proprie funzionalità. Infatti, oltre all'applicazione per Android, che è il punto cardine di questo progetto, abbiamo dovuto realizzare anche un backend. Per la precisione, abbiamo realizzato una **REST API** che comunica con l'applicativo tramite protocollo HTTP e dati in formato JSON.

---

## Contenuto della Repository
In questa repository si trovano diverse cartelle:
- `Apk`: Una cartella contenente un file `.apk` dell'applicazione per Android da poter eseguire su un dispositivo mobile. Per la comunicazione con il backend in localhost (ad esempio un dispositivo connesso tramite ADB)
- `ApplicazioneKotlinCompose`: Contiene il codice sorgente dell'applicazione in Kotlin per Android.
- `Backend`: Contiene il fat JAR eseguibile del backend (scritto in Kotlin), un file di configurazione d'esempio e il codice sorgente del backend stesso.
- `Mysql-Db`: Contiene il DDL per creare la struttura del database utilizzato dal backend per la memorizzazione dei dati.

---

## Funzionalità Principali

### Appranzo
- **Login:**
  - Login e Registrazione sulla piattaforma, con accesso semplificato grazie a token JWT.
- **Ristoranti:**
  - Visualizzazione dei locali filtrati per vicinanza e miglior valutazione.
  - Ricerca dei locali per nome o per categorie.
  - Salvataggio dei locali preferiti.
- **Mappa:**
  - Visualizzazione dei locali su una mappa (interna all'app o su un'altra applicazione).
  - Visualizzazione sulla mappa dei locali presenti nelle vicinanze dell'utente.
- **Amicizia:**
  - Inviare e ricevere richieste di amicizia.
  - Visualizzare la lista dei propri amici, con possibilità di rimuoverli.
- **Badges:**
  - Possibilità di sfidare se stessi e gli amici tramite badge ottenibili con la pubblicazione di recensioni.
- **Recensioni:**
  - Pubblicare recensioni, dare voti in tre categorie diverse, lasciare un commento e aggiungere foto.
  - Visualizzare lo storico delle proprie recensioni.
  - Visualizzare tutte le recensioni pubblicate per ogni locale.
- **Profilo:**
  - Possibilità di vedere le informazioni sul proprio profilo, compresi i punti ottenuti tramite le recensioni.

### Backend
- Gestione dei dati (ricezione, memorizzazione, invio) per abilitare le funzionalità dell'app.
- Salvataggio dei dati su un database MySQL locale.
- Riempimento automatizzato del database con i locali delle città tramite query a OpenStreetMap, fornendo solo la città di riferimento.
- Personalizzazione dell'esecuzione del Backend tramite riga di comando.

---

## Avviare il Backend

1.  **Creazione Database:**
    Utilizzare un qualsiasi strumento di gestione per database MySQL (es. phpMyAdmin) per creare la struttura del database usando il file DDL fornito.

2.  **Avvio del Backend:**
    Avviare il backend da riga di comando con:
    ```bash
    java -jar apPranzo-all.jar
    ```
    a. Per visualizzare tutte le opzioni di avvio, usare il flag `--help`.
        - `--port, -p [8080]`: Sceglie la porta su cui il server si mette in ascolto.
        - `--host, -ho [0.0.0.0]`: Sceglie l'indirizzo su cui il server si mette in ascolto.
        - `--scraping, -scr [false]`: Esegue il riempimento del database con i locali di una determinata città.
        - `--city, -ci [Cesena]`: Seleziona la città per lo scraping (richiede il flag `-scr`).
        - `--region, -re [Emilia-Romagna]`: Seleziona la regione della città per lo scraping (richiede il flag `-scr`).
        - `--admin, -ad [false]`: Crea un utente di default (user: `admin`, pwd: `admin`) se non esiste.
        - `--db-name [ApPranzo]`: Permette di selezionare il nome del database.
        - `--db-user [root]`: Permette di selezionare l'utente del database.
        - `--db-password []`: Permette di selezionare la password del database.
        - `--photo-path, -pp [uploads]`: Permette di scegliere la cartella in cui salvare le foto.

    b. Se non si specificano i valori da riga di comando, il programma cercherà nella stessa directory un file `.jwt.env.json` con la seguente struttura per caricare le impostazioni:
    ```json
    {
      "secret-access": "...",
      "secret-refresh": "...",
      "audience": "...",
      "expirationAccessTime": 900000,
      "expirationRefreshTime": 604800000,
      "photoStoragePath": "..."
    }
    ```

    c. In assenza di alternative, verranno usati i seguenti valori di default:
        - `secret-access`: "secret-access"
        - `secret-refresh`: "secret-refresh"
        - `audience`: "AppsUsers"
        - `expirationAccessTime`: 900000
        - `expirationRefreshTime`: 60480000
        - `photoStoragePath`: "uploads"
        - `db-user`: "root"
        - `db-password`: ""
        - `db-name`: "ApPranzo"
        - `city`: "Cesena"
        - `region`: "Emilia Romagna"

    Se l'avvio è andato a buon fine, il terminale mostrerà i seguenti messaggi:
    ```
    2025-07-10 16:01:04.492 [main] INFO  io.ktor.server.Application - Autoreload is disabled because the development mode is off.
    2025-07-10 16:01:04.646 [main] INFO  [Koin] - Started 18 definitions in 0.41614 ms
    Verifico la connessione al DB...
    2025-07-10 16:01:05.633 [main] INFO  io.ktor.server.Application - Application started in 1.346 seconds.
    2025-07-10 16:01:05.799 [DefaultDispatcher-worker-1] INFO  io.ktor.server.Application - Responding at [http://0.0.0.0:8080](http://0.0.0.0:8080)
    ```

---

## Avvio dell'App
Al primo avvio si presenta la schermata di login, da cui è possibile registrarsi. Dagli avvii successivi, l'accesso sarà automatico grazie all'uso dei token JWT (se il token non è scaduto). Una volta effettuato l'accesso, l'applicazione presenta una Top Bar e una Bottom Bar che permettono la navigazione tra tutte le schermate.
