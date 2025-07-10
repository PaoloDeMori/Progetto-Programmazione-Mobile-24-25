#APPranzo

## Progetto-Programmazione-Mobile-24-25
-Francesco Barzanti (0001078545) - francesco.barzanti@studio.unibo.it
-Paolo De Mori (0001071000)- paolo.demori@studio.unibo.it

## Scopo del progetto
Ci siamo posti come obbiettivo quello di realizzare una applicazione per smartphone funzionante, che permettesse la visualizzazione,possibilità di recensire e salvare ristoranti e altri locali in generale.

## Struttura del Progetto
"Appranzo" è un applicativo che ha necessità di un backend per poter svolgere le proprie funzionalità, infatti oltre all'applicazione per android, che è il punto cardine di questo progetto, abbiamo dovuto realizzare anche un backend, per la precisione abbiamo realizzato una RESTAPI, che quindi comunica con l'applicativo tramite protocollo http e dati in formato json.

## In questa repository si trovano diverse cartelle:
    - Apk: Una cartella contenente un Apk dell'applicazione per Android da poter eseguire su un dispositivo mobile Android. Per la comunicazione con il backend in localhost (ad esempio, su un emulatore o un dispositivo connesso tramite ADB), l'app         sfrutta le funzionalità di ADB per reindirizzare il traffico verso l'host locale
    - ApplicazioneKotlinCompose: Contiene il codice sorgente dell'applicazione in kotlin per Android
    - Backend: Contiene il fat jar eseguibile del backend scritto anch'esso in kotlin, un file di configurazione non necessario ma utile a scopo di esempio e il codice sorgente del backend stesso
    - Mysql-Db: Contiene il ddl per creare la struttura del database utilizzato dal backend per la memorizzazione dei dati

## Funzionalita Principali:
    ## Appranzo : 
      #Login:
      - Login e Registrazione sulla piattaforma, con accesso semplificato grazie a token-jwt
      #Ristoranti:
      - Visualizzazione dei locali filtrati per Vicinanza dall'utente e miglior valutazione
      - Ricerca dei locali per nome o per Categorie
      - Salvataggio dei Locali preferiti
      #Mappa:
      - Visualizzazione su mappa(all'interno dell'app o su altra applicazione) dei locali
      - Visualizzazione su mappa dei locali presenti nelle vicinanze del utente 
      #Amicizia:
      -Inviare e ricevere richieste di amicizia
      -visualizzare la lista dei propri amici, con possibilità di rimuoverli in qualsiasi momento
      #Badges:
      -Possibilità di sfidare se stessi e i propri amici tramite badges ottenibili con la pubblicazione di recensioni
      #Recensioni:
      -Posiiblità di pubblicare recensioni, dare voti in tre categorie diverse, lasciare un commento e foto
      -Visualizzare lo storico delle proprie recensioni
      -Visualizzare tutte le recensioni pubblicate per ogni locale
      #Profilo
      -Possibilità di vedere le informazioni sul proprio profilo, compresi i punti ottenuti tramite le recensioni

    ## Backend : 
      #Possibilità di ricevere, memorizzare e inviare i dati per ottenere le funzionalità sopra elencate
      #Salvataggio in locale tramite database mysql dei dati necessari per l'applicazione
      #Riempimento automatizzato del database con i locali delle città tramite apposite query open-street-map, dando in input solamente città di riferimento
      #Personalizzazione dell'esecuzione del Backend tramite riga di comando

    ## Avviare il Backend:
      1)Creazione database:
        Utilizzare qualsiasi framework per la gestione di database mysql per popolarlo con il ddl, noi abbiamo utilizzato phpmyadmin
      2) Avvio del Backend tramite riga di comando con "java -jar apPranzo-all.jar" 
        a) In caso si lanci il comando seguito da "--help", verranno illustrati tutti i possibili flag per modificare l'esecuzione del backend
              --port, -p [8080] -> permette di scegliere su quale porta mettere in ascolto il server
              --host, -ho [0.0.0.0] -> permette di scegliere su quale indirizzo mettere in ascolto il server
              --scraping, -scr [false] -> Esegue il riempimento del database con i locali  di una determinatà città 
              --city, -ci [Cesena] ->  Permette di scegliere i locali di quale città si vuole riempire il db(se presente flag -scr)
              --region, -re [Emilia-Romagna] -> Seleziona la regione della città di cui si riempie il db tramite scraping (se presente flag -scr)
              --admin, -ad [false] -> Crea un utente user:admin,pwd:admin di default nel db se non esiste 
              --db-name [ApPranzo] -> Permette di selezionare il nome del  db
              --db-user [root] -> Permette di selezionare il nome del proprio utente nel db
              --db-password [] -> Permette di selezionare la propria password del db
              --photo-path, -pp [uploads] ->Permette di scegleire la Cartella in cui salvare le foto
        b) In caso non si specifichino i valori tramite riga di comando, se è presente nella stessa directory un file denominato ".jwt.env.json" , con i seguenti valori
               "" "secret-access": "...",
                  "secret-refresh": "...",
                  "audience": "...",
                  "expirationAccessTime": ---,
                  "expirationRefreshTime": ---,
                  "photoStoragePath": "..." "" 
            Il programma utilizzerà questi per la propria impostazione
        c) Altrimenti sono presenti dei valori di default quali :
            secretAccess = "secret-access",
            secretRefresh = "secret-refresh",
            audience = "AppsUsers",
            expirationAccessTime = 900000,
            expirationRefreshTime = 60480000,
            photoStoragePath = photoPath
            db-name = "root"
            db-password = ""
            db-name = ApPranzo
            city = "Cesena"
            region = "Emilia Romagna"

            dopo l'avvio in caso tutto sia andato per il verso giusto si vedranno queste righe sul terminale, (in caso si sia effetuato lo scraping vengono intermezzate da informazioni sui locali trovati) 
            2025-07-10 16:01:04.492 [main] INFO  io.ktor.server.Application - Autoreload is disabled because the development mode is off.
            2025-07-10 16:01:04.646 [main] INFO  [Koin] - Started 18 definitions in 0.41614 ms
            Verifico la connessione al DB...
            2025-07-10 16:01:05.633 [main] INFO  io.ktor.server.Application - Application started in 1.346 seconds.
            2025-07-10 16:01:05.799 [DefaultDispatcher-worker-1] INFO  io.ktor.server.Application - Responding at http://0.0.0.0:8080

    

## Avvio dell'app 
  Al primo avvio si presenta la schermata di login, da cui è possibile registrarsi.
  Dal secondo avvio in poi in caso non si acceda dopo troppo tempo, tramite l'utilizzo dei token jwt si verrà loggati automaticamente
  Una volta fatto l'accesso, l'applicazione si presenta con una top bar e una bottom bar che permettono la navigazione tra tutte le schermate sopra elencate
    


      




