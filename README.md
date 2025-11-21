# 4D Viewer

Ein leistungsstarkes, intuitives und erweiterbares Tool zur
**Visualisierung, Analyse und Bearbeitung von vierdimensionalen Bilddaten**.
Entwickelt in **Java**, speziell für **mikroskopische und biologische Anwendungen**
(z. B. Embryonenanalyse), bietet der 4D Viewer eine moderne Oberfläche, performante
Darstellung und flexible Bildmanipulation.

## 🚀 Funktionsübersicht

- **Visualisierung von 4D-Bilddaten** (3D-Raum + Zeit) mit flüssiger Navigation
- **Interaktive Steuerung** über Maus und Tastatur (inkl. Shortcuts)
- **Bildbearbeitung**:
    - Horizontale/Vertikale Spiegelung
    - Rotation
- **Markierungen**:
    - Formen: Ellipse und Rechteck
    - Farbe, Größe und Position frei wählbar
- **Unterstützte Formate**: GIF, JPEG, PNG, TIFF
- **Internationalisierung**: Deutsch / Englisch
- **Anwendungsverzeichnis**:\
  `{Benutzerverzeichnis}/4D_Viewer/.config`\
  Enthält alle globalen Einstellungen und Nutzerdaten der Anwendung:
    - _history.txt_: Verlauf der zuletzt geöffneten Projekte
    - _settings.json_: Benutzereinstellungen (Sprache, Theme, Schriftgröße)
- **Projektverzeichnis**:\
  `{Benutzerverzeichnis}/4D_Viewer/projects/{Bilderverzeichnis}`\
  Beinhaltet alle projektspezifischen Dateien und Konfigurationen:
    - _snapshots/_: Ablage für Momentaufnahmen
    - _config.json_: projektbezogene Konfigurationen (Einheiten, Spiegelung, Rotation, Zoom)
    - _markers.json_: gespeicherte Markierungen aus der UI

## 🧩 Installation & Ausführung

1. **Repository klonen**
    ```bash
    git clone https://gitlab.git.nrw/okaiser1/4d_viewer.git
    cd 4d_viewer
    ```

2. **Abhängigkeiten installieren & Build erzeugen**
    ```bash
    mvn clean install && rm target/original-*.jar
    ```

3. **Anwendung starten**

   _Option A – Über die erzeugte JAR-Datei_
    ```bash
    java -jar target/*.jar
    ```

   _Option B – Direkt über Maven_
    ```bash
    mvn exec:java
    ```

## ⚙️ Troubleshooting & Entwicklungsumgebung

### Java-Version prüfen

- Prüfen Sie, ob Java 20 installiert ist:
    ```bash
    java -version
    ```
- Beispielsausgabe:
    ```bash
    openjdk version "20.0.2" 2023-06-14
    ```

Falls eine andere Version angezeigt wird, ggf. `JAVA_HOME` anpassen oder eine passende JDK-Version installieren.

### Wichtige Maven-Befehle

| Befehl                   | Zweck                            |
|--------------------------|----------------------------------|
| `mvn clean install`      | Projekt bereinigen und neu bauen |
| `mvn compile`            | Kompilieren des Projekts         |
| `mvn dependency:resolve` | Nur Abhängigkeiten aktualisieren |
| `mvn test`               | Tests ausführen                  |

### Hinweise für IntelliJ IDEA

1. Projekt öffnen → `4d_viewer/`
2. Project SDK auswählen: **JDK 20**
3. IntelliJ erkennt automatisch das Maven-Projekt
4. Zum Starten:
    - Menü: *Run → Run 'Main'*
    - oder eigene Run-Konfiguration mit Main-Class (z. B. `de.uzk.Main`)

## 📂 Projektstruktur

