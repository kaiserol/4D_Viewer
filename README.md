# 4D_Viewer

Ein leistungsstarkes und intuitives Tool zur Visualisierung und Analyse von vierdimensionalen Bildern. Dieses Projekt basiert auf Java und wurde entwickelt, um anspruchsvolle Visualisierungen für Mikroben zu ermöglichen.

## 🚀 Funktionsübersicht

- Visualisierung von 4D-Bildern
- Unterstützung für folgende Formate: [GIF, JPEG PNG, TIFF]

## 📂 Projektstruktur
4d_viewer:\
├── *images/* >>> **Test-Bilder für den 4D Viewer**\
├── *snapshots/* >>> **Momentaufnahmen, die zur Laufzeit erstellt werden**\
├── *src/* >>> **Source-Code des Projekts**\
├── *config.cfg/* >>> **Konfigurationsdatei für den 4D Viewer**\
├── *pom.xml* >>> **Konfigurationsdatei für Maven**\
├── *README.md* >>> **Projektdokumentation**\
└── *.gitignore* >>> **Konfigurationsdatei (Git Ignore)**

## 📝 Installation

1. **Repository klonen**
   ```bash
   git clone https://gitlab.git.nrw/okaiser1/4d_viewer.git
   ```

2. **Abhängigkeiten installieren**
   Navigieren Sie in das Projektverzeichnis und führen Sie Maven aus:
   ```bash
   mvn clean install
   ```

3. **Projekt starten**
   ```bash
   mvn exec:java
   ```

## 🔧 Entwicklung

### Technologien und Abhängigkeiten

- **Programmiersprache:** Java (OpenJDK 20)
- **Build-Tool:** Maven 3
- [Weitere Abhängigkeiten hier hinzufügen, z.B. Bibliotheken aus der `pom.xml`]

### Build und Testen

- **Build ausführen:**
  ```bash
  mvn compile
  ```

- **Tests ausführen:**
  ```bash
  mvn test
  ```

## 📗 Dokumentation

- [Benutzeranleitung](LINK-ZUR-BENUTZERANLEITUNG)
- [API-Dokumentation](LINK-ZUR-API-DOKUMENTATION)

## 📸 Screenshots

![Beispielbild](images/example.png)

[Demo-GIFs, Screenshots und Beispiele können hier hinzugefügt werden]

## 🗺️ Roadmap

- [ ] Internationalisierung (Übersetzung in mehrere Sprachen)
- [ ] Verbesserte Performance bei großen Datenmengen
- [ ] Erweiterung der Möglichkeiten zur Bildermanipulation

## 📊 Projektstatus

Dieses Projekt befindet sich aktuell in der **[aktiven Entwicklung / Wartung]**.

## 🤝 Beitragende

Ein großes Dankeschön an alle Entwickler, Tester und Nutzer, die zu diesem Projekt beigetragen haben!

## 🔐 Lizenz

Dieses Projekt ist unter der **[Lizenzname, z. B. MIT, Apache 2.0]** lizenziert. Sie können die Lizenz [hier](LINK-ZUR-LIZENZ) einsehen.