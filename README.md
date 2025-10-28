# 4D_Viewer

Ein leistungsstarkes, intuitives und erweiterbares Tool zur **Visualisierung, Analyse und Bearbeitung von vierdimensionalen Bilddaten**.
Entwickelt in **Java**, speziell für **mikroskopische und biologische Anwendungen** (z. B. Embryonenanalyse), bietet der 4D Viewer eine moderne Oberfläche und flexible Bildmanipulation.

## 🚀 Funktionsübersicht

- **Visualisierung von 4D-Bildern** (Zeit + Raum)
- **Interaktive Steuerung**: Bewegungen mit Maus und Tastatur
- **Bilder Bearbeitung**: Spiegelung, Rotation
- **Markierungen**: Ellipse, Rechteck (Farbe frei wählbar)
- **Unterstützte Formate**: GIF, JPEG, PNG, TIFF
- **Internationalisierung**: Deutsch und Englisch
- **Projektdaten**:\
  *Verzeichnis*: {Benutzerverzeichnis}/.4D_Viewer/{Bilderverzeichnis}/
    - *config.json* – speichert projektbezogene Konfigurationen (Dateityp, Zeit-Trenner, Ebenen-Trenner,
      Zeit-Einheit, Ebenen-Einheit, Bild-Spiegelung_X, Bild-Spiegelung_Y, Bild-Rotation)
    - *markers.json* – speichert projektbezogene Markierungen von der UI
    - *snapshots/ (Ordner)* – Ablage für erzeugte Momentaufnahmen
- **Einstellungen**:\
  *Verzeichnis*: {Benutzerverzeichnis}/.4D_Viewer/
    - *history.txt* – speichert die zuletzt verwendeten Bilderverzeichnisse
    - *settings.json* – speichert die Benutzereinstellungen (Sprache, Theme, Schriftgröße, ...)

## 📂 Projektstruktur
4d_viewer/\
├── *images/* – Ablage für Test-Bilder\
├── *src/* – Source-Code des Projekts\
├── *pom.xml* – Maven Build-Konfiguration\
├── *README.md* – Projektdokumentation\
└── *.gitignore* – Git Ignore-Regeln

## 🔧 Entwicklung

### Technologien

- IDE: IntelliJ IDEA (Community / Ultimate)
- Sprache: Java 20 (OpenJDK)
- Build-Tool: Apache Maven 3.9.11

### Abhängigkeiten (Auszug)
| Bibliothek                                       | Version | Zweck / Beschreibung                                         |
|--------------------------------------------------|---------|--------------------------------------------------------------|
| com.fasterxml.jackson.core:jackson-annotations   | 2.20    | Annotationen zur Steuerung von JSON-(De-)Serialisierung      |
| tools.jackson.core:jackson-core                  | 3.0.1   | JSON Parser/Generator                                        |
| tools.jackson.core:jackson-databinding           | 3.0.1   | ObjectMapper für POJO ↔ JSON (inkl. Serializer/Deserializer) |
| com.formdev:flatlaf                              | 3.6     | Modernes Look & Feel für Swing                               |
| com.formdev:flatlaf-extras                       | 3.6     | Erweiterungen für FlatLaf                                    |
| javax.swing                                      | -       | Standard-GUI-Toolkit (Java SE)                               |
| org.jetbrains:annotations                        | 26.0.2  | Annotationen für statische Analysen                          |
| org.junit.jupiter:junit-jupiter                  | 6.0.0   | Test-Framework (JUnit 5)                                     |


### Installation
1. **Repository klonen**
   ```bash
   git clone https://gitlab.git.nrw/okaiser1/4d_viewer.git
   cd 4d_viewer
   ```
2. **Abhängigkeiten installieren**\
   Eine ausführbare JAR-Datei wird im Ordner *target/* erzeugt.
   ```bash
   mvn clean install && rm target/original-*.jar
   ```
3. **Projekt starten**\
   Falls in der pom.xml Datei konfiguriert:
   ```bash
   mvn exec:java
   ```

   Alternativ über die erzeugte JAR:
   ```bash
   java -jar target/*.jar
   ```

### Nützliche Hinweise
- Java-Version prüfen (Sollte Java 20 melden):
  ```bash
  java -version
  ```

- Typische Maven-Befehle:
    - Kompilieren: `mvn compile`
    - Tests: `mvn test`
- IntelliJ:
    - JDK 20 als Projekt-SDK wählen, dann App über Run starten.

## 📗 Dokumentation

- [Benutzeranleitung](LINK-ZUR-BENUTZERANLEITUNG) **[Link hinzufügen]**
- [API-Dokumentation](LINK-ZUR-API-DOKUMENTATION) **[Link hinzufügen]**

## 📸 Screenshots

![Beispielbild](images/example.png) **[Demo-GIFs, Screenshots und Beispiele hinzufügen]**

## 🗺️ Roadmap

- [x] Umstieg auf FlatLaf (Neues Erscheinungsbild)
- [x] Bildbearbeitung (Spiegelung und Rotation) hinzugefügt
- [x] Erweiterung um Menüleiste
- [x] Erweiterung um Shortcuts
- [x] Internationalisierung (DE/EN)
- [x] Optimierung der Ladezeit und des Ladedialogs von Bildern
- [x] Modularisierung des Codes
- [ ] Erweiterung um Reiter "Markierungen"
- [ ] Neue Features beim Reiter "Bearbeiten"
    - [ ] Helligkeit-, Kontrast-Regler
    - [ ] Zoom und Crop-Funktion
- [ ] Neue Features beim Reiter "Navigieren"
- [ ] Weitere Performance-Optimierungen

## 📊 Projektstatus

Das Projekt befindet sich aktuell in **aktiver Entwicklung**.
Ziel ist es, eine stabile, nutzerfreundliche und erweiterbare Plattform zur 4D-Visualisierung zu schaffen.

## 🤝 Beitragende

Ein großes Dankeschön an alle Entwickler, Tester und Nutzer, die dieses Projekt mitgestalten!
>„Ich glaube, das Projekt macht dir genauso viel Spaß wie mir – und wir sind auf dem richtigen Weg.“\
> ~ Olaf Bossinger

## 🔐 Lizenz

Dieses Projekt ist unter der **[Lizenzname hinzufügen]** lizenziert. \
Details siehe [Lizenzdate](LINK-ZUR-LIZENZ) **[Link hinzufügen]**