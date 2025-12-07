# 📜 Changelog – 4D Viewer

Alle nennenswerten Änderungen an diesem Projekt werden in dieser Datei dokumentiert.
Das Format orientiert sich an [Keep a Changelog](https://keepachangelog.com/de/1.1.0/) und folgt
dem [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Geplante und in Entwicklung befindliche Funktionen

- **Erweiterung des Reiters „Bearbeiten“**
    - Neues Panel mit Schaltflächen für gängige Aktionen
    - Zoom- und Zuschneidefunktionen (Crop)
    - Regler für Helligkeit, Kontrast und Sättigung

- **Überarbeitung des Reiters „Navigieren“**
    - Neues Panel mit Schaltflächen für gängige Aktionen
    - Vereinfachte und intuitive Bildnavigation

- **Allgemeine Verbesserungen (geplant)**
    - Leistungsoptimierungen beim Laden großer Datensätze
    - Erweiterte Einstellungsoptionen (Tastenkürzel)

### Hinzugefügt

- **Internationalisierung (Deutsch / Englisch)** implementiert
- **Markierungsfunktion** mit frei definierbaren Formen, Farben und Größen
- **Einstellungsverwaltung** mit persistentem Speichern im Appverzeichnis
- **Einstellungsdialog** mit Profilverwaltung (Sprache, Farbschema, Schriftgröße)
- **Automatisches Projektspeichern** (Konfigurationen & Markierungen)
- **Modernisiertes UI-Design** mit abgerundeten Komponenten und blauen Akzenten als primärer Farbgebung

### Geändert

- **Code modularisiert** zur besseren Wartbarkeit und Erweiterbarkeit (Klassen feiner voneinander in Pakete abgegrenzt)
- **Tooltips** und kontextsensitive Hilfe deutlich verbessert

## [2.0] - 23.11.2023

### Hinzugefügt

- **FlatLaf** als neues Look & Feel integriert
- **Darstellungsoptionen**: Unterstützung für Dark- und Light-Mode
- **Unterstützte Formate**: GIF und PNG
- **Bildbearbeitung** Drehen von Bildern
- **Tastenkombinationen (Shortcuts)** implementiert
- **Menüleiste** hinzugefügt

### Verbessert

- **Code modularisiert** Klassen nach Funktionstyp in Pakete untergliedert (Actions, Handler, ...)
- **Ladezeiten** großer Bildverzeichnisse deutlich reduziert
- **Ladedialog** mit Fortschrittsanzeige und besserem Benutzerfeedback

### Gelöscht

- **Unterstütztes Format**: LWF

--------------------------------------------------------------------------------

## [1.2] - 02.03.2017

- **Versionsname**: Colonia 1.3

--------------------------------------------------------------------------------

## [1.1] - 24.08.2010

- **Versionsname**: 0.100824

## Hinzugefügt

- **Einstellbare Ebenendifferenz**
    - Analoge Funktion zur Zeitdifferenz
    - Einführung einer konfigurierbaren Einheit für Ebenen (Mikrometer/Ebene)
- **Bildbearbeitung**
    - **Horizontale und vertikale Spiegelung** des aktuell angezeigten Bildes
    - **Neue Buttons** in der UI (Spiegelung wird sofort angewendet)
- **Momentaufnahmen (Snapshots)**
    - Speichern einzelner Bilder im Verzeichnis der ausführbaren Anwendung
    - Snapshot-Button in der UI hinzugefügt
    - Dateiformat der gespeicherten Bilder:\
      `images_<frame-sep><cur_frame><level_sep><cur_level>(<snapshot-count>).<Dateityp>`
    - Snapshot-Zähler wird bei jedem Programmstart auf 0 gesetzt
- **Checkpoint-Funktion**
    - Setzen eines **Referenz-Frames** während der Navigation in der Frame-Achse
    - Beim erneuten Aktivieren wird die **Frame-Differenz** zwischen dem aktuellen Frame und dem gesetzten Checkpoint
      automatisch berechnet und angezeigt

## Geändert

- **Der konkrete Bildname** wird bei der Navigation korrekt aktualisiert und in der UI angezeigt (Anzeige im Pfad des
  Bildverzeichnisses)
- **Statusanzeige erweitert**
    - Analog zur Zeitdifferenz wird nun auch die Ebenendifferenz in Klammern hinter dem Level angezeigt
    - Checkpoint-Funktion in die Statusleiste integriert (Anzeige der Frame-Differenz zum gesetzten Referenz-Frame)

--------------------------------------------------------------------------------

## [1.0] - 08.06.2010

- **Versionsname**: 0.61
- **Erste lauffähige Grundversion des 4D-Viewers** zur Visualisierung von Bildsequenzen in Zeit- und Ebenenrichtung

### Hinzugefügt

- **Anzeige von 4D-Daten** als einfache Bildsequenz ohne Skalierung – Bilder werden stets in ihrer Originalgröße
  angezeigt.
- **Unterstützte Bildformate**:
    - TIFF (Standard)
    - JPEG und LWF über auswählbaren Dateityp
- **Grundnavigation und Bedienung**
    - **UI-Buttons** für:
        - Frame vor/zurück
        - Sprünge um ±10 Frames
        - Ebenen vor/zurück\
          _(Hinweis: In dieser Version noch ohne vollständige Navigationslogik)_
    - **Tastatursteuerung**:
        - Links/Rechts: Frame vor/zurück
        - Seite Hoch/Runter: Sprünge um ±10 Frames
        - Hoch/Runter: Ebenen vor/zurück\
          _(Hinweis: In dieser Version noch ohne vollständige Navigationslogik)_
    - **Anzeigenbereich** zur Darstellung des aktuell geladenen Bildes
    - **Horizontale und vertikale Bildlaufleisten im Anzeigebereich** ermöglichen die direkte Auswahl und Anpassung von
      Frame und Ebene
- **Anzeige des Bildverzeichnisses** in der UI\
  _(Hinweis: Der angezeigte Pfad wird in dieser Version nicht dynamisch beim Navigieren aktualisiert; der konkrete
  Bildname wird nicht mit angezeigt)_
- **Einstellbare Zeitdifferenz**
    - Zeitdifferenz pro Frame (Sekunden/Frame) frei konfigurierbar
    - Automatische Berechnung und Anzeige der Gesamtzeit
- **Statusanzeige**:
    - Darstellung des aktuellen **Frames** und der aktuellen **Ebene**
    - Hinter dem Frame-Wert wird zusätzlich in Klammern die berechnete Gesamtzeit im Format `hh:mm:ss` angezeigt
- **Verzeichniswahl über Dialog** zum Laden von Bilddaten
- **Konfigurierbares Dateinamensschema**:
    - Einstellbare Trenner für Frame- und Ebeneninformationen
    - Unterstützung für unterschiedliche Dateitypen
- **Konfigurationsdatei** `config.cfg`:
    - Speicherung des zuletzt verwendeten Bildverzeichnisses
    - Speicherung des zuletzt gewählten Bildtyps