```
4d_viewer/
├── .idea/                  # IntelliJ-Projektkonfiguration
├── images/                 # Beispiel- und Testbilder
├── java_executables/       # Kompilierte Java-Programme (.jar)
├── screenshots/            # Screenshots für Dokumentation und Tests
├── src/
│   ├── main/
│   │   ├── java/           # Java-Quellcode
│   │   └── resources/      # Ressourcen (Bilder, META-INF, Sprachendateien)
│   └── test/
│       └── java/           # Testklassen
│
├── .editorconfig           # Definiert einheitliche Formatierungsregeln für den Quellcode
├── .gitignore              # Regeln für nicht zu versionierende Dateien
├── 4d_viewer.iml           # IntelliJ IDEA-Projektdatei
├── CHANGELOG.md            # Dokumentation der Änderungen
├── CONTRIBUTING.md         # Hinweise zu Beitragenden
├── LICENSE                 # Lizenzbedingungen
├── NOTICE                  # Urheberrechtshinweise
├── pom.xml                 # Maven-Build-Konfiguration
└── README.md               # Projektbeschreibung, Setup- und Nutzungshinweise
```

## 📸 Screenshots

[//]: # (### Helles Farbschema)

[//]: # ()
[//]: # (![App LightMode]&#40;src/main/resources/images/screenshots/App_LightMode.png&#41;)

[//]: # ()
[//]: # (### Dunkles Farbschema)

[//]: # ()
[//]: # (![App DarkMode]&#40;src/main/resources/images/screenshots/App_DarkMode.png&#41;)

### Einstellungen

Im Einstellungsfenster lassen sich Anzeigeoptionen individuell anpassen.

![Window Settings](src/main/resources/images/screenshots/Window_Settings.png)

_(Weitere Demo-GIFs, Screenshots und Beispiele folgen)_

## 🔧 Entwicklung

### Technologien

- **IDE:** IntelliJ IDEA (Community / Ultimate)
- **Sprache:** Java 20 (OpenJDK)
- **Build-Tool:** Apache Maven 3.9.11

### Abhängigkeiten (Auszug)

| Bibliothek                                       | Version | Zweck                               |
|--------------------------------------------------|---------|-------------------------------------|
| `com.fasterxml.jackson.core:jackson-annotations` | 2.20    | JSON-(De-)Serialisierung            |
| `tools.jackson.core:jackson-core`                | 3.0.1   | JSON Parser/Generator               |
| `tools.jackson.core:jackson-databinding`         | 3.0.1   | Objektabbildung (POJO ↔ JSON)       |
| `com.formdev:flatlaf`                            | 3.6     | Modernes Look & Feel für Swing      |
| `com.formdev:flatlaf-extras`                     | 3.6     | Erweiterungen für FlatLaf           |
| `org.jetbrains:annotations`                      | 26.0.2  | Annotationen für statische Analysen |
| `org.junit.jupiter:junit-jupiter`                | 6.0.0   | Test-Framework (JUnit 6)            |

## 📊 Projektstatus

Das Projekt befindet sich aktuell in **aktiver Entwicklung**.
Ziel ist es, eine stabile, nutzerfreundliche und erweiterbare Plattform zur 4D-Visualisierung zu schaffen.

## 🗂️ Änderungsprotokoll

Die Datei [CHANGELOG.md](CHANGELOG.md) dokumentiert alle wichtigen Änderungen, Neuerungen und Versionen des Projekts.
Es wird empfohlen, diese Datei bei Updates oder neuen Releases zu prüfen, um über aktuelle Anpassungen und Verbesserungen zu informieren.

## 🔐 Lizenz & rechtliche Hinweise

Dieses Projekt steht unter der [Apache License 2.0](LICENSE)<br><br>
Bitte beachten Sie, dass die Datei [NOTICE](NOTICE) Teil der Lizenzanforderungen ist und zusammen mit der Software
verteilt werden muss.<br><br>
Copyright © 2025 Universität zu Köln

## 🤝 Beitragende

Ein großes Dankeschön an **alle Entwickler, Tester und Nutzer**, die dieses Projekt mitgestalten!
Eine detaillierte Übersicht über alle Beitragende finden Sie in der [CONTRIBUTING.md](CONTRIBUTING.md).