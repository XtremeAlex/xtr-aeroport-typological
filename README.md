> Stato: deprecato dal 29/09/2026. Questo progetto non è più mantenuto.
> È confluito in `xtr-aeroport-api-spring`, l'API unica della suite (non ancora pubblicata su GitHub), insieme a `xtr-aeroport-ms`.
> Il codice resta qui per chi vuole consultarlo, ma non riceverà più correzioni, aggiornamenti di sicurezza o nuove release.

<a name="readme-top"></a>

<div align="center">
  <img src="_assets/images/banner-dark.png" alt="Aeroport Typological" width="100%">
  <br /><br />
  <img src="_assets/images/logo.png" width="300" alt="Logo">
</div>

# xtr-aeroport-typology

Il microservizio che dava accesso ai dati tipologici su aeroporti e rotte aeree di tutto il mondo.

<details>
  <summary>Sommario</summary>
  <ol>
    <li><a href="#perché-esiste">Perché esiste</a></li>
    <li><a href="#la-suite">La suite</a></li>
    <li><a href="#stack-tecnologico">Stack tecnologico</a></li>
    <li><a href="#per-iniziare">Per iniziare</a></li>
    <li><a href="#play--test">Play &amp; Test</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#come-contribuire">Come contribuire</a></li>
    <li><a href="#licenza">Licenza</a></li>
    <li><a href="#contatti">Contatti</a></li>
    <li><a href="#ringraziamenti">Ringraziamenti</a></li>
  </ol>
</details>

## Perché esiste

È un progetto personale nato per provare tecnologie e framework recenti su un caso concreto. L'obiettivo era un set di API affidabili per consultare nel dettaglio aeroporti e rotte aeree, con un occhio di riguardo alla compilazione nativa con GraalVM.

Perché l'ho deprecato: `xtr-aeroport-ms` chiamava questo servizio via Feign solo per avere le tipologie. Erano due JVM, una chiamata di rete in più e un punto in più in cui le cose potevano rompersi. Su un Raspberry Pi è un costo che non ha senso. Così i due servizi sono diventati uno, `xtr-aeroport-api-spring`.

## La suite

