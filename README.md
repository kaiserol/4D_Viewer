# 4D_Viewer

Ein leistungsstarkes, intuitives und erweiterbares Tool zur **Visualisierung, Analyse und Bearbeitung von vierdimensionalen Bilddaten**.
Entwickelt in **Java**, speziell für **mikroskopische und biologische Anwendungen** (z. B. Embryonenanalyse), bietet der 4D Viewer eine moderne Oberfläche und flexible Bildmanipulation.

## 🚀 Funktionsübersicht

- **Visualisierung von 4D-Bildern** (Zeit + Raum)
- **Unterstützte Formate**: GIF, JPEG, PNG, TIFF
- **Interaktive Steuerung**: Bewegungen mit Maus und Tastatur möglich
- **Markierungen**: Ellipse, Rechteck (Farbe frei wählbar)
- **Bilder Bearbeitung**: Spiegelung, Rotation
- **Internationalisierung**: Deutsch und Englisch
- **Snapshots**: Momentaufnahmen von bearbeiteten Bildern
- **Konfigurierbare Bilderverzeichnisse** über config.cfg

## 📂 Projektstruktur
4d_viewer/\
├── images/ >>> **Test-Bilder für den 4D Viewer**\
├── src/ >>> **Source-Code des Projekts**\
├── pom.xml >>> **Maven Build-Konfiguration**\
├── README.md >>> **Projektdokumentation**\
└──.gitignore >>> **Git Ignore-Regeln**

## 📝 Installation

1. **Repository klonen**
   ```bash
   git clone https://gitlab.git.nrw/okaiser1/4d_viewer.git
   ```

2. **Abhängigkeiten installieren**\
   Navigieren Sie in das Projektverzeichnis und führen Sie Maven aus
   ```bash
   mvn clean install
   ```

3. **Projekt starten**
   ```bash
   mvn exec:java
   ```

## 🔧 Entwicklung

### Technologien

- **Entwicklungsumgebung (IDE)**: IntelliJ IDEA
- **Programmiersprache:** Java (OpenJDK 20)
- **Build-Tool:** Maven 3

### Abhängigkeiten

Auszug aus pom.xml:

| **Bibliothek**                  | **Version** | **Zweck / Beschreibung**                                            | 
|---------------------------------|-------------|---------------------------------------------------------------------|
| com.formdev:flatlaf             | 3.6         | Moderne, leichtgewichtige Look-and-Feel-Bibliothek für Swing-GUIs.  |
| com.formdev:flatlaf-extras      | 3.6         | Erweiterungen für FlatLaf, z. B. Themen, Icons und UI-Utilities.    |
| com.google.code.gson:gson       | 2.13.2      | Serialisierung und Deserialisierung von JSON-Daten                  |
| javax.swing                     | -           | GUI-Framework (Standardbibliothek) für Desktop-Anwendungen in Java. |
| org.jetbrains:annotations       | 26.0.2      | Statische Analyse-Annotationen für IntelliJ IDEA und andere Tools.  |
| org.junit.jupiter:junit-jupiter | 6.0.0       | JUnit 5 (Jupiter)-Plattform für moderne Teststrukturen.             |

### Build und Testen

- **Build ausführen**
  ```bash
  mvn compile
  ```

- **Tests starten**
  ```bash
  mvn test
  ```

## 📗 Dokumentation

- [Benutzeranleitung](LINK-ZUR-BENUTZERANLEITUNG) **[Link hinzufügen]**
- [API-Dokumentation](LINK-ZUR-API-DOKUMENTATION) **[Link hinzufügen]**

## 📸 Screenshots

![Beispielbild](images/example.png) **[Demo-GIFs, Screenshots und Beispiele hinzufügen]**

## 🗺️ Roadmap

- [x] Umstieg auf FlatLaf (Neues Erscheinungsbild)
- [x] Menüleiste (Fenster, Hilfe)
- [x] Verwendung von Shortcuts
- [x] Internationalisierung (DE/EN)
- [x] Modularisierung des Codes
- [x] Ladeleiste Optimierung
- [ ] Reiter "Markierungen"
- [ ] Reiter "Bearbeiten" Erweiterung
    - [ ] Helligkeit-, Kontrast-Regler
    - [ ] Zoom und Crop-Funktion
- [ ] Reiter "Navigieren" Erweiterung
- [ ] Performance-Optimierung

## 📊 Projektstatus

Das Projekt befindet sich aktuell in **aktiver Entwicklung**.
Ziel ist es, eine stabile, nutzerfreundliche und erweiterbare Plattform zur 4D-Visualisierung zu schaffen.

## 🤝 Beitragende

Ein großes Dankeschön an alle Entwickler, Tester und Nutzer, die dieses Projekt mitgestalten!
>„Ich glaube, das Projekt macht dir genauso viel Spaß wie mir – und wir sind auf dem richtigen Weg.“ ~ Olaf

## 🔐 Lizenz

Dieses Projekt ist unter der **[Lizenzname hinzufügen]** lizenziert. \
Details siehe [Lizenzdate](LINK-ZUR-LIZENZ) **[Link hinzufügen]**