| Modulo | A cosa serve | Stato |
|---|---|---|
| `xtr-aeroport-api-spring` | API unica per aeroporti, tipologie, paesi e messaggi EDIFACT (non ancora pubblicata su GitHub) | Attivo |
| `xtr-aeroport-api-quarkus` | Porting della stessa API su Quarkus (non ancora pubblicato su GitHub) | Sperimentale |
| `xtr-aeroport-edifact-spring-web` | Console web EDIFACT, ha preso il posto di `xtr-aeroport-web-java` (non ancora pubblicata su GitHub) | Attivo |
| [`xtr-aeroport-batch`](https://github.com/XtremeAlex/xtr-aeroport-batch) | Import massivo dei dati | Attivo, offline |
| [`xtr-aeroport-common-lib`](https://github.com/XtremeAlex/xtr-aeroport-common-lib) | Libreria condivisa | Legacy |
| [`xtr-aeroport-ms`](https://github.com/XtremeAlex/xtr-aeroport-ms) | Microservizio di ricerca aeroporti | Deprecato |
| [`xtr-aeroport-typology`](https://github.com/XtremeAlex/xtr-aeroport-typology) | Servizio dati tipologici (questo modulo) | Deprecato |
| [`xtr-aeroport-web-java`](https://github.com/XtremeAlex/xtr-aeroport-web-java) | Frontend web | Deprecato |

## Stack tecnologico

- Java 17 (GraalVM)
- Spring Boot 3.2.1
- Maven
- Gira su Linux, macOS e Windows

## Per iniziare
Si compila con Maven, su Spring Boot 3 e Java 17, e si avvia senza problemi in locale.

### Cosa serve

- Git (>= 2.43)
- Java OJDK (GraalVM versione 17)
- Maven (Apache Maven >= 3.9.6)

### Coordinate del progetto

| Proprietà | Valore |
|---|---|
| artifactId | `typological` |
| version | `1.2.0` |
| main class | `com.xtremealex.aeroport.AeroportApplication` |
| porta | `8080` |

### Clonare e compilare

1. Clona il repository:
   ```bash
   git clone https://github.com/XtremeAlex/xtr-aeroport-typology.git
   cd xtr-aeroport-typology
   ```

2. Compila con Maven:
   ```bash
   mvn clean package -DskipTests
   ```
   <img src="_assets/images/mvn-build.png" alt="Build Maven"/>

3. Avvia l'applicazione:
   ```bash
   java -jar ./target/aeroport-*.jar
   ```
   <img src="_assets/images/run-by-graal-jdk17.png" alt="Avvio con GraalVM JDK 17"/>

### Build nativa (GraalVM)

**1. Generare i metadati per la native image**

Prima di qualsiasi compilazione nativa bisogna lanciare il `native-image-agent`, altrimenti la reflection dà problemi. L'agent esegue il jar sulla JVM e genera i metadati necessari:

```bash
java -agentlib:native-image-agent=config-output-dir=./src/main/resources/META-INF/native-image -jar ./target/*.jar com.xtremealex.aeroport.AeroportApplication
```

<img src="_assets/images/run-agentlib.png" alt="native-image-agent" />

L'agent produce diversi file `.json`, ognuno dedicato a un aspetto del codice da compilare. Se stanno in `resources/META-INF/native-image` non serve aggiungere `buildArgs`, perché è lì che vengono cercati di default. Volendo, si possono comunque dichiarare a mano:

```
<buildArg>-H:ReflectionConfigurationFiles=configs/native-image/reflect-config.json</buildArg>
<buildArg>-H:JNIConfigurationFiles=configs/native-image/jni-config.json</buildArg>
<buildArg>-H:DynamicProxyConfigurationFiles=configs/native-image/proxy-config.json</buildArg>
<buildArg>-H:ResourceConfigurationFiles=configs/native-image/resource-config.json</buildArg>
<buildArg>-H:SerializationConfigurationFiles=configs/native-image/serialization-config.json</buildArg>
```

**2. Le opzioni principali in `buildArgs` (nel pom)**

Cosa fa ciascuna:

- `--verbose`: messaggi dettagliati durante la compilazione, utile per il debug.
- `-Dspring.aot.enabled=true`: attiva l'ottimizzazione Ahead-of-Time di Spring, per un avvio più rapido e meno memoria.
- `-H:TraceClassInitialization=true`: traccia l'inizializzazione delle classi, per scovare quelle che creano problemi.
- `-H:+ReportExceptionStackTraces`: stampa lo stack trace delle eccezioni quando la compilazione fallisce.
- `-H:Name=aeroport`: il nome dell'eseguibile finale.
- `-H:DashboardDump=aeroport-dump` / `-H:+DashboardAll`: dati per il dashboard di GraalVM.
- `--initialize-at-build-time=org.slf4j.LoggerFactory,ch.qos.logback,...`: classi e pacchetti da inizializzare a build time.
- `--initialize-at-run-time=framework`: framework personalizzati da inizializzare a runtime.
- `-Dspring.graal.remove-unused-autoconfig=true`: toglie le autoconfigurazioni non usate, per un'immagine più piccola.
- `-Dspring.graal.remove-yaml-support=true`: toglie il supporto YAML, per ridurre ancora l'immagine.

**3. Compilare l'immagine nativa**

```bash
mvn package -DskipTests -Pnative
```

<img src="_assets/images/native-mvn-build.png" alt="Build nativa" />

Il binario esce in `./target/aeroport` (su Windows `aeroport.exe`).

<img src="_assets/images/native-macos-result-build.png" alt="Risultato build" />

**4. Avviare l'applicazione nativa**

```bash
./target/aeroport
```

<img src="_assets/images/native-run-app.png" alt="Avvio applicazione nativa" />

In nativo il tempo di avvio si dimezza (`4.22s`), anche se all'avvio c'è l'init che importa i dati JSON in H2. Togliendo l'init la differenza diventa ancora più netta, e in cloud si sente:

- JVM senza init: `1.507s`

  <img src="_assets/images/run-no-init-by-graal-jdk17.png" alt="JVM no-init" />

- Nativo senza init: `0.151s`, circa 10 volte più veloce e con meno risorse.

  <img src="_assets/images/native-run-no-init-by-graal-jdk17.png" alt="Nativo no-init" />

**Tutti i comandi in fila**

```bash
mvn clean
mvn package -DskipTests
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar ./target/*.jar com.xtremealex.aeroport.AeroportApplication
mvn package -DskipTests -Pnative
./target/aeroport
```

### Build Docker

```bash
mvn package -DskipTests -Pnative && mvn package -DskipTests -Pdocker-m1-arm
docker run -p 8080:8080 artifactory.io/k8s-test/namespace/com.xtremealex/aeroport:0.2.0
```

<img src="_assets/images/native-docker-arm-build.png" alt="Build Docker ARM" />
<img src="_assets/images/native-run-docker-arm.png" alt="Run Docker ARM" />

## Play & Test

**Controller**

1. `getAirportsBy`
   ```java
   @GetMapping("/getAirportsBy")
   public ResponseEntity<?> getAirportsBy(@RequestParam(required = false) Set<String> types,
                                          @RequestParam(required = false) String isoCountry,
                                          @RequestParam(required = false) String name,
                                          @RequestParam(defaultValue = "0") int pageNumber,
                                          @RequestParam(defaultValue = "12") int pageSize,
                                          @RequestParam(required = false) String sortField,
                                          @RequestParam(defaultValue = "ASC") String sortDir) {
       // code...
   }
   ```
   ```
   URL: http://localhost:8080/xtr-aeroport/getAirportsBy?pageNumber=0&pageSize=4&sortField=name&types=1,2,3,4,5,6,7,8,9
   ```

2. `searchAirports`
   ```java
   @PostMapping("/searchAirports")
   public ResponseEntity<?> searchAirports(@RequestBody AirportSearchRequest searchRequest) {
       // code...
   }
   ```
   ```
   URL: http://localhost:8080/xtr-aeroport/searchAirports
   ```

## Roadmap

Chiusa con la deprecazione. Questo è quello che è stato fatto:

- [x] Verticale SearchAirport
- [x] Verticale SearchAirportType
- [x] ModelMapper sostituito con MapStruct
- [x] Compilazione nativa con GraalVM

Le vecchie issue restano consultabili [qui](https://github.com/XtremeAlex/xtr-aeroport-typology/issues).

## Come contribuire

Il progetto è deprecato, quindi aprire Pull Request qui ha poco senso. Se vuoi contribuire alla suite, il posto giusto è `xtr-aeroport-api-spring`. Per chi vuole comunque partire da qui con un fork, il giro è quello classico:

1. fai un fork del progetto;
2. crea un branch per la tua modifica (`git checkout -b feature/nome-feature`);
3. fai commit (`git commit -m "Aggiunge nome-feature"`);
4. fai push del branch (`git push origin feature/nome-feature`).

## Licenza
Doppia licenza: **GNU AGPL-3.0** (vedi [`LICENSE`](LICENSE)) per l'uso open source, e **licenza commerciale** per l'uso dentro prodotti proprietari (vedi [`COMMERCIAL-LICENSE.md`](COMMERCIAL-LICENSE.md)).

## Contatti

Andrei Alexandru Dabija (XtremeAlex) · [alexdabi92@gmail.com](mailto:alexdabi92@gmail.com) · [2ad.bubume.it](https://2ad.bubume.it/) · [LinkedIn](https://www.linkedin.com/in/andrei-alexandru-dabija/) · [github.com/XtremeAlex](https://github.com/XtremeAlex)

## Ringraziamenti

- [Spring Boot](https://spring.io/projects/spring-boot)
- [GraalVM](https://www.graalvm.org/) per la compilazione nativa
- [Best-README-Template](https://github.com/othneildrew/Best-README-Template), da cui ho preso spunto per la struttura

<p align="right">(<a href="#readme-top">torna su</a>)</p